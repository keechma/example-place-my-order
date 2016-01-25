(ns client.controllers.restaurants
  (:require [keechma.controller :as controller]
            [client.edb :as edb]
            [cljs-http.client :as http]
            [cljs.core.async :as async :refer [<!]]
            [client.util :refer [unpack-req]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(defn load-collection [entity list app-db-atom req]
  (do
    (reset! app-db-atom
            (edb/insert-collection @app-db-atom entity list [] {:is-loading? true}))
    (go
      (let [[is-success? body] (unpack-req (<! req))
            meta {:is-loading? false}
            data (if is-success? (:data body) [])]
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
        (reset! app-db-atom (-> app-db
                                (assoc-in [:kv :current-state] new-state)
                                (assoc [:kv] (dissoc (:kv app-db) :current-city))
                                (edb/remove-collection :restaurants :list)
                                (edb/remove-collection :cities :list)))
        (when-not (empty? new-state)
          (load-cities app-db-atom
                       (http/get "/cities" {:query-params {:state new-state}})))))))

(defn select-city [app-db-atom new-city]
  (let [app-db @app-db-atom
        current-city (get-in app-db [:kv :current-city])
        current-state (get-in app-db [:kv :current-state])]
    (when-not (= current-city new-city)
      (do
        (reset! app-db-atom (-> app-db
                                (assoc-in [:kv :current-city] new-city)
                                (edb/remove-collection :restaurants :list)))
        (when-not (empty? new-city)
          (load-restaurants app-db-atom
                            (http/get "/restaurants"
                                      {:query-params {"address.city" new-city
                                                      "address.state" current-state}})))))))

(defrecord Controller []
  controller/IController
  (params [_ route]
    (let [route-data (:data route)]
      (when (and (= (:page route-data) "restaurants")
                 (nil? (:slug route-data)))
        true)))
  (start [this params app-db]
    (controller/execute this :load-states)
    app-db)
  (handler [_ app-db-atom in-chan out-chan]
    (go (loop []
          (let [[command args] (<! in-chan)]
            (case command
              :load-states (load-states app-db-atom (http/get "/states"))
              :select-state (select-state app-db-atom args)
              :select-city (select-city app-db-atom args)
              nil)
            (when command (recur))))))
  (stop [_ _ app-db]
    (let [kv (-> (:kv app-db)
                 (dissoc :current-city)
                 (dissoc :current-state))]
      (-> app-db
          (assoc :kv kv)
          (edb/remove-collection :states :list)
          (edb/remove-collection :cities :list)
          (edb/remove-collection :restaurants :list)))))

