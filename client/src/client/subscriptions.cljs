(ns client.subscriptions
  (:require [client.edb :refer [edb-schema]]
            [keechma.toolbox.dataloader.subscriptions :refer [make-subscriptions]]
            [client.datasources :refer [datasources]])
  (:require-macros [reagent.ratom :refer [reaction]]))

(def subscriptions (make-subscriptions datasources edb-schema))
