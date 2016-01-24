(ns client.component-system
  (:require [client.components.app :as app]
            [client.components.landing :as landing]
            [client.components.restaurant-list :as restaurant-list]
            [client.components.restaurants :as restaurants]
            [client.components.cities :as cities]
            [client.components.states :as states]
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
    :restaurants restaurants/component
    :restaurant-list (-> restaurant-list/component
                         (assoc :topic :restaurants))}))
