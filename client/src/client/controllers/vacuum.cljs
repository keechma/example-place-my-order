(ns client.controllers.vacuum
  (:require [ashiba.controller :as controller]
            [client.edb :as edb]
            [cljs.core.async :as async :refer [<!]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(defn log-entity-db-count [db]
  (let [entity-db (:entity-db db)
        entity-keys (keys entity-db)
        total-entities (reduce
                        (fn [total entity-key]
                          (if (= entity-key :__meta-store__)
                            total
                            (let [all-keys (keys (get-in entity-db [entity-key :store]))]
                                     (+ total (count all-keys))))) 0 entity-keys)]
    (.log js/console "Total entities in EDB" total-entities)))

(defrecord Controller []
  controller/IController
  (params [_ route] true)
  (handler [_ app-db-atom in-chan out-chan]
    (go (loop []
          (let [[command args] (<! in-chan)]
            (when (= command :route-changed)
              (do
                (reset! app-db-atom (edb/vacuum @app-db-atom))
                (log-entity-db-count @app-db-atom)))
            (when command (recur)))))))
