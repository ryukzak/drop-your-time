(ns drop-your-time.misc
  (:require ["moment" :as moment]
            ["moment-timezone"]
            [drop-your-time.i18n :refer [t add-tr]]))

(add-tr :en
        ::today "(Today)")

(defn min-to-milli [min]
  (* min 60 1000))

(defn milli-to-min [milli]
  (/ milli 60 1000))

(defn user-time-zone []
  (.. js/Intl DateTimeFormat resolvedOptions -timeZone))

(defn lead-zero [x]
  (. (str (Math/abs x)) (padStart 2 "0")))

(defn utime-to-hhmm [utime]
  (let [dt (js/Date. utime)]
    (str (lead-zero (.getHours dt)) ":" (lead-zero (.getMinutes dt)))))

(defn nice-tz [name]
  (let [tz (.. moment -tz (zone name))
        offset (. tz utcOffset (new js/Date.))
        offset-hour (js/Math.floor (/ offset 60))
        offset-min (mod offset 60)
        name (. tz -name)
        abbr (. tz (abbr (new js/Date.)))
        utc (str "UTC"
                 (if (> offset 0) "-" "+") (lead-zero offset-hour)
                 (if (> offset-min 0)
                   (str ":" offset-min)
                   ""))]
    {:name name
     :offset offset
     :abbr abbr
     :gmt utc
     :label (str name " (" abbr " - " utc ")")
     :population (. tz -population)}))

(defonce timezones
  (->> (.. moment -tz names)
       (map nice-tz)
       (filter #(-> % :population (> 100000)))
       ;; (group-by :abbr)
       ;; (map (fn [[_abbr tzs]] (first (sort-by :population tzs))))
       (sort-by :offset)
       reverse))

(defonce name-tz-to-label
  (into {} (map (fn [tz] [(-> tz :name) (-> tz :label)]) timezones)))

(defn local-date [unix-milli]
  (-> (new js/Date. unix-milli)
      (.toLocaleDateString)))

(defn user-timezone []
  (.. js/Intl DateTimeFormat resolvedOptions -timeZone))

(defn today [] (let [dt (new js/Date.)]
                 (. dt setHours 0)
                 (. dt setMinutes 0)
                 (. dt setSeconds 0)
                 (. dt setMilliseconds 0)
                 (. dt getTime)))

(defn day-title [dt]
  (str (.. (moment dt) (format "ddd, ll"))
       " " (when (= (today) dt) (t ::today))))
