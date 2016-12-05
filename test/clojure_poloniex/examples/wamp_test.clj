(ns clojure-poloniex.examples.wamp-test
  (:use
    [clojure.test]
    [clojure-poloniex.examples.wamp]
    [clojure-poloniex.api.push]
    [clojure-poloniex.assertions])
  (:import (rx.schedulers Schedulers)
           (clojure_poloniex.callbacks WampCallback)))

(def tmp-db (atom []))

(define-push-api-method "btc-ltc"
                        :wsurl *wsurl*
                        :realm *realm*
                        :scheduler (Schedulers/computation)
                        :callbacks (WampCallback. #(swap! tmp-db conj (.arguments %)) #(prn %)))

(deftest test-run
  (do
    (run :push-method (btc-ltc) :timeout-in-secs 7)
    (Thread/sleep 12000)
    (is-db-not-empty @tmp-db)))
