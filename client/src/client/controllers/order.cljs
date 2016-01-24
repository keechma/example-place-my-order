(ns client.controllers.order
  (:require [ashiba.controller :as controller]
            [client.edb :as edb]
            [cljs-http.client :as http]
            [cljs.core.async :as async :refer [<!]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(defn unpack-req [req]
  [(:success req) (:body req)])

(defn remove-order [app-db-atom]
  (reset! app-db-atom
          (edb/remove-named-item @app-db-atom :orders :current)))

(defn save-order [app-db-atom order]
  (go
    (let [req (<! (http/post "/api/orders" {:json-params order}))
          [success data] (unpack-req req)]
      (reset! app-db-atom
              (edb/insert-named-item @app-db-atom :orders :current data)))))

(defrecord Controller []
  controller/IController
  (params [_ route]
    (when (= (get-in route [:data :action]) "order")
      true))
  (handler [this app-db-atom in-chan out-chan]
    (go (loop []
          (let [[command args] (<! in-chan)]
            (case command
              :save-order (save-order app-db-atom args)
              :clear-order (remove-order app-db-atom)
              nil)
            (when command (recur))))))
  (stop [_ _ app-db]
    (edb/remove-named-item app-db :orders :current)))
