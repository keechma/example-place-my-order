(ns client.controllers.order-history
  (:require [ashiba.controller :as controller]
            [client.edb :as edb]
            [cljs-http.client :as http]
            [cljs.core.async :as async :refer [<! put!]]
            [client.util :refer [unpack-req]]
            [clojure.set :as set])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(defn connect-socketio [listener]
  (try
    (let [conn (.io js/window)
          send (fn [command order]
                 (put! listener [command (js->clj order :keywordize-keys true)]))]
      (.connect conn #js {:forceNew true})
      (.on conn "orders created" #(send :order-created %))
      (.on conn "orders updated" #(send :order-updated %))
      (.on conn "orders removed" #(send :order-removed %))
      (fn []
        (println "Disconnecting from socket.io")
        (.disconnect conn)))
    (catch :default e
      (do
        (println "io function doesn't exist.")
        (fn [])))))

(defn make-req [status]
  (http/get "/orders" {:query-params {:status status}}))

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

(defn order-created [app-db-atom order]
  (let [current-history (edb/get-collection @app-db-atom :orders :history)
        ids (set (map :_id current-history))]
    (when-not (contains? ids (:_id order))
      (reset! app-db-atom
              (edb/insert-collection @app-db-atom :orders :history (concat [order] current-history))))))

(defn order-updated [app-db-atom order]
  (reset! app-db-atom
          (edb/insert-item @app-db-atom :orders order)))

(defn order-removed [app-db-atom order]
  (reset! app-db-atom
          (edb/remove-item @app-db-atom :orders (:_id order))))

(defrecord Controller []
  controller/IController
  (params [_ route]
    (when (= (get-in route [:data :page]) "order-history")
      true))
  (start [this params app-db]
    (controller/execute this :load-order-history)
    app-db)
  (handler [this app-db-atom in-chan out-chan]
    (let [disconnect (connect-socketio in-chan)]
      (go (loop []
            (let [[command args] (<! in-chan)]
              (case command
                :load-order-history (load-order-history app-db-atom)
                :order-created (order-created app-db-atom args)
                :order-updated (order-updated app-db-atom args)
                :order-removed (order-removed app-db-atom args)
                :disconnect (disconnect)
                nil)
              (when command (recur)))))))
  (stop [this params app-db]
    (controller/execute this :disconnect)
    (edb/remove-collection app-db :orders :history)))
