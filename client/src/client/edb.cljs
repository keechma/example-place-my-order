(ns client.edb
  (:require [entitydb.core :as edb])
  (:require-macros [keechma.toolbox.edb :refer [defentitydb]]))

(def edb-schema
  {:states {:id :short}
   :cities {:id :name}
   :restaurants {:id :slug}
   :orders {:id :_id}})

(defentitydb edb-schema)

(def dbal (edb/make-dbal edb-schema))

(defn wrap-entity-db-get [dbal-fn]
  (fn [db & rest]
    (let [entity-db (:entity-db db)]
      (apply dbal-fn (concat [entity-db] rest)))))

(defn wrap-entity-db-mutate [dbal-fn]
  (fn [db & rest]
    (let [entity-db (:entity-db db)
          resulting-entity-db (apply dbal-fn (concat [entity-db] rest))]
      (assoc db :entity-db resulting-entity-db))))

(defn update-item-by-id [db entity-kw id data]
  (let [item (get-item-by-id db entity-kw id)]
    (insert-item db entity-kw (merge item data))))

(defn collection-empty? [collection]
  (let [collection-meta (meta collection)]
    (and (= (:state collection-meta) :completed)
         (= (count collection) 0))))
