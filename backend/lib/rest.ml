let cors_middleware handler req =
  let handlers =
    [ "Access-Control-Allow-Origin", "*"
    ; "Access-Control-Allow-Methods", "*"
    ; "Access-Control-Allow-Headers", "*"
    ]
  in
  let%lwt res = handler req in
  handlers |> List.map (fun (key, value) -> Dream.add_header res key value) |> ignore;
  Lwt.return res
;;

open Schemes

type vote =
  { author : string
  ; vote : int64 list
  }
[@@deriving yojson]

let internal_error e = Dream.respond ~status:`Internal_Server_Error (Caqti_error.show e)

let post_poll_handler request =
  let%lwt body = Dream.body request in
  let poll = poll_of_string body in
  let%lwt status, uuid = Dream.sql request (Crud.create_poll poll) in
  match status with
  | Error e -> Dream.respond ~status:`Internal_Server_Error Caqti_error.(show e)
  | Ok () ->
    `Assoc [ "poll_uuid", `String uuid; "poll", poll_to_yojson poll ]
    |> Yojson.Safe.to_string
    |> Dream.json
;;

let get_vote_handler request =
  let uuid = Dream.param request "uuid" in
  let%lwt poll = Dream.sql request (Crud.read_poll uuid) in
  match poll with
  | Ok poll' -> poll' |> poll_to_string |> Dream.json
  | Error e -> failwith @@ Caqti_error.show e
;;

let post_vote_handler request =
  let uuid = Dream.param request "uuid" in
  let%lwt body = Dream.body request in
  let new_vote = body |> Yojson.Safe.from_string |> vote_of_yojson |> Result.get_ok in
  let%lwt new_poll_res =
    Dream.sql request (Crud.update_poll uuid new_vote.author new_vote.vote)
  in
  match new_poll_res with
  | Error `Already_voted ->
    Dream.respond ~status:`Forbidden ("Already voted: " ^ new_vote.author)
  | Error ((#Caqti_error.call | #Caqti_error.retrieve) as e) ->
    Dream.respond ~status:`Internal_Server_Error (Caqti_error.show e)
  | Ok new_poll ->
    `Assoc [ "poll_uuid", `String uuid; "poll", poll_to_yojson new_poll ]
    |> Yojson.Safe.to_string
    |> Dream.json
;;
