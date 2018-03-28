(ns client.controllers
  (:require [keechma.toolbox.dataloader.controller :as dataloader]
            [client.edb :refer [edb-schema]]
            [client.datasources :refer [datasources]]
            [client.controllers.order :as order]
            [client.controllers.order-history :as order-history]
            [client.controllers.vacuum :as vacuum]))

(def controllers (-> {:vacuum vacuum/controller
                      :order order/controller
                      :order-history (order-history/->Controller)}
                     (dataloader/register datasources edb-schema)))
