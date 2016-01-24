(ns client.app
  (:require [client.component-system :refer [system]]
            [client.controllers.restaurants :as c-restaurants]
            [client.controllers.restaurant :as c-restaurant]
            [client.controllers.order :as c-order]))

(def definition {:routes [[":page" {:page "home"}]
                          ":page/:slug"
                          ":page/:slug/:action"]
                 :controllers {:restaurants (c-restaurants/->Controller)
                               :restaurant (c-restaurant/->Controller)
                               :order (c-order/->Controller)}
                 :html-element (.getElementById js/document "app")
                 :components system})
