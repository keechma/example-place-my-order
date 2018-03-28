(ns client.components.restaurant-list
  (:require [keechma.ui-component :as ui]
            [client.components.restaurant-shared :refer [render-address render-hours]]
            [keechma.toolbox.ui :refer [sub>]]))

(defn render-restaurant [ctx restaurant]
  [:div.restaurant {:key (:slug restaurant)}
   [:img {:src (get-in restaurant [:images :thumbnail])}]
   [:h3 (:name restaurant)]
   (render-address (:address restaurant))
   (render-hours)
   [:a.btn {:href (ui/url ctx {:page "restaurants" :slug (:slug restaurant)})}
    "Place My Order"]
   [:br]])

(defn render [ctx]
  (let [restaurants (sub> ctx :restaurants)
        restaurants-meta (sub> ctx :restaurants-meta)]
    [:div.restaurants
     [:h2.page-header "Restaurants"]
     [:form.form
      [(ui/component ctx :states)]
      [(ui/component ctx :cities)]]
     (if (= :pending (:status restaurants-meta))
       [:div.restaurants.loading]
       (map (partial render-restaurant ctx) restaurants))]))

(def component (ui/constructor
                {:subscription-deps [:restaurants :restaurants-meta]
                 :component-deps [:cities :states]
                 :renderer render}))
