(ns client.components.order-report
  (:require [keechma.ui-component :as ui]
            [client.components.order-shared :refer [menu-item-key]]
            [keechma.toolbox.ui :refer [sub> <cmd]]))
            
(defn render-menu-item [item]
  [:li {:key (menu-item-key item)}
   [:label
    (:name item) " "
    [:span.badge (str "$" (:price item))]]])

(defn menu-items-with-total [items]
  (let [rendered-items (map render-menu-item items)
        total (reduce #(+ %1 (:price %2)) 0 items)]
    (concat rendered-items
            [[:li.list-group-item {:key "items-total"}
              [:label "Total " [:span.badge (str "$" (.toFixed total 2))]]]])))

(defn render [ctx] 
  (let [o (sub> ctx :current-order)]
    [:div.order-form
     [:h3 (str "Thanks for your order " (:name o) "!")]
     [:div>label.control-label (str "Confirmation Number: " (:_id o))]
     [:h4 "Items ordered:"]
     [:ul.list-group.panel
      (menu-items-with-total (:items o))]
     [:div>label.control-label "Phone: " (:phone o)]
     [:div>label.control-label "Address: " (:address o)]
     [:p>button.btn.btn-link {:on-click #(<cmd ctx :clear-order)} "Place another order"]]))

(def component (ui/constructor 
                {:renderer render
                 :subscription-deps [:current-order]}))
