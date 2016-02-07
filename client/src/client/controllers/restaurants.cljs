(ns client.controllers.restaurants
  (:require [keechma.controller :as controller]
            [client.edb :as edb]
            [cljs-http.client :as http]
            [cljs.core.async :as async :refer [<!]]
            [client.util :refer [unpack-req]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(defn load-collection [entity list app-db-atom req]
  (do
    ;; Before loading the new collection, we insert an empty collection that has
    ;; meta data set on it - {:is-loading? true}
    (reset! app-db-atom
            (edb/insert-collection @app-db-atom entity list [] {:is-loading? true}))
    (go
      ;; Make a request and unpack it
      (let [[is-success? body] (unpack-req (<! req))
            meta {:is-loading? false}
            data (if is-success? (:data body) [])]
        ;; Save the collection in the entity-db
        (reset! app-db-atom
                (edb/insert-collection @app-db-atom entity list data meta))))))

(def load-cities (partial load-collection :cities :list))
(def load-states (partial load-collection :states :list))
(def load-restaurants (partial load-collection :restaurants :list))

(defn select-state [app-db-atom new-state]
  (let [app-db @app-db-atom
        current-state (get-in app-db [:kv :current-state])]
    (when-not (= current-state new-state)
      (do
        ;; - replace current state selection
        ;; - remove the current city selection
        ;; - remove the current restaurants list
        ;; - remove the current cities list
        (reset! app-db-atom (-> app-db
                                (assoc-in [:kv :current-state] new-state)
                                (assoc [:kv] (dissoc (:kv app-db) :current-city))
                                (edb/remove-collection :restaurants :list)
                                (edb/remove-collection :cities :list)))
        ;; If the new state is selected call the load-cities function which will load 
        ;; the cities data and put it in the entity-db
        (when-not (empty? new-state)
          (load-cities app-db-atom
                       (http/get "/cities" {:query-params {:state new-state}})))))))

(defn select-city [app-db-atom new-city]
  (let [app-db @app-db-atom
        current-city (get-in app-db [:kv :current-city])
        current-state (get-in app-db [:kv :current-state])]
    (when-not (= current-city new-city)
      (do
        ;; - remove the current city selection
        ;; - remove the current restaurants list
        (reset! app-db-atom (-> app-db
                                (assoc-in [:kv :current-city] new-city)
                                (edb/remove-collection :restaurants :list)))
        ;; If the new city is selected call the load-restaurants function which will load
        ;; the restaurants data and put it in the entity-db
        (when-not (empty? new-city)
          (load-restaurants app-db-atom
                            (http/get "/restaurants"
                                      {:query-params {"address.city" new-city
                                                      "address.state" current-state}})))))))

(defrecord Controller []
  controller/IController
  (params [_ route]
    ;; This controller is active only when we're on the 
    ;; restaurants page, and there is no :slug defined.
    (let [route-data (:data route)]
      (when (and (= (:page route-data) "restaurants")
                 (nil? (:slug route-data)))
        true)))
  (start [this params app-db]
    ;; When the controller is started we execute the :load-states
    ;; command. The handler function is listening to this command.
    (controller/execute this :load-states)
    app-db)
  (handler [_ app-db-atom in-chan out-chan]
    ;; Handler is listening to commands on the in-chan. When the 
    ;; command comes, it calls the appropriate function
    (go (loop []
          (let [[command args] (<! in-chan)]
            (case command
              ;; Load states command is sent by the start function
              :load-states (load-states app-db-atom (http/get "/states"))
              ;; Select state command is sent from the UI (states component)
              :select-state (select-state app-db-atom args)
              ;; Select city command is sent from the UI (cities component)
              :select-city (select-city app-db-atom args)
              nil)
            (when command (recur))))))
  (stop [_ _ app-db]
    ;; When the controller is stopped (the route changed) we need to clean 
    ;; the loaded data from the app state.
    (let [kv (-> (:kv app-db)
                 (dissoc :current-city)
                 (dissoc :current-state))]
      (-> app-db
          (assoc :kv kv)
          (edb/remove-collection :states :list)
          (edb/remove-collection :cities :list)
          (edb/remove-collection :restaurants :list)))))

