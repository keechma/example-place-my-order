(ns client.util)

(defn unpack-req [req]
  [(:success req) (:body req)])
