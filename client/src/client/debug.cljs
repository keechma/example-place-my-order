(ns client.debug
  (:require [taoensso.sente :as sente]
            [cljs.core.async :refer [put! <! chan]]
            [clojure.walk :as walk]
            [cljs.pprint :as pprint]
            [clojure.string :as str])
  (:require-macros [cljs.core.async.macros :refer [go-loop]]))

(defn event-cleanup [e]
  (walk/postwalk
   (fn [part]
     (let [string-rep (pr-str part)]
       (if (str/starts-with? string-rep "#object")
         string-rep
         part))) e))

(defn make-reporter []
  (let [reporter-chan (chan)
        s (sente/make-channel-socket! "/chsk" {:type :auto :host "localhost:1337"})]
    (go-loop []
      (let [event (<! reporter-chan)]
        (when event
          ((:send-fn s) [:keechma/debugger (event-cleanup event)])
          (recur))))
    (fn [& args]
      (put! reporter-chan args))))
