(ns client.app
  (:require [client.ui :refer [ui]]
            [client.subscriptions :refer [subscriptions]]
            [client.controllers :refer [controllers]]))

(def definition {:routes [[":page" {:page "home"}]
                          ":page/:slug"
                          ":page/:slug/:action"]
                 :controllers controllers
                 :html-element (.getElementById js/document "app")
                 :components ui
                 :subscriptions subscriptions})
