(ns client.controllers.vacuum
  (:require [keechma.toolbox.pipeline.controller :as pp-controller]
            [keechma.toolbox.pipeline.core :as pp :refer-macros [pipeline!]]
            [client.edb :as edb]
            [cljs.core.async :as async :refer [<!]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(defn log-entity-db-count [db]
  ;; Log the current entity count in the entity-db. For debugging purposes.
  (let [entity-db (:entity-db db)
        entity-keys (keys entity-db)
        total-entities (reduce
                        (fn [total entity-key]
                          (if (= entity-key :__meta-store__)
                            total
                            (let [all-keys (keys (get-in entity-db [entity-key :store]))]
                              (+ total (count all-keys))))) 0 entity-keys)]
    (println "Total entities in EDB" total-entities)))

(def controller
  (pp-controller/constructor
   (constantly true)
   {:route-changed (pipeline! [value app-db]
                     (pp/commit! (edb/vacuum app-db))
                     (log-entity-db-count app-db))}))
