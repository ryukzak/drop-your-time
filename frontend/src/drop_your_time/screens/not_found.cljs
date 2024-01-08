(ns drop-your-time.screens.not-found
  (:require [accountant.core :as accountant]
            [drop-your-time.components.general :as general]
            [drop-your-time.i18n :refer [t add-tr]]))

(add-tr :en
        ::not-found "Page not found"
        ::home-link "Go to home")

(defn not-found []
  [:div.container
   [general/header (t ::not-found)]
   [:a {:href "#" :on-click #(accountant/navigate! "/")} (t ::home-link)]])
