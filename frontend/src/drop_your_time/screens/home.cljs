(ns drop-your-time.screens.home
  (:require [accountant.core :as accountant]
            [drop-your-time.components.general :as general]
            [drop-your-time.i18n :refer [t add-tr]]))

(add-tr :en
        ::about "Drop Your Time is a simple tool to find a time to meet with your friends."
        ::new-poll-btn "Create a new poll")

(defn home []
  [:div.container
   [general/header]

   [:p.text-center (t ::about)]

   [general/screen-wide-btn
    {:on-click #(accountant/navigate! "/new-poll-header")}
    (t ::new-poll-btn)]])
