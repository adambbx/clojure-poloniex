(ns clojure-poloniex.api.push
  (:use
    [clojure-poloniex.utils]
    [clojure-poloniex.ws-client])
  (:import (rx.schedulers Schedulers)))

(def ^:dynamic *wsurl* "wss://api.poloniex.com")
(def ^:dynamic *realm* "realm1")

(defmacro define-push-api-method
  [feed & rest]
  (let [rest-map (apply sorted-map rest)
        fname (symbol feed)
        formatted-feed (format-feed-name feed)]
    `(defn ~fname
       {:doc (get ~rest-map :doc)}
       [& {:as args#}]
       (let [arg-map# (merge {:feed ~formatted-feed} ~rest-map args#)]
         (get-poloniex-ws-client arg-map#)))))

(define-push-api-method "trollbox"
                        :scheduler (Schedulers/computation)
                        :wsurl *wsurl*
                        :realm *realm*)

(define-push-api-method "ticker"
                        :scheduler (Schedulers/computation)
                        :wsurl *wsurl*
                        :realm *realm*)

(define-push-api-method "btc-xmr"
                        :scheduler (Schedulers/computation)
                        :wsurl *wsurl*
                        :realm *realm*)