(ns drop-your-time.screens.new-poll-time-slots
  (:require [accountant.core :as accountant]
            [drop-your-time.components.general :as general]
            [drop-your-time.components.poll :as pollc]
            [drop-your-time.i18n :refer [t add-tr]]
            [drop-your-time.misc :as misc]
            [drop-your-time.models.poll :as poll]
            [reagent.core :as r]))

(add-tr :en
        ::title "Define time slots"
        ::next-btn "Next (check your poll)"
        ::min "min")

(defonce *state
  (r/atom {:selected-date (misc/today)}))

(defn create-time-slot! [start]
  (swap! poll/*poll update-in [:slots]
         (fn [slots] (into [] (sort-by :start (cons {:start start} slots))))))

(defn today! []
  (swap! *state update :selected-date
         (fn [_] (misc/today))))

(defn prev-day! []
  (swap! *state update :selected-date
         #(- % (* 24 60 60 1000))))

(defn next-day! []
  (swap! *state update :selected-date
         #(+ % (* 24 60 60 1000))))

(defn day-header []
  [:div.row.mt-3
   [:div.col-auto.text-start
    [:button.btn {:type "button" :on-click prev-day!}
     [:i.bi.bi-chevron-left]]]

   [:div.col.text-center {:on-click today!}
    [:h3 (misc/day-title (-> @*state :selected-date))]]

   [:div.col-auto.text-end
    [:button.btn {:type "button" :on-click next-day!}
     [:i.bi.bi-chevron-right]]]])

(defn time-slots-to-cards [all-slots day-start duration]
  (let [next-time-slot-start (fn [result]
                               (-> result last
                                   ((fn [[_type {start :start}]] start))
                                   (+ duration)))
        first-add-btn
        (if (empty? all-slots)
          [:add-button (.. (new js/Date. day-start) (setHours 8 0 0 0))]
          [:add-button (-> all-slots first :start (- duration))])]

    (loop [result [first-add-btn]
           [slot & slots] all-slots]
      (cond
        (empty? all-slots) result

        (nil? slot)
        (conj result
              [:add-button (next-time-slot-start result)])

        (and (some? (first slots))
             (<= duration
                 (- (some-> slots first :start)
                    (-> slot :start (+ duration)))))
        (recur (conj result
                     [:slot slot]
                     [:add-button (-> slot :start (+ duration))])

               slots)

        :else
        (recur (conj result [:slot slot])
               slots)))))

(defmulti render-card (fn [x] x))

(defmethod render-card :add-button [_ start]
  [:div.d-grid.gap-2.mt-3
   [:button.btn.btn-outline-dark.btn-md
    {:type "button" :on-click #(create-time-slot! start)}
    [:i.bi.bi-plus-square]]])

(defmethod render-card :slot [_ slot]
  [:div.card.mt-3
   [:div.card-body
    [:div.card-text
     [:div.row
      [:div.col.col-auto [pollc/interval slot]]

      [:div.col.text-end

       [:button.btn.btn-outline-dark
        {:type "button" :on-click (-> slot :handlers :delete-me)}
        [:i.bi.bi-trash]]]]]

    [:div.row.mt-3
     (for [offset [5 15 -15 -5]]
       ^{:key offset}
       [:div.col.d-flex.justify-content-center
        [:a.card-link.col-auto {:href "#"
                                :on-click #((-> slot :handlers :move-me) offset)}
         (str (if (neg? offset) "" "+") offset " " (t ::min))]])
     [:div.col.d-flex.justify-content-center
      [:input
       {:type :time
        :value (misc/utime-to-hhmm (:start slot))
        :on-change #(let [new-hhmm (-> % .-target .-value)]
                      ;; FIXME: type input working only if you type very fast.
                      ((-> slot :handlers :set-hhmm) (:start slot) new-hhmm))}]]]]])

(defn render-day-cards [day]
  [:div.row
   (let [slots-by-days (->> @poll/*poll
                            poll/with-handlers
                            :slots
                            (group-by #(-> % :start misc/local-date)))
         slots (get slots-by-days (misc/local-date day))
         duration (:duration @poll/*poll)]
     [:div.col-lg
      (for [[idx card] (map-indexed vector
                                    (time-slots-to-cards slots day duration))]
        ^{:key idx}
        [:<> (apply render-card card)])])])

(defn screen []
  [:div.container
   [general/header (t ::title)]

   [pollc/poll-header "/new-poll-header"]

   [day-header]

   [render-day-cards (-> @*state :selected-date)]

   [general/screen-wide-btn
    {:on-click #(accountant/navigate! "/new-poll-confirm")
     :class "mt-3"}
    (t ::next-btn)]

   [general/debug @poll/*poll @*state]])
