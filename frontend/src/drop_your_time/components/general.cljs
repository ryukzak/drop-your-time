(ns drop-your-time.components.general
  (:require [cljs.pprint :as pprint]
            [clojure.spec.alpha :as s]
            [drop-your-time.conf :as conf]))

(defn header
  ([] [header nil])
  ([sub-header]
   [:<>
    [:h1.display-1 "Drop Your Time (DEV)"]
    [:h1.display-5 sub-header]]))

(defn screen-wide-btn [opts & childs]
  [:div.d-grid.gap-2.mt-3
   [:button.btn.btn-primary.btn-md opts
    (for [[idx child] (map-indexed vector childs)]
      ^{:key idx} child)]])

(defn debug [& args]
  (when conf/DEBUG
    [:div
     [:hr]
     [:div.row
      (for [[key arg] (map-indexed vector args)]
        ^{:key key}
        [:div.col [:pre (with-out-str (pprint/pprint arg))]])]]))

(defn bool-to-cls [b]
  (if b :is-valid :is-invalid))

(defn valid-cls [spec x]
  (bool-to-cls (s/valid? spec x)))
