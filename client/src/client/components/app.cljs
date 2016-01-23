(ns client.components.app
  (:require [ashiba.ui-component :as ui]))

(defn active-class [route-data page]
  (when (= page (:page route-data))
    {:class "active"}))

(defn header [ctx current-route]
  (let [active-class (partial active-class current-route)]
    [:div.pmo-header
     [:header
      [:nav
       [:h1 "place-my-order.com"]
       [:ul
        [:li (active-class "home")
         [:a {:href (ui/url ctx {:page "home"})} "Home"]]
        [:li (active-class "restaurants")
         [:a {:href (ui/url ctx {:page "restaurants"})} "Restaurants"]]
        [:li (active-class "order-history")
         [:a {:href (ui/url ctx {:page "order-history"})} "Order History"]]]]]]))

(defn render [ctx]
  (fn []
    (let [current-route (:data @(ui/current-route ctx))
          page (:page current-route)]
      [:div
       [header ctx current-route]
       (case page
         "home" [(ui/component ctx :landing)]
         "restaurants" [(ui/component ctx :restaurants)]
         [:h1 "404 Not found"])])))

(def component (ui/constructor
                {:renderer render
                 :component-deps [:landing :restaurants]}))
