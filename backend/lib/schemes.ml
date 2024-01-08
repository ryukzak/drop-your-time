type slot = { start : int64 } [@@deriving show, yojson]
type vote = { slots : int64 list } [@@deriving show, yojson]
type votes = (string, vote) Hashtbl.t

let votes_to_yojson votes =
  let assoc =
    Hashtbl.fold (fun name vote acc -> (name, vote_to_yojson vote) :: acc) votes []
  in
  `Assoc assoc
;;

let%test "votes_to_yojson" =
  let votes =
    [ "Alice", { slots = [ 3_147_483_647L ] }; "Bob", { slots = [ 4L ] } ]
    |> List.to_seq
    |> Hashtbl.of_seq
  in
  let actual = votes |> votes_to_yojson |> Yojson.Safe.to_string in
  actual = "{\"Alice\":{\"slots\":[3147483647]},\"Bob\":{\"slots\":[4]}}"
;;

let votes_of_yojson = function
  | `Assoc assoc ->
    let votes = Hashtbl.create (List.length assoc) in
    List.iter
      (fun (name, vote) -> Hashtbl.add votes name (vote_of_yojson vote |> Result.get_ok))
      assoc;
    Ok votes
  | _ -> Error "Expected an object"
;;

let%test "votes_of_yojson" =
  let json = "{\"Alice\":{\"slots\":[3147483647]},\"Bob\":{\"slots\":[4]}}" in
  let actual = json |> Yojson.Safe.from_string |> votes_of_yojson
  and expected =
    [ "Alice", { slots = [ 3_147_483_647L ] }; "Bob", { slots = [ 4L ] } ]
    |> List.to_seq
    |> Hashtbl.of_seq
    |> Ok
  in
  actual = expected
;;

let pp_votes fmt votes =
  let assoc =
    Hashtbl.fold
      (fun name vote acc ->
        (name, vote.slots |> List.map Int64.to_string |> String.concat ", ") :: acc)
      votes
      []
  in
  let assoc' = List.map (fun (k, v) -> k ^ ": " ^ v) assoc in
  Format.fprintf fmt "%s" (String.concat "\n" assoc')
;;

type poll =
  { duration : int64
  ; title : string
  ; poll_timezone : string [@key "poll-timezone"]
  ; user_timezone : string [@key "user-timezone"]
  ; author : string
  ; slots : slot list
  ; votes : votes
  }
[@@deriving show, yojson]

let%test_unit "poll_to_yojson" =
  let poll =
    { duration = 1L
    ; title = "title"
    ; poll_timezone = "UTC"
    ; user_timezone = "UTC"
    ; author = "author"
    ; slots = [ { start = 1L } ]
    ; votes =
        [ "Alice", { slots = [ 3_147_483_647L ] }; "Bob", { slots = [ 4L ] } ]
        |> List.to_seq
        |> Hashtbl.of_seq
    }
  in
  let actual = poll |> poll_to_yojson |> Yojson.Safe.to_string in
  let expected =
    "{ \"duration\":1, \"title\":\"title\", \"poll-timezone\":\"UTC\", \
     \"user-timezone\":\"UTC\", \"author\":\"author\", \"slots\":[{\"start\":1}], \
     \"votes\":{\"Alice\":{\"slots\":[3147483647]},\"Bob\":{\"slots\":[4]}} }"
    |> Yojson.Safe.from_string
    |> Yojson.Safe.to_string
  in
  Base.([%test_eq: string] actual expected)
;;

let%test "poll_of_yojson" =
  let json =
    "{ \"duration\":1, \"title\":\"title\", \"poll-timezone\":\"UTC\", \
     \"user-timezone\":\"UTC\", \"author\":\"author\", \"slots\":[{\"start\":1}], \
     \"votes\":{\"Alice\":{\"slots\":[3147483647]},\"Bob\":{\"slots\":[4]}} }"
  in
  let actual = json |> Yojson.Safe.from_string |> poll_of_yojson |> Result.get_ok
  and expected =
    { duration = 1L
    ; title = "title"
    ; poll_timezone = "UTC"
    ; user_timezone = "UTC"
    ; author = "author"
    ; slots = [ { start = 1L } ]
    ; votes =
        [ "Alice", { slots = [ 3_147_483_647L ] }; "Bob", { slots = [ 4L ] } ]
        |> List.to_seq
        |> Hashtbl.of_seq
    }
  in
  actual = expected
;;

let poll_to_string poll = poll |> poll_to_yojson |> Yojson.Safe.to_string
let poll_of_string s = s |> Yojson.Safe.from_string |> poll_of_yojson |> Result.get_ok
