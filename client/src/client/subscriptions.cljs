(ns client.subscriptions
  (:require [client.edb :as edb])
  (:require-macros [reagent.ratom :refer [reaction]]))

(defn states [app-db]
  (reaction
   (edb/get-collection @app-db :states :list)))

(defn cities [app-db]
  (reaction
   (edb/get-collection @app-db :cities :list)))

(defn restaurants [app-db]
  (reaction
   (edb/get-collection @app-db :restaurants :list)))

(def all {:states states
          :cities cities
          :restaurants restaurants})
