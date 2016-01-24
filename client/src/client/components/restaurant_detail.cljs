(ns client.components.restaurant-detail
  (:require [ashiba.ui-component :as ui]
            [client.components.restaurant-shared :refer [render-address render-hours]]))

(defn render-header [r]
  [:div.restaurant-header
   {:style {:background-image (str "url(/" (get-in r [:images :banner]) ")")}}
   [:div.background
    [:h2 (:name r)]
    (render-address (:address r))
    (render-hours)
    [:br]]])

(defn render-content [ctx r current-route]
  [:div.restaurant-content
   [:h3 "The best food this side of the Mississippi"]
   [:p.description
    [:img {:src (get-in r [:images :owner])}]
    (str "Description for " (:name r))]
   [:p.order-link
    [:a.btn {:href (ui/url ctx (merge current-route {:action "order"}))}
     (str "Order from " (:name r))]]])

(defn render [ctx]
  (let [current-route-sub (ui/current-route ctx)
        current-restaurant-sub (ui/subscription ctx :current-restaurant)]
    (fn []
      (let [r @current-restaurant-sub
            r-meta (meta r)
            current-route (:data @current-route-sub)]
        (if (:is-loading? r-meta)
          [:div.loading]
          [:div
           (render-header r)
           (render-content ctx r current-route)])))))

(def component (ui/constructor
                {:renderer render
                 :subscription-deps [:current-restaurant]}))
