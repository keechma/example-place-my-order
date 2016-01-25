(ns client.controllers.order
  (:require [keechma.controller :as controller]
            [client.edb :as edb]
            [cljs-http.client :as http]
            [client.util :refer [unpack-req]]
            [cljs.core.async :as async :refer [<!]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(defn remove-order [app-db-atom]
  (reset! app-db-atom
          (edb/remove-named-item @app-db-atom :orders :current)))

(defn save-order [app-db-atom order]
  (go
    (let [req (<! (http/post "/orders" {:json-params order}))
          [success data] (unpack-req req)]
      (reset! app-db-atom
              (edb/insert-named-item @app-db-atom :orders :current data)))))

(defn mark-order [app-db-atom [order new-status]]
  (go
    (let [id (:_id order)
          marked-order (merge order {:status new-status})
          [success body] (unpack-req (<! (http/put (str "/orders/" id)
                                                   {:json-params marked-order})))]
      (when success
        (reset! app-db-atom
                (edb/insert-item @app-db-atom :orders body))))))

(defn delete-order [app-db-atom order]
  (go
    (let [id (:_id order)
          [success body] (unpack-req (<! (http/delete (str "/orders/" id))))]
      (if success
        (reset! app-db-atom
                (edb/remove-item @app-db-atom :orders id))))))

(defrecord Controller []
  controller/IController
  (params [_ route]
    (when (or (= (get-in route [:data :action]) "order")
              (= (get-in route [:data :page]) "order-history"))
      true))
  (handler [this app-db-atom in-chan out-chan]
    (go (loop []
          (let [[command args] (<! in-chan)]
            (case command
              :save-order (save-order app-db-atom args)
              :clear-order (remove-order app-db-atom)
              :mark-order (mark-order app-db-atom args)
              :delete-order (delete-order app-db-atom args)
              nil)
            (when command (recur))))))
  (stop [_ _ app-db]
    (edb/remove-named-item app-db :orders :current)))
