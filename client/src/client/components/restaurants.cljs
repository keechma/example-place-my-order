(ns client.components.restaurants
  (:require [ashiba.ui-component :as ui]
            [client.components.restaurant-list :refer [render-address]]))

(defn render-detail [r ctx current-route]
  (let [r-meta (meta r)]
    (if (:is-loading? r-meta)
      [:div.loading]
      [:div
       [:div.restaurant-header
        {:style {:background-image (str "url(/" (get-in r [:images :banner]) ")")}}
        [:div.background
         [:h2 (:name r)]
         (render-address (:address r))
         [:div.hours-price 
          "$$$" [:br] "Hours: M-F 10am-11pm" [:span.open-now "Open Now"]]
         [:br]]]
       [:div.restaurant-content
        [:h3 "The best food this side of the Mississippi"]
        [:p.description
         [:img {:src (get-in r [:images :owner])}]
         (str "Description for " (:name r))]
        [:p.order-link
         [:a.btn {:href (ui/url ctx (merge current-route {:action "order"}))}
          (str "Order from " (:name r))]]]])))

(defn render [ctx]
  (let [current-route (ui/current-route ctx)
        current-restaurant (ui/subscription ctx :current-restaurant)]
    (fn []
      (let [slug (get-in @current-route [:data :slug])]
        [:div
         (if (nil? slug)
           [(ui/component ctx :restaurant-list)]
           (render-detail @current-restaurant ctx (:data @current-route)))]))))

(def component (ui/constructor
                {:renderer render
                 :component-deps [:restaurant-list]
                 :subscription-deps [:current-restaurant]}))
