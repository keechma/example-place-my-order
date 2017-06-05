(ns client.controllers
  (:require [keechma.toolbox.dataloader.controller :as dataloader]
            [client.edb :refer [edb-schema]]
            [client.datasources :refer [datasources]]))

(def controllers (-> {}
                     (dataloader/register datasources edb-schema)))
