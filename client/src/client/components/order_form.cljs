(ns client.components.order-form
  (:require [keechma.ui-component :as ui]
            [reagent.core :refer [atom]]
            [client.components.order-shared :refer [menu-item-key]]
            [clojure.set :as set]
            [keechma.toolbox.ui :refer [sub>]]))

(def tabs {:lunch "Lunch menu"
           :dinner "Dinner menu"})

(def form-fields {:name "Name"
                  :address "Address"
                  :phone "Phone"})

(defn order-has-item? [order-atom item]
  (let [items (:items @order-atom)]
    (contains? items item)))

(defn toggle-order-item [order-atom item]
  (let [items (:items @order-atom)]
    (if (contains? items item)
      (swap! order-atom assoc :items (disj items item))
      (swap! order-atom assoc :items (conj items item)))))

(defn order-total [order-atom]
  (let [items (:items @order-atom)]
    (reduce #(+ %1 (:price %2)) 0 items)))

(defn order-has-items? [order-atom]
  (< 0 (count (:items @order-atom))))

(defn is-missing? [order field]
  (let [val (get order field)]
    (or (nil? val) (clojure.string/blank? val))))

(defn error-or-submit [ctx order-atom missing-atom]
  (let [order @order-atom
        fields-keys (keys form-fields)
        missing (reduce (fn [m f]
                          (if (is-missing? order f)
                            (conj m f)
                            m)) (set nil) fields-keys)]
    (reset! missing-atom [])
    (if (empty? missing)
      (ui/send-command ctx :save-order order)
      (reset! missing-atom missing))))

(defn render-form-field [order-atom missing-atom [field label]]
  (let [order @order-atom
        has-errors? (contains? @missing-atom field)]
    [:div.form-group {:key (str "form-field-" field)}
     [:label.control-label label]
     [:input.form-control
      {:value (field order)
       :on-change #(swap! order-atom assoc field (.. % -target -value))}]
     [:p.help-text {:style (when has-errors? {:display "block"})}
      (str "Please enter your " (.toLowerCase label))]]))

(defn render-info [order-atom]
  (let [item-count (count (:items @order-atom))
        has-items? (< 0 item-count)]
    [:p {:class (str "info " (if has-items? "text-success" "text-error"))}
     (if has-items?
       (str item-count " selected")
       "Please choose an item")]))

(defn render-tab-links [current-tab-atom [tab label]]
  [:li {:on-click #(reset! current-tab-atom tab)
        :class (when (= @current-tab-atom tab) "active")
        :key (str "tab-" tab)}
   [:a {:on-click #(.preventDefault %) :href "#"} label]])

(defn render-menu-item [order-atom item]
  (let [key (menu-item-key item)]
    [:li.list-group-item.checkbox {:key key}
     [:label
      [:input {:type "checkbox"
               :default-checked (order-has-item? order-atom item)
               :on-click #(toggle-order-item order-atom item)}]
      (:name item)
      " "
      [:span.badge (str "$" (:price item))]]]))

(defn render-menu-panel [r current-tab-atom order-atom]
  (let [current-tab @current-tab-atom
        items (get-in r [:menu current-tab])]
    [:ul.list-group
     (doall
      (map (partial render-menu-item order-atom) items))]))

(defn render-tabs [r current-tab-atom order-atom]
  [:div
   [:ul.nav.nav-tabs
    (doall
     (map (partial render-tab-links current-tab-atom) tabs))]
   (render-info order-atom)])

(defn render [ctx]
  (let [order-atom (atom {:status "new"
                          :items (set nil)})
        missing-atom (atom (set nil))
        current-tab-atom (atom :lunch)]
    (fn []
      (let [r (sub> ctx :current-restaurant)
            r-meta (meta r)]
        [:div.order-form
         (if (:is-loading? r-meta)
           [:div.loading]
           [:div 
            [:h3 (str "Order from " (:name r))]
            [:form {:on-submit (fn [e]
                                 (.preventDefault e)
                                 (error-or-submit ctx order-atom missing-atom))}
             (render-tabs r current-tab-atom order-atom)
             (render-menu-panel r current-tab-atom order-atom)
             (doall
              (map (partial render-form-field order-atom missing-atom)
                   form-fields))
             [:div.submit
              [:h4 (str "Total: $" (.toFixed (order-total order-atom) 2))]
              [:button.btn {:type "submit"
                            :disabled (not (order-has-items? order-atom))}
               "Place My Order!"]]]])]))))
(def component (ui/constructor
                {:renderer render
                 :subscription-deps [:current-restaurant]}))
