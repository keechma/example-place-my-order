(ns client.controllers.order
  (:require [client.edb :as edb]
            [promesa.core :as p]
            [keechma.toolbox.ajax :refer [POST PUT DELETE]]
            [keechma.toolbox.pipeline.core :as pp :refer-macros [pipeline!]]
            [keechma.toolbox.pipeline.controller :as pp-controller]))

(defn make-req
  ([method-fn url] (make-req method-fn url nil))
  ([method-fn url params]
   (let [default-config {:response-format :json
                         :keywords? true}]
     (method-fn url (if params (assoc default-config :params params :format :json) default-config)))))

(defn mark-order [[order new-status]]
  ;; Marks the order with the new status
  (let [id (:_id order)
        marked-order (merge order {:status new-status})]

    ;; If the request was successful, insert the order to entity db. If we already
    ;; have order with that id in the entity db, it will be updated.
    (make-req PUT (str "/orders/" id) marked-order)))

(defn delete-order [order]
  (let [id (:_id order)]
    (->> (make-req DELETE (str "/orders/" id))
         (p/map (fn [_] order)))))

;; This controller is active either on the order page or 
;; on the order-history page.
;;
;; It knows how to handle anything related to orders:
;;
;; - saving a new order
;; - clearing the current order from the app state so user can make another one
;; - marking the order (changing it's status: new -> preparing -> delivery -> delivered)
;; - deleting the order
(def controller
  (pp-controller/constructor
   {:params (fn [route]
              (when (or (= (get-in route [:data :action]) "order")
                        (= (get-in route [:data :page]) "order-history"))
                true))
    :stop   (fn [_ _ app-db]
              (edb/remove-named-item app-db :orders :current))}

   {:save-order   (pipeline! [value app-db]
                    (make-req POST "/orders/" value)
                    (pp/commit! (edb/insert-named-item app-db :orders :current value)))
    :clear-order  (pipeline! [value app-db]
                    (pp/commit! (edb/remove-named-item app-db :orders :current)))
    :mark-order   (pipeline! [value app-db]
                    (mark-order value)
                    (pp/commit! (edb/insert-item app-db :orders value)))
    :delete-order (pipeline! [value app-db]
                    (delete-order value)
                    (pp/commit! (edb/remove-item app-db :orders (:id value))))}))
