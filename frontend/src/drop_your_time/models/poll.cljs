(ns drop-your-time.models.poll
  (:require ["moment" :as moment]
            [clojure.spec.alpha :as s]
            [clojure.string :as str]
            [drop-your-time.i18n :refer [t add-tr]]
            [drop-your-time.misc :as misc]
            [reagent.core :as r]))

(def *poll (let [duration (misc/min-to-milli 60)]
             (r/atom {:duration duration
                      :title "New Poll"
                      :poll-timezone "Europe/Moscow"
                      :user-timezone (misc/user-timezone)
                      :author "Anonymous"
                      :slots [{:start (- (. js/Date now) (* 24 duration))}
                              {:start (. js/Date now)}
                              {:start (+ (. js/Date now) duration)}
                              {:start (+ (. js/Date now) (* 24 duration))}]
                      :votes {}}
                     ;; :validator (fn [{:keys [slots]}]
                     ;;              (or (empty? slots)
                     ;;                  (->> slots (map :start) (apply <=))))
                     )))

(s/def ::not-blank-string
  (s/and string?
         (complement str/blank?)))

(defn title [] (:title @*poll))
(defn set-title! [v] (swap! *poll assoc :title v))

(defn author [] (:author @*poll))
(defn set-author! [v] (swap! *poll assoc :author v))

(defn slot-duration [] (misc/milli-to-min (:duration @*poll)))
(defn set-slot-duration! [v] (swap! *poll assoc :duration v))
(s/def ::timeslot-duration
  (s/and number? #(<= 15 % 120)))

(defn poll-timezone [] (:poll-timezone @*poll))
(defn set-poll-timezone! [v] (swap! *poll assoc :poll-timezone v))

(s/def :poll/title ::not-blank-string)
(s/def :poll/author ::not-blank-string)
(s/def ::only-header
  (s/keys :req-un [:poll/title :poll/author]))

(defn slots-by-days []
  (->> @*poll
       :slots
       (group-by #(-> % :start misc/day-title))))

(add-tr :en
        ::from "from"
        ::to "to")

(defn slot-interval [{:keys [start]}]
  (let [{:keys [duration user-timezone poll-timezone]} @*poll

        hour-min (fn [dt zone] (.. (moment dt) (tz zone) (format "LT")))
        interval (fn [zone] (str (t ::from) " " (hour-min start zone)
                                 " " (t ::to) " " (hour-min (+ start duration) zone)))

        local (interval user-timezone)
        poll (interval poll-timezone)]
    (if (= local poll)
      [local]
      [local (str poll " (" poll-timezone ")")])))

(defn delete-me-handler! [idx]
  (fn []
    (swap! *poll update-in [:slots]
           (fn [slots] (into (subvec slots 0 idx) (subvec slots (inc idx)))))))

(defn move-slot-handler! [idx]
  (fn [offset]
    (swap! *poll update-in [:slots idx :start]
           + (misc/min-to-milli offset))))

(defn set-hhmm-handler! [idx]
  (fn [utime hhmm]
    (swap! *poll update-in [:slots idx :start]
           (constantly (let [[hour min] (str/split hhmm #":")
                             dt (js/Date. utime)]
                         (.setHours dt (js/parseInt hour))
                         (.setMinutes dt (js/parseInt min))
                         (.getTime dt))))))

(defn with-handlers [poll]
  (update poll :slots
          (fn [slots]
            (map-indexed (fn [idx slot]
                           (assoc slot :handlers
                                  {:delete-me (delete-me-handler! idx)
                                   :move-me (move-slot-handler! idx)
                                   :set-hhmm (set-hhmm-handler! idx)}))
                         slots))))

(defn initial-votes! []
  (swap! *poll (fn [{:keys [author slots] :as poll}]
                 (assoc-in poll [:votes author]
                           {:slots (->> slots (map :start) (into []))}))))
