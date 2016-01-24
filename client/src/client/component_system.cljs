(ns client.component-system
  (:require [client.components.app :as app]
            [client.components.landing :as landing]
            [client.components.restaurant-list :as restaurant-list]
            [client.components.restaurant-detail :as restaurant-detail]
            [client.components.cities :as cities]
            [client.components.states :as states]
            [client.components.order :as order]
            [client.components.order-form :as order-form]
            [client.components.order-report :as order-report]
            [client.components.order-history :as order-history]
            [client.components.order-list-item :as order-list-item]
            [ashiba.ui-component :as ui]
            [client.subscriptions :as subscriptions]))

(defn resolve-component-subscriptions [subs component]
  (reduce (fn [c dep]
            (let [sub (get subs dep)]
              (if (nil? sub)
                (throw (js/Error (str "Missing subscription: " dep)))
                (ui/resolve-subscription-dep c dep sub))))
          component (or (:subscription-deps component) [])))

(defn resolve-subscriptions [subs components]
  (reduce-kv (fn [components k c]
               (assoc components k (resolve-component-subscriptions subs c)))
             {} components))

(def system 
  (resolve-subscriptions
   subscriptions/all
   {:main app/component
    :landing landing/component
    :cities (assoc cities/component :topic :restaurants)
    :states (assoc states/component :topic :restaurants)
    :restaurant-list (assoc restaurant-list/component :topic :restaurants)
    :restaurant-detail restaurant-detail/component
    :order order/component
    :order-report (assoc order-report/component :topic :order)
    :order-form (assoc order-form/component :topic :order)
    :order-list-item (assoc order-list-item/component :topic :order)
    :order-history order-history/component}))
