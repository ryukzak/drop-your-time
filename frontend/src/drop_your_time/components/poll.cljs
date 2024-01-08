(ns drop-your-time.components.poll
  (:require [accountant.core :as accountant]
            [clojure.string :as str]
            [drop-your-time.i18n :refer [t add-tr]]
            [drop-your-time.misc :as misc]
            [drop-your-time.models.poll :as poll]))

(add-tr :en
        ::by "by "
        ::at "at "
        ::slot-duration " min slots"
        ::vote-count "person voted")

(defn interval [slot]
  [:<>
   (for [[idx line] (->> (poll/slot-interval slot)
                         (map-indexed vector))]
     ^{:key idx} [:<> [:span line] [:br]])])

(defn poll-header
  ([] (poll-header nil))
  ([edit-url]
   [:div.row
    [:div.col
     [:h1.display-6 (poll/title)]
     [:div.row
      (for [[prefix value postfix] [[(t ::by) (poll/author) nil]
                                    [(t ::at) (misc/name-tz-to-label (poll/poll-timezone)) nil]
                                    [nil (poll/slot-duration) (t ::slot-duration)]]]
        ^{:key value}
        [:div.col.d-flex.justify-content-center
         [:p.text-nowrap prefix value postfix]])]]

    (when (some? edit-url)
      [:div.col-auto.text-end
       [:button.btn.btn-outline-dark
        {:type "button" :on-click #(accountant/navigate! edit-url)}
        [:i.bi.bi-pencil]]])]))

(defn slot-card [{{:keys [start] :as slot} :slot
                  other-votes :other-votes
                  my-vote :my-vote
                  add-vote :add-vote
                  rem-vote :rem-vote}]

  (let [is-my-vote (->> my-vote (some #{start}))
        votes (->> other-votes
                   (filter (fn [[_ votes]] (some #{start} (:slots votes))))
                   (map first))
        accordion-id (str "accordion-" start)]
    [:div.card.mt-3
     [:div.card-body
      [:div.card-text.row
       [:div.col [interval slot]]
       (when (some? my-vote)
         [:div.col-auto.text-end
          (if is-my-vote
            [:i.bi.bi-check-circle-fill.h2 {:on-click #(rem-vote start)}]
            [:i.bi.bi-check-circle.h2 {:on-click #(add-vote start)}])])]]

     (when (some? other-votes)
       [:div.card-footer.text-muted
        [:div.accordion
         [:div.accordion-item
          [:button.accordion-button.collapsed {:type :button
                                               :data-bs-toggle :collapse
                                               :data-bs-target (str "#" accordion-id)
                                               :aria-expanded :false
                                               :aria-controls accordion-id}
           [:p.accordion-header (str (count votes) " " (t ::vote-count))]]
          [:div.accordion-collapse.collapse {:id accordion-id :aria-labelledby accordion-id}
           [:div.accordion-body (->> votes (map name) (str/join ", "))]]]]])]))
