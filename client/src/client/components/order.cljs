(ns client.components.order
  (:require [keechma.ui-component :as ui]))

(defn render [ctx]
  (let [current-order-sub (ui/subscription ctx :current-order)]
    (fn []
      (let [current-order @current-order-sub
            component (if current-order :order-report :order-form)]
        [(ui/component ctx component)]))))

(def component (ui/constructor
                {:component-deps [:order-form :order-report]
                 :subscription-deps [:current-order]
                 :renderer render}))
