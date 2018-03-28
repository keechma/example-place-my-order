(ns client.components.cities
  (:require [keechma.ui-component :as ui]
            [keechma.toolbox.ui :refer [route> sub> <cmd]]))

(defn update-url [ctx city]
  (let [current-route (route> ctx)]
    (if (seq city)
      (ui/redirect ctx (assoc current-route :city city))
      (ui/redirect ctx (dissoc current-route :city)))))

(defn render-city [city]
  [:option {:value (:name city) :key (:name city)} (:name city)])

(defn render [ctx]
  (let [cities (into [{:name "Choose a city" :short ""}] (sub> ctx :cities))
        current-route (route> ctx)
        cities-meta (sub> ctx :cities-meta)]
    [:div.form-group
     [:label "City"]
     [:select
      {:disabled (= 1 (count cities))
       :value (or (:city current-route) "")
       :on-change #(update-url ctx (.. % -target -value))}
      (if (= :pending (:status cities-meta))
        [:option "Loading"]
        (doall
         (map render-city cities)))]]))

(def component (ui/constructor
                {:subscription-deps [:cities :cities-meta]
                 :renderer render}))
