(ns client.components.order-shared
  (:import goog.crypt.hash32))

(defn menu-item-key [menu-item]
  (str "menu-item" (.encodeString hash32 (:name menu-item))))
