(ns client.components.order
  (:require [keechma.ui-component :as ui]
            [keechma.toolbox.ui :refer [sub>]]))

(defn render [ctx]
  (let [current-order (sub> ctx :current-order)
        component (if current-order :order-report :order-form)]
    [(ui/component ctx component)]))

(def component (ui/constructor
                {:component-deps [:order-form :order-report]
                 :subscription-deps [:current-order]
                 :renderer render}))
