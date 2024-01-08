(ns drop-your-time.screens.new-poll-header
  (:require [accountant.core :as accountant]
            [clojure.spec.alpha :as s]
            [clojure.string :as str]
            [drop-your-time.components.general :as general]
            [drop-your-time.i18n :refer [t add-tr]]
            [drop-your-time.misc :as misc]
            [drop-your-time.models.poll :as poll]))

(add-tr :en
        ::title "Describe your poll"
        ::form-author-label "Your Name"
        ::form-author-help "Your name will be shown to participants"
        ::form-title-label "Poll Title"
        ::form-title-help "Describe the purpose of the poll"
        ::timezone-label "Timezone"
        ::timezone-help "The timezone of the poll"
        ::timeslot-duration-label "Time Slot Duration"
        ::timeslot-duration-help "input the duration of all time slots in minutes"
        ::form-password-label "Password (optional)"
        ::form-password-help "Only if you need to edit your selected time slots."
        ::next-step-btn "Next step")

(s/def ::not-blank-string
  (s/and string?
         (complement str/blank?)))

(s/def ::timeslot-duration
  (s/and number? #(<= 15 % 120)))

(s/def :poll/title ::not-blank-string)
(s/def :poll/author ::not-blank-string)

(s/def ::poll
  (s/keys :req-un [:poll/title :poll/author]))

(defn bool-to-cls [b]
  (if b :is-valid :is-invalid))

(defn valid-cls [spec x]
  (when (some? spec)
    (bool-to-cls (s/valid? spec x))))

(defn text-input [{:keys [id label spec value set-value help]}]
  (let [help-id (str (ns-name id) "-help")]
    [:div.mt-3
     [:label.form-label {:for id} label]
     [:input.form-control {:type :text
                           :id id
                           :class (valid-cls spec value)
                           :value value
                           :on-change #(set-value (-> % .-target .-value))
                           :aria-describedby help-id}]
     (when (some? help)
       [:div.form-text {:id help-id} help])]))

(defn slot-duration-input [{:keys [id]}]
  (let [help-id (str (ns-name :slot-duration-input) "-help")
        value (poll/slot-duration)]
    [:div.mt-3
     [:label.form-label {:for id} (t ::timeslot-duration-label)]
     [:div.input-group
      [:input.form-control {:type :number :id id
                            :aria-describedby help-id
                            :class (valid-cls ::timeslot-duration value)
                            :value value
                            :on-change #(poll/set-slot-duration! (-> % .-target .-value misc/min-to-milli))}]
      (for [min [30 45 60 90 120] :let [milli (misc/min-to-milli min)]]
        ^{:key min}
        [:button.btn.btn-outline-secondary
         {:type :button :on-click #(poll/set-slot-duration! milli)} min])]
     ;; [:div.form-text {:id help-id} (t ::timeslot-duration-help)]
     ]))

(defn timezone-input [{:keys [id]}]
  (let [help-id (str (ns-name :timezone-input) "-help")]
    [:div.mt-3
     [:label.form-label {:for id} (t ::timezone-label)]
     [:select.form-select {:id :tz-select
                           :aria-describedby help-id
                           :defaultValue (poll/poll-timezone)
                           :on-change #(poll/set-poll-timezone! (-> % .-target .-value))}
      (for [{:keys [label name]} misc/timezones]
        ^{:key label}
        [:option {:value name} label])]
     ;; [:div.form-text {:id help-id} (t ::timezone-help)]
     ]))

(defn screen []
  [:div.container
   [general/header (t ::title)]

   [:form
    [slot-duration-input {:id :slot-duration-input}]

    [timezone-input {:id :timezone-input}]]

   [text-input {:id :title-input
                :label (t ::form-title-label)
                :spec ::not-blank-string
                :value (poll/title)
                :set-value poll/set-title!
                :help (t ::form-title-help)}]

   [:div.row
    [:div.col-6
     [text-input {:id :author-input
                  :label (t ::form-author-label)
                  :spec ::not-blank-string
                  :value (poll/author)
                  :set-value poll/set-author!
                  :help (t ::form-author-help)}]]
    ;; [:div.col-6
    ;;  [text-input {:id :password-input
    ;;               :label (t ::form-password-label)
    ;;               :spec ::not-blank-string
    ;;               :value (poll/author)
    ;;               :set-value poll/set-author!
    ;;               :help (t ::form-password-help)}]]
    ]

   [:div.mt-3
    [general/screen-wide-btn
     {:on-click #(accountant/navigate! "/new-poll-time-slots")
      :disabled (not (s/valid? ::poll @poll/*poll))}
     (t ::next-step-btn)]]

   (general/debug @poll/*poll)])
