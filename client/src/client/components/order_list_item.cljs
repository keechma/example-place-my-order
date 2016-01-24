(ns client.components.order-list-item
  (:require [ashiba.ui-component :as ui]
            [client.components.order-shared :refer [menu-item-key]]))

(defn mark-order [ctx order status e]
  (.preventDefault e)
  (ui/send-command ctx :mark-order [order status]))

(defn delete-order [ctx order e]
  (.preventDefault e)
  (ui/send-command ctx :delete-order order))

(defn render-menu-item [item]
  [:li {:key (menu-item-key item)} (:name item)])

(defn make-order-action [badge mark-as next-status] 
  (fn [ctx order]
    [:div.actions
     [:span.badge badge]
     [:p.action
      "Mark As "
      [:a {:href "#" :on-click (partial mark-order ctx order next-status)}
       mark-as]]
     [:p.action>a {:href "#" :on-click (partial delete-order ctx order)} "Delete"]]))

(def new-action (make-order-action "New Order" "Preparing" "preparing"))
(def preparing-action (make-order-action "Preparing" "Out for delivery" "delivery"))
(def delivery-action (make-order-action "Out for delivery" "Delivered" "delivered"))

(defn delivered-action [ctx order]
  [:div.actions
   [:span.badge "Delivered"]
   [:p.action>a {:href "#" :on-click (partial delete-order ctx order)} "Delete"]])

(defn render [ctx o]
  (let [items (:items o)
        total (.toFixed (reduce #(+ %1 (:price %2)) 0 items) 2)
        status (:status o)
        ]
    [:div {:class (str "order " status) :key (:_id o)}
     [:address (:name o) [:br] (:address o) [:br] (:phone o)]
     [:div.items>ul (map render-menu-item items)]
     [:div.total (str "$" total)]
     (case status
       "new" (new-action ctx o)
       "preparing" (preparing-action ctx o)
       "delivery" (delivery-action ctx o)
       "delivered" (delivered-action ctx o))]))

(def component {:renderer render})
