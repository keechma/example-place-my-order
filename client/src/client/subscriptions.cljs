(ns client.subscriptions
  (:require [client.edb :as edb :refer [edb-schema]]
            [keechma.toolbox.dataloader.subscriptions :refer [make-subscriptions]]
            [client.datasources :refer [datasources]])
  (:require-macros [reagent.ratom :refer [reaction]]))

(defn current-order [app-db-atom]
  (reaction
   (edb/get-named-item @app-db-atom :orders :current)))

(def subscriptions
  (merge (make-subscriptions datasources edb-schema)
         {:current-order current-order}))
