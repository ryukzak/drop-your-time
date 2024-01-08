(ns drop-your-time.service
  (:require [cljs-http.client :as http]
            [cljs.core.async :refer [<!]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(def host-api "http://localhost:8080/api")
(def host "http://localhost:8080")

(defn submit-poll [poll callback]
  (go (let [response (<! (http/post (str host-api "/polls")
                                    {:json-params poll
                                     :with-credentials? false}))]
        (prn :submit-poll response)
      ;; TODO: handle errors
        (callback (:body response)))))

(defn submit-vote [uuid author vote callback]
  (go (let [response (<! (http/post (str host-api "/polls/" uuid)
                                    {:json-params {:author author
                                                   :vote vote}
                                     :with-credentials? false}))]
        (prn :submit-vote response)
      ;; TODO: handle errors
        (callback (:body response)))))

(defn get-poll [uuid callback]
  (go (let [response (<! (http/get (str host-api "/polls/" uuid)
                                   {:with-credentials? false}))]
        (prn :get-poll response)
      ;; TODO: handle errors
        (callback (:body response)))))
