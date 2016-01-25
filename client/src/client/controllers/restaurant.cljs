(ns client.controllers.restaurant
  (:require [ashiba.controller :as controller]
            [client.edb :as edb]
            [cljs-http.client :as http]
            [cljs.core.async :as async :refer [<!]]
            [client.util :refer [unpack-req]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(defn update! [app-db-atom updater]
  (reset! app-db-atom (updater @app-db-atom)))


(defn load-restaurant [app-db-atom slug]
  (update! app-db-atom
           #(edb/insert-named-item % :restaurants :current {} {:is-loading? true}))
  (go 
    (let [req (<! (http/get (str "/restaurants/" slug)))
          meta {:is-loading? false}
          [success data] (unpack-req req)]
      (update! app-db-atom
               #(edb/insert-named-item % :restaurants :current data meta)))))

(defrecord Controller []
  controller/IController
  (params [this route]
    (when (= (get-in route [:data :page]) "restaurants")
      (get-in route [:data :slug])))
  (start [this slug app-db]
    (let [restaurant (edb/get-item-by-id app-db :restaurants slug)]
      (if restaurant
        (edb/insert-named-item app-db :restaurants :current restaurant)
        (do
          (controller/execute this :load-restaurant slug)
          app-db))))
  (handler [this app-db-atom in-chan out-chan]
    (go (loop []
          (let [[command args] (<! in-chan)]
            (case command
              :load-restaurant (load-restaurant app-db-atom args)
              nil)
            (when command (recur)))))))
