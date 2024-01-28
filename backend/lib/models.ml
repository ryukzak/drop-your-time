(* NOTE: it should be a mongodb, but mongo driver doesn't work with ocaml 5 *)

open Schemes

let create_poll_table =
  let query =
    let module T = Caqti_type in
    let open Caqti_request.Infix in
    (T.unit ->. T.unit)
      "CREATE TABLE IF NOT EXISTS 'polls' (\n'id' VARCHAR(36) UNIQUE,\n'poll' TEXT\n)"
  in
  fun (module Db : Caqti_lwt.CONNECTION) ->
    let%lwt unit_or_error = Db.exec query () in
    Caqti_lwt.or_fail unit_or_error
;;

let init_models db = Lwt_main.run (create_poll_table db)

let insert_poll_query =
  [%rapper
    execute
      {sql|
      INSERT INTO polls (@string{id}, @string{poll})
      VALUES (%string{uuid}, %string{poll_record})
      |sql}]
;;

let insert_poll ~uuid ~poll db =
  let poll_record = poll |> poll_to_yojson |> Yojson.Safe.to_string in
  insert_poll_query ~uuid ~poll_record db
;;

let update_poll_query =
  [%rapper
    execute
      {sql|
      UPDATE polls
      SET poll = %string{poll}
      WHERE id = %string{uuid}
      |sql}]
;;

let update_poll ~uuid ~poll db =
  let poll_record = poll_to_string poll in
  update_poll_query ~uuid ~poll:poll_record db
;;

let get_poll_query =
  [%rapper
    get_opt
      {sql|
      SELECT @string{poll}
      FROM polls
      WHERE id = %string{uuid}
      |sql}]
;;

let get_poll ~uuid db =
  let%lwt resp = get_poll_query ~uuid db in
  match resp with
  | Ok poll_str -> poll_str |> Option.get |> poll_of_string |> Lwt.return_ok
  | Error e -> Lwt.return_error e
;;
