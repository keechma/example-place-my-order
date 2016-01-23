(ns client.components.restaurants
  (:require [ashiba.ui-component :as ui]))

(defn render-address [address]
  [:div.address
   (:street address)
   [:br]
   (str (:city address) ", " (:state address) " " (:zip address))])

(defn render-restaurant [ctx restaurant]
  [:div.restaurant {:key (:slug restaurant)}
   [:img {:src (get-in restaurant [:images :thumbnail])}]
   [:h3 (:name restaurant)]
   (render-address (:address restaurant))
   [:div.hours-price
    "$$$"
    [:br]
    "Hours: M-F 10am-11pm"
    [:span.open-now "Open Now"]]
   [:a.btn {:href (ui/url ctx {:page "restaurants" :slug (:slug restaurant)})}
    "Place My Order"]
   [:br]])

(defn render [ctx]
  (let [restaurants-sub (ui/subscription ctx :restaurants)]
    (fn []
      (let [restaurants @restaurants-sub
            restaurants-meta (meta restaurants)]
        [:div.restaurants
         [:h2.page-header "Restaurants"]
         [:form.form
          [(ui/component ctx :states)]
          [(ui/component ctx :cities)]]
         (if (:is-loading? restaurants-meta)
           [:div.restaurants.loading]
           (map (partial render-restaurant ctx) restaurants))
         ]))))

(def component (ui/constructor
                {:subscription-deps [:restaurants]
                 :component-deps [:cities :states]
                 :renderer render}))
