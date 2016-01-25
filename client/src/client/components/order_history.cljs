(ns client.components.order-history
  (:require [keechma.ui-component :as ui]))

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

(defn render-order-list [ctx status items]
  [:div {:key (str "order-" status)}
   [:h4 (get order-titles status)]
   (if (empty? items)
     [:div.order.empty (get order-empty-messages status)]
     (map (ui/component ctx :order-list-item) items))])

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
                 :component-deps [:order-list-item]
                 :subscription-deps [:order-history]}))
