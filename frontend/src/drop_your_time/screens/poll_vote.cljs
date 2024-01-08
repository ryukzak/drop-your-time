(ns drop-your-time.screens.poll-vote
  (:require [clojure.spec.alpha :as s]
            [drop-your-time.components.general :as general]
            [drop-your-time.components.poll :as pollc]
            [drop-your-time.i18n :refer [t add-tr]]
            [drop-your-time.models.poll :as poll]
            [drop-your-time.service :as service]
            [reagent.core :as r]))

(add-tr :en
        ::title "Make your vote"
        ::form-name-label "Your name"
        ::vote-btn "Vote")

(defonce *state (r/atom {:uuid nil
                         :author ""
                         :vote #{}}))

(defn add-vote! [start]
  (swap! *state update :vote conj start))

(defn rem-vote! [start]
  (swap! *state update :vote disj start))

(defn slots-overview []
  [:div.row.row-cols-md-2.row-cols-lg-3
   (let [slots-by-days (poll/slots-by-days)
         my-vote (@*state :vote)
         other-votes (:votes @poll/*poll)]

     (for [[day slots] slots-by-days]
       ^{:key day}
       [:div.col
        [:h4.text-center.text-nowrap day]
        (for [{:keys [start] :as slot} slots]
          ^{:key start} [pollc/slot-card {:slot slot
                                          :other-votes other-votes
                                          :my-vote my-vote
                                          :add-vote add-vote!
                                          :rem-vote rem-vote!}])]))])

(s/def ::vote-author (s/and ::poll/not-blank-string
                            #(nil? (get (@poll/*poll :votes) (keyword %)))))

(defn screen [uuid]
  (swap! *state assoc :uuid uuid)
  (service/get-poll uuid #(reset! poll/*poll %))
  (fn []
    [:div.container
     (general/header (t ::title))

     [pollc/poll-header]

     [slots-overview]

     [:div.mt-3
      [:label.form-label {:for :name-input} (t ::form-name-label)]
      [:input.form-control {:type :text
                            :id :name-input
                            :class (general/valid-cls ::vote-author (:author @*state))
                            :value (:author @*state)
                            :on-change #(swap! *state assoc
                                               :author (-> % .-target .-value))}]]

     (general/screen-wide-btn
      {:on-click #(service/submit-vote
                   uuid
                   (-> @*state :author)
                   (-> @*state :vote)
                   (fn [body] (reset! poll/*poll (-> body :poll))))
       ;; :disabled (or (empty? (:vote @*state))
       ;;               (not (s/valid?  ::vote-author (-> @*state :author))))
       }
      (t ::vote-btn))

     (general/debug @poll/*poll @*state)]))
