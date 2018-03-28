(ns client.edb
  (:require [entitydb.core :as edb])
  (:require-macros [keechma.toolbox.edb :refer [defentitydb]]))

(def edb-schema
  {:states {:id :short}
   :cities {:id :name}
   :restaurants {:id :slug}
   :orders {:id :_id}})

(defentitydb edb-schema)
