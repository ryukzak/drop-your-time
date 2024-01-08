let generate_uuid () =
  let st = Random.get_state () in
  ignore (Random.bits ());
  let uuid = Uuidm.v4_gen st () in
  Uuidm.to_string uuid
;;

let create_poll poll db =
  let uuid = generate_uuid () in
  let%lwt status = Models.insert_poll ~uuid ~poll db in
  Lwt.return (status, uuid)
;;

let read_poll uuid db =
  let%lwt poll = Models.get_poll ~uuid db in
  Lwt.return poll
;;

let update_poll uuid author vote (module Db : Caqti_lwt.CONNECTION) =
  Db.with_transaction (fun () ->
    let%lwt poll_resp = Models.get_poll ~uuid (module Db) in
    let poll = poll_resp |> Result.get_ok in
    Hashtbl.mem poll.votes author
    |> function
    | true -> Lwt.return (Error `Already_voted)
    | false ->
      Hashtbl.add poll.votes author { slots = vote };
      let%lwt _ = Models.update_poll ~uuid ~poll (module Db) in
      Lwt.return (Ok poll))
;;
