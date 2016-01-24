(ns client.components.order-history
  (:require [ashiba.ui-component :as ui]
            [client.components.order-shared :refer [menu-item-key]]))

(def order-statuses ["new" "preparing" "delivery" "delivered"])

(def order-titles {"new" "New Orders"
                   "preparing" "Preparing"
                   "delivery" "In delivery"
                   "delivered" "Delivered"})

(def order-empty-messages {"new" "No new orders"
                           "preparing" "No orders preparing"
                           "delivery" "No orders in delivery"
                           "delivered" "No delivered orders"})

(defn grouped-history [history]
  (group-by :status history))

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

(defn render-order [ctx o]
  (let [items (:items o)
        total (.toFixed (reduce #(+ %1 (:price %2)) 0 items) 2)
        status (:status o)
        ]
    [:div {:class (str "order " status) :key (:_id o)}
     [:address (:name o) [:br] (:address o) [:br] (:phone o)]
     [:div.items>ul (map render-menu-item items)]
     [:div.total total]
     (case status
       "new" (new-action ctx o)
       "preparing" (preparing-action ctx o)
       "delivery" (delivery-action ctx o)
       "delivered" (delivered-action ctx o))]))

(defn render-order-list [ctx status items]
  [:div {:key (str "order-" status)}
   [:h4 (get order-titles status)]
   (if (empty? items)
     [:div.order.empty (get order-empty-messages status)]
     (map (partial render-order ctx) items))])

(defn render [ctx]
  (let [order-history-sub (ui/subscription ctx :order-history)]
    (fn []
      (let [order-history @order-history-sub
            grouped (grouped-history order-history)]
        [:div.order-history
         [:div.order.header
          [:address "Name / Address / Phone"]
          [:div.items "Order"]
          [:div.total "Total"]
          [:div.actions "Action"]]
         (map #(render-order-list ctx % (get grouped %)) order-statuses)]))))

(def component (ui/constructor
                {:renderer render
                 :subscription-deps [:order-history]}))
