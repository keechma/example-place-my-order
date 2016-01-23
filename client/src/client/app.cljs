(ns client.app
  (:require [client.component-system :refer [system]]
            [client.controllers.restaurants :as c-restaurants]))

(def definition {:routes [[":page" {:page "home"}]
                          ":page/:slug"]
                 :controllers {:restaurants (c-restaurants/->Controller)}
                 :html-element (.getElementById js/document "app")
                 :components system})
