(ns client.controllers.restaurant
  (:require [keechma.controller :as controller]
            [client.edb :as edb]
            [cljs-http.client :as http]
            [cljs.core.async :as async :refer [<!]]
            [client.util :refer [unpack-req]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(defn update! [app-db-atom updater]
  (reset! app-db-atom (updater @app-db-atom)))


(defn load-restaurant [app-db-atom slug]
  ;; Before making the request for the restaurant save the empty item with 
  ;; the meta defined - {:is-loading? true}
  (update! app-db-atom
           #(edb/insert-named-item % :restaurants :current {} {:is-loading? true}))
  (go 
    ;; Load the restaurant and save it in the entity-db
    (let [req (<! (http/get (str "/restaurants/" slug)))
          meta {:is-loading? false}
          [success data] (unpack-req req)]
      (update! app-db-atom
               #(edb/insert-named-item % :restaurants :current data meta)))))

(defrecord Controller []
  controller/IController
  ;; This controller is runnign when we're on the restaurants page
  ;; and when the :slug route param is defined
  (params [this route]
    (when (= (get-in route [:data :page]) "restaurants")
      (get-in route [:data :slug])))
  ;; If the user is coming from the restaurants page, it means that we
  ;; have the restaurant already loaded, so we just save it in the etity-db.
  ;; Otherwise send the command that will load the restaurant
  (start [this slug app-db]
    (let [restaurant (edb/get-item-by-id app-db :restaurants slug)]
      ;; If we already have the restaurant save it
      (if restaurant
        (edb/insert-named-item app-db :restaurants :current restaurant)
        (do
          ;; If we don't have the restaurant load it
          (controller/execute this :load-restaurant slug)
          app-db))))
  (handler [this app-db-atom in-chan out-chan]
    (go (loop []
          (let [[command args] (<! in-chan)]
            (case command
              ;; Loads the restaurant
              :load-restaurant (load-restaurant app-db-atom args)
              nil)
            (when command (recur)))))))
