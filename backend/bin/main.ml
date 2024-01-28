module Crud = Backend.Crud
module Rest = Backend.Rest

let db connect_string =
  let init_conn () =
    let connect = Caqti_lwt.connect (Uri.of_string connect_string) in
    Lwt_main.run (Lwt.bind connect Caqti_lwt.or_fail)
  in
  (module (val init_conn ()) : Caqti_lwt.CONNECTION)
;;

let api =
  Dream.scope
    "/api"
    []
    [ (Dream.options "/polls" @@ fun _request -> Dream.empty `OK)
    ; Dream.post "/polls" @@ Rest.post_poll_handler
    ; (Dream.options "/polls/:uuid" @@ fun _request -> Dream.empty `OK)
    ; Dream.get "/polls/:uuid" @@ Rest.get_vote_handler
    ; (Dream.options "/polls/:uuid" @@ fun _request -> Dream.empty `OK)
    ; Dream.post "/polls/:uuid" @@ Rest.post_vote_handler
    ]
;;

let () =
  let connect_string =
    Sys.getenv_opt "CONNECTION_STRING" |> Option.value ~default:"sqlite3:db.sqlite"
  and js_path = Sys.getenv_opt "JS_PATH" |> Option.value ~default:"../frontend/target/js"
  and html_path =
    Sys.getenv_opt "HTML_PATH" |> Option.value ~default:"../frontend/resources/public"
  in
  print_endline ("Using connection string: " ^ connect_string);
  print_endline ("Using JS path: " ^ js_path);
  print_endline ("Using HTML path: " ^ html_path);
  Dream.initialize_log ~level:`Debug ();
  Random.self_init ();
  Backend.Models.init_models (db connect_string);
  Dream.run ~interface:"0.0.0.0"
  @@ Dream.logger
  (* @@ Dream.pipeline [ Backend.Rest.cors_middleware ] *)
  @@ Dream.sql_pool connect_string
  @@ Dream.router
       [ api
       ; Dream.get "/js/**" @@ Dream.static js_path
       ; Dream.get "/**" @@ Dream.from_filesystem html_path "index.html"
       ]
;;
