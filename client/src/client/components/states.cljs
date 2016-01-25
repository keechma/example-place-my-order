(ns client.components.states
  (:require [keechma.ui-component :as ui]))

(defn render-state [state]
  [:option {:value (:short state) :key (str "state-" (:short state))} (:name state)])

(defn render [ctx]
  (let [states-sub (ui/subscription ctx :states)]
    (fn []
      (let [states @states-sub
            states-meta (meta states)
            select-state #(ui/send-command ctx :select-state (.. % -target -value))]
        [:div.form-group
         [:label "State"]
         [:select {:disabled (:is-loading? states-meta) :on-change select-state}
          (if (:is-loading? states-meta)
            [:option "Loading"]
            (map render-state (into [{:name "Select State" :short ""}] states)))]]))))

(def component (ui/constructor
                {:subscription-deps [:states]
                 :renderer render}))
