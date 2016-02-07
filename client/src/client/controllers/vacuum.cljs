(ns client.controllers.vacuum
  (:require [keechma.controller :as controller]
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
    (.log js/console "Total entities in EDB" total-entities)))

(defrecord Controller []
  controller/IController
  ;; This controller is always running (on any route)
  (params [_ route] true)
  (handler [_ app-db-atom in-chan out-chan]
    (go (loop []
          (let [[command args] (<! in-chan)]
            ;; When the route changes, each running controller receives
            ;; the `route-changed` command.
            ;;
            ;; When we receive this command, we vacuum the database. This will
            ;; remove any data that is stored inside the entity-db, and which is
            ;; not referenced by any collection or named item.
            ;;
            ;; This ensures that the entity-db always holds only the data it needs
            ;; to render the current page. Otherwise you could create a subtle memory
            ;; leak where your entity-db holds more and more data as user goes from
            ;; page to page.
            (when (= command :route-changed)
              (do
                (reset! app-db-atom (edb/vacuum @app-db-atom))
                (log-entity-db-count @app-db-atom)))
            (when command (recur)))))))
