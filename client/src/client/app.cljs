(ns client.app
  (:require [client.component-system :refer [system]]
            [client.controllers.restaurants :as c-restaurants]
            [client.controllers.restaurant :as c-restaurant]))

(def definition {:routes [[":page" {:page "home"}]
                          ":page/:slug"]
                 :controllers {:restaurants (c-restaurants/->Controller)
                               :restaurant (c-restaurant/->Controller)}
                 :html-element (.getElementById js/document "app")
                 :components system})
