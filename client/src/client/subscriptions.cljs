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

(defn current-restaurant [app-db]
  (reaction
   (let [slug (get-in @app-db [:route :data :slug])]
     (when slug
       (edb/get-item-by-id @app-db :restaurants slug)))))

(defn current-order [app-db]
  (reaction
   (edb/get-named-item @app-db :orders :current)))

(def all {:states states
          :cities cities
          :restaurants restaurants
          :current-restaurant current-restaurant
          :current-order current-order})
