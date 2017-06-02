(ns client.app
  (:require [client.component-system :refer [system]]
            [client.subscriptions :as subscriptions]
            [client.controllers.restaurants :as c-restaurants]
            [client.controllers.restaurant :as c-restaurant]
            [client.controllers.order :as c-order]
            [client.controllers.vacuum :as c-vacuum]
            [client.controllers.order-history :as c-order-history]
            [client.debug :refer [make-reporter]]))

(def has-reporter? (= "?debugger" (.-search js/location)))

(def definition {:routes [[":page" {:page "home"}]
                          ":page/:slug"
                          ":page/:slug/:action"]
                 :controllers {:restaurants (c-restaurants/->Controller)
                               :restaurant (c-restaurant/->Controller)
                               :order (c-order/->Controller)
                               :order-history (c-order-history/->Controller)
                               :vacuum (c-vacuum/->Controller)}
                 :html-element (.getElementById js/document "app")
                 :reporter (if has-reporter? (make-reporter) (fn [_]))
                 :components system
                 :subscriptions subscriptions/all})
