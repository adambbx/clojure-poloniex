(ns clojure-poloniex.examples.wamp
  (:use
    [clojure-poloniex.api.push]))

; run a trollbox method for a 10 seconds
(with-open [push-method (trollbox)]
  (Thread/sleep 10000))