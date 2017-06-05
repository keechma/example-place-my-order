(ns client.components.states
  (:require [keechma.ui-component :as ui]
            [keechma.toolbox.ui :refer [route> sub> <cmd]]))

(defn update-url [ctx state]
  (let [current-route (route> ctx)]
    (if (seq state)
      (ui/redirect ctx (assoc current-route :state state))
      (ui/redirect ctx (dissoc current-route :state :city)))))

(defn render-state [state]
  [:option {:value (:short state) :key (:short state)} (:name state)])

(defn render [ctx]
  (let [states (into [{:name "Select State" :short ""}] (sub> ctx :states))
        current-route (route> ctx)
        states-meta (sub> ctx :states-meta)]
    [:div.form-group
     [:label "State"]
     [:select
      {:disabled false
       :value (or (:state current-route) "")
       :on-change #(update-url ctx (.. % -target -value))}
      (if (= :pending (:status states-meta))
        [:option "Loading"]
        (doall
         (map render-state states)))]]))

(def component (ui/constructor
                {:subscription-deps [:states :states-meta]
                 :renderer render}))
