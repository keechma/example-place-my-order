(ns client.datasources
  (:require [keechma.toolbox.ajax :as ajax]
            [promesa.core :as p]
            [client.edb :refer [get-item-by-id]]))

(defn map-loader [loader]
  (fn [reqs]
    (map loader reqs)))

(defn extract-data [res _] (:data res))

(defn GET
  ([url]
   (GET url {}))
  ([url params]
   (ajax/GET url (merge {:keywords? true :response-format :json} params))))


(def datasources
  {:states             {:target    [:edb/collection :states/list]
                        :processor extract-data
                        :loader    (map-loader
                                    (fn [req]
                                      (when (:params req)
                                        (GET "/states"))))
                        :params    (fn [_ {:keys [page]} _]
                                     (when (= "restaurants" page)
                                       true))}

   :cities             {:target    [:edb/collection :cities/list]
                        :processor extract-data
                        :loader    (map-loader
                                    (fn [req]
                                      (when-let [params (:params req)]
                                        (GET "/cities" {:params params}))))
                        :params    (fn [_ {:keys [state page]} _]
                                     (when (and state (= "restaurants" page))
                                       {:state state}))}

   :restaurants        {:target    [:edb/collection :restaurants/list]
                        :processor extract-data
                        :loader    (map-loader
                                    (fn [req]
                                      (when-let [params (:params req)]
                                        (GET "/restaurants" {:params params}))))
                        :params    (fn [_ {:keys [page city state]} _]
                                     (when (and city state (= "restaurants" page))
                                       {:address.city city
                                        :address.state state}))}

   :current-restaurant {:target [:edb/named-item :restaurants/current]
                        :loader (map-loader
                                 (fn [req]
                                   (let [app-db (:app-db req)
                                         slug (:params req)]
                                     (when slug
                                       (or (get-item-by-id app-db :restaurants slug)
                                        (GET (str "/restaurants/" slug)))))))
                        :params (fn [_ {:keys [page slug]} _]
                                  (when (and slug (= "restaurants" page))
                                    slug))}

   :current-order      {:target [:edb/named-item :orders/current]
                        :loader (map-loader (fn [_]))
                        :params (fn [_ _ _])}

   :order-history      {:target [:edb/collection :orders/list]
                        :loader (map-loader (fn [_]))
                        :params (fn [_ _ _])}})
