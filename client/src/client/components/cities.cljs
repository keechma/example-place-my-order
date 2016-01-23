(ns client.components.cities
  (:require [ashiba.ui-component :as ui]))

(defn render-city [city]
  [:option {:value (:name city) :key (str "city-" (:name city))} (:name city)])

(defn render [ctx]
  (let [cities-sub (ui/subscription ctx :cities)]
    (fn []
      (let [cities @cities-sub
            cities-meta (meta cities)
            select-city #(ui/send-command ctx :select-city (.. % -target -value))]
        [:div.form-group
         [:label "City"]
         [:select {:disabled (or (empty? cities) (:is-loading? cities-meta))
                   :on-change select-city}
          (if (:is-loading? cities-meta)
            [:option "Loading"]
            (map render-city (into [{:name "Choose a city"}] cities)))]]))))

(def component (ui/constructor
                {:subscription-deps [:cities]
                 :renderer render}))
