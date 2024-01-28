(ns drop-your-time.router
  (:require ["react-dom/client" :refer [createRoot]]
            [accountant.core :as accountant]
            [bidi.bidi :as bidi]
            [drop-your-time.screens.home :as home]
            [drop-your-time.screens.new-poll-confirm :as new-poll-confirm]
            [drop-your-time.screens.new-poll-header :as new-poll-header]
            [drop-your-time.screens.new-poll-time-slots :as new-poll-time-slots]
            [drop-your-time.screens.not-found :as not-found]
            [drop-your-time.screens.poll-vote :as poll-vote]
            [goog.dom :as gdom]
            [reagent.core :as r]))

(def route ["/" {"" :home
                 "home" :home

                 "new-poll-header" :new-poll-header
                 "new-poll-time-slots" :new-poll-time-slots
                 "new-poll-confirm" :new-poll-confirm

                 ["polls/" :uuid] :polls

                 "help" :help}])

(defmulti page-contents identity)

(defmethod page-contents :home []
  (fn [] [home/home]))

(defmethod page-contents :new-poll-header []
  (fn [] [new-poll-header/screen]))

(defmethod page-contents :new-poll-time-slots []
  (fn [] [new-poll-time-slots/screen]))

(defmethod page-contents :new-poll-confirm []
  (fn [] [new-poll-confirm/screen]))

(defmethod page-contents :polls [_ params]
  (fn [] [poll-vote/screen (:uuid params)]))

(defmethod page-contents :help []
  (fn [] [:span.main
          [:h1 "About routing-example"]]))

(defonce root (createRoot (gdom/getElement "app")))

(defn render-app
  [page]
  (.render root (r/as-element [page])))

(defn init []
  (accountant/configure-navigation!
   {:nav-handler (fn [path]
                   (let [{handler :handler
                          route-params :route-params
                          :as route} (bidi/match-route route path)]
                     (prn :path path :route route)
                     (render-app (if handler
                                   (page-contents handler route-params)
                                   #(not-found/not-found)))))
    :path-exists? (fn [path]
                    (boolean (bidi/match-route route path)))})

  (accountant/dispatch-current!))

(defn ^:dev/after-load start []
  (js/console.log "start"))

;; this is called before any code is reloaded
(defn ^:dev/before-load stop []
  (js/console.log "stop"))
