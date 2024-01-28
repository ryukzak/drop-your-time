(ns drop-your-time.i18n
  (:require [taoensso.tempura :as tempura :refer [tr]]))

(def *tr-options-dict
  (atom {:default-locale :en
         :dict {}}))

(defn add-tr [lang & {:as kv}]
  (swap! *tr-options-dict
         (fn [m]
           (reduce (fn [m [key text]]
                     (assoc-in m [:dict lang key] text))
                   m
                   kv))))

(defn t [key]
  (tr @*tr-options-dict
      [:em]
      [key (str "missing resource: " key)]))
