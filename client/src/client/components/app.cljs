(ns client.components.app
  (:require [ashiba.ui-component :as ui]
            [cljs.core.match :refer-macros [match]]))

(def !nil? (complement nil?))

(def pages {"home" "Home"
            "restaurants" "Restaurants"
            "order-history" "Order History"})

(defn active-class [route-data page]
  (when (= page (:page route-data))
    "active"))

(defn render-nav-link [ctx current-route [page label]]
  (let [active-class (partial active-class current-route)]
    [:li {:class (active-class page) :key (str "nav-" page)}
     [:a {:href (ui/url ctx {:page page})} label]]))

(defn render-header [ctx current-route]
  [:div.pmo-header
   [:header
    [:nav
     [:h1 "place-my-order.com"]
     [:ul
      (map (partial render-nav-link ctx current-route) pages)]]]])

(defn render [ctx]
  (fn []
    (let [current-route (:data @(ui/current-route ctx))
          page (:page current-route)
          slug (:slug current-route)
          action (:action current-route)
          c-landing (ui/component ctx :landing)
          c-restaurant-list (ui/component ctx :restaurant-list)
          c-order-history (ui/component ctx :order-history)
          c-restaurant-detail (ui/component ctx :restaurant-detail)
          c-order (ui/component ctx :order)]
      (when-not (nil? current-route)
        [:div
         [render-header ctx current-route]
         (match [page slug action]
                ["home" nil nil] [c-landing]
                ["restaurants" nil nil] [c-restaurant-list]
                ["order-history" nil nil] [c-order-history]
                ["restaurants" (slug :guard !nil?) nil] [c-restaurant-detail]
                ["restaurants" (slug :guard !nil?) "order"] [c-order]
                :else [:h1 "Not found"])]))))

(def component (ui/constructor
                {:renderer render
                 :component-deps [:landing
                                  :restaurant-detail
                                  :restaurant-list
                                  :order
                                  :order-history]}))
