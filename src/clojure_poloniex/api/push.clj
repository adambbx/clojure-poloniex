(ns clojure-poloniex.api.push
  (:use
    [clojure-poloniex.utils]
    [clojure-poloniex.ws-client]))

(def ^:dynamic *wsurl* "wss://api.poloniex.com")
(def ^:dynamic *realm* "realm1")

(defmacro define-push-api-method
  [feed & rest]
  (let [rest-map (apply sorted-map rest)
        fname (symbol feed)
        default-args (hash-map :wsurl *wsurl* :realm *realm*)
        formatted-feed (format-feed-name feed)]
    `(defn ~fname
       {:doc (get ~rest-map :doc)}
       [& {:as args#}]
       (let [arg-map# (merge {:feed ~formatted-feed} ~default-args ~rest-map args#)]
         (open (get-poloniex-ws-client arg-map#))))))

(define-push-api-method "trollbox")

(define-push-api-method "ticker")