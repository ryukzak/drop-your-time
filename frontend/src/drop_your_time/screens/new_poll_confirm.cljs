(ns drop-your-time.screens.new-poll-confirm
  (:require [accountant.core :as accountant]
            [drop-your-time.components.general :as general]
            [drop-your-time.components.poll :as pollc]
            [drop-your-time.i18n :refer [t add-tr]]
            [drop-your-time.misc :as misc]
            [drop-your-time.models.poll :as poll]
            [drop-your-time.service :as service]
            [reagent.core :as r]))

(add-tr :en
        ::title "Check your poll"
        ::create-poll-btn "Create poll & get the link"
        ::open-poll-btn "Open poll in new tab")

(def *state (r/atom {:uuid nil}))

(defn slot-overview [slot]
  [:div.card.mt-3
   [:div.card-body
    [:div.card-text
     [:div.row
      [:div.col-auto [pollc/interval slot]]]]]])

(defn slots-overview []
  [:div.row.row-cols-md-2.row-cols-lg-3
   (let [slots-by-days (->> @poll/*poll
                            :slots
                            (group-by #(-> % :start misc/day-title)))]
     (for [[day slots] slots-by-days]
       ^{:key day}
       [:div.col
        [:h4.text-center.text-nowrap day]
        (for [{:keys [start] :as slot} slots]
          ^{:key start}
          [pollc/slot-card {:slot slot}])]))])

(defn container-wide-btn [label on-click]
  [:div.d-grid.gap-2
   [:button.btn.btn-primary.btn-md
    {:on-click on-click}
    label]])

(defn screen []
  [:div.container
   [general/header (t ::title)]
   [pollc/poll-header]

   [slots-overview]

   [general/screen-wide-btn
    {:on-click #(do (poll/initial-votes!)
                    (service/submit-poll
                     @poll/*poll
                     (fn [body] (swap! *state assoc :uuid (-> body :poll_uuid)))))}
    (t ::create-poll-btn)]

   (let [uuid (-> @*state :uuid)
         local-href (str "/polls/" uuid)
         href (str service/host local-href)]
     (when uuid
       [:<>
        [:pre.mt-3 href]
        [general/screen-wide-btn
         {:on-click #(accountant/navigate! local-href)}
         (t ::open-poll-btn)]]))

   (general/debug @poll/*poll @*state)])
