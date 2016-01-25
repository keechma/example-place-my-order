(ns client.components.landing
  (:require [keechma.ui-component :as ui]))

(defn render [ctx]
  [:div.pmo-home
   [:div.homepage
    [:img {:src "http://place-my-order.com/node_modules/place-my-order-assets/images/homepage-hero.jpg"}]
    [:h1 "Ordering food has never been easier"]
    [:p "We make it easier than ever to order gourmet food from your favorite local restaurants"]
    [:p>a {:class "btn" :href (ui/url ctx {:page "restaurants"}) :role "button"}
     "Choose a Restaurant"]]])

(def component (ui/constructor
                {:renderer render}))
