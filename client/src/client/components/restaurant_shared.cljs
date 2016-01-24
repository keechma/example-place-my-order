(ns client.components.restaurant-shared)

(defn render-address [address]
  [:div.address
   (:street address)
   [:br]
   (str (:city address) ", " (:state address) " " (:zip address))])

(defn render-hours []
  [:div.hours-price
    "$$$"
    [:br]
    "Hours: M-F 10am-11pm"
    [:span.open-now "Open Now"]])
