(ns clojure-poloniex.core
  (:use [clojure-poloniex.request]))

(defrecord ApiContext
  [^String protocol
   ^String host
   ^String resource])

(defn create-api-context
  [^String protocol
   ^String host
   ^String resource]
  (ApiContext. protocol host resource))

(defn create-uri
  [^ApiContext api-context]
  (str (:protocol api-context) "://"
       (:host api-context) "/"
       (:resource api-context)))

(defmacro define-poloniex-method
  [command http-method & rest]
  (let [rest-map (apply sorted-map rest)
        fname (symbol command)]
    `(defn ~fname
       {:doc (get ~rest-map :doc)}
       [& {:as args#}]
       (let [arg-map# (merge {:command ~command} ~rest-map args#)
             url# (create-uri (:api arg-map#))]
         (http-request url# ~http-method arg-map#)))))