(ns client.controllers.order-history
  (:require [ashiba.controller :as controller]
            [client.edb :as edb]
            [cljs-http.client :as http]
            [cljs.core.async :as async :refer [<!]]
            [client.util :refer [unpack-req]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(defn make-req [status]
  (http/get "/api/orders" {:query-params {:status status}}))

(defn load-order-history [app-db-atom]
  (go
    (let [reqs [(<! (make-req "new"))
                (<! (make-req "preparing"))
                (<! (make-req "delivery"))
                (<! (make-req "delivered"))]
          success (every? true? (map :success reqs))]
      (when success
        (let [data (into [] (apply concat (map #(get-in % [:body :data]) reqs)))]
          (reset! app-db-atom
                  (edb/insert-collection @app-db-atom :orders :history data)))))))

(defn mark-order [app-db-atom [order new-status]]
  (go
    (let [id (:_id order)
          marked-order (merge order {:status new-status})
          [success body] (unpack-req (<! (http/put (str "/api/orders/" id)
                                                   {:json-params marked-order})))]
      (when success
        (reset! app-db-atom
                (edb/insert-item @app-db-atom :orders body))))))

(defn delete-order [app-db-atom order]
  (go
    (let [id (:_id order)
          [success body] (unpack-req (<! (http/delete (str "/api/orders/" id))))]
      (if success
        (reset! app-db-atom
                (edb/remove-item @app-db-atom :orders id))))))

(defrecord Controller []
  controller/IController
  (params [_ route]
    (when (= (get-in route [:data :page]) "order-history")
      true))
  (start [this params app-db]
    (controller/execute this :load-order-history)
    app-db)
  (handler [this app-db-atom in-chan out-chan]
    (go (loop []
          (let [[command args] (<! in-chan)]
            (case command
              :load-order-history (load-order-history app-db-atom)
              :mark-order (mark-order app-db-atom args)
              :delete-order (delete-order app-db-atom args)
              nil)
            (when command (recur))))))
  (stop [this params app-db]
    (edb/remove-collection app-db :orders :history)))
