(ns client.core
  (:require [client.app :as app]
            [ashiba.app-state :as app-state]))

(enable-console-print!)

(defonce running-app (clojure.core/atom))

(defn start-app! []
  (reset! running-app (app-state/start! app/definition)))

(defn restart-app! []
  (let [current @running-app]
    (if current
      (app-state/stop! current start-app!)
      (start-app!))))
 
(restart-app!)

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)
