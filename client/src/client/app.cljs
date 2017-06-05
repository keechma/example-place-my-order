(ns client.app
  (:require [client.ui :refer [ui]]
            [client.subscriptions :refer [subscriptions]]
            [client.controllers :refer [controllers]]
            [client.controllers.restaurants :as c-restaurants]
            [client.controllers.order :as c-order]
            [client.controllers.vacuum :as c-vacuum]
            [client.controllers.order-history :as c-order-history]))

(def definition {:routes [[":page" {:page "home"}]
                          ":page/:slug"
                          ":page/:slug/:action"]
                 :controllers controllers
                 :html-element (.getElementById js/document "app")
                 :components ui
                 :subscriptions subscriptions})
