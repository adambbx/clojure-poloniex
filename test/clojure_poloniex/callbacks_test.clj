(ns clojure-poloniex.callbacks-test
  (:use [clojure.test]
        [clojure-poloniex.callbacks]
        [clojure-poloniex.handlers])
  (:import (clojure_poloniex.callbacks SyncCallback)))

(deftest test-sync-callback-protocol
  (let [callback (SyncCallback. identity identity identity)]
    (is (= "test-on-success" ((:on-success callback) "test-on-success")))
    (is (= "test-on-failure" ((:on-failure callback) "test-on-failure")))
    (is (= "test-on-exception" ((:on-exception callback) "test-on-exception")))))

(deftest test-get-default-callbacks
  (let [default-callbacks (get-default-callbacks)]
    (is (instance? SyncCallback default-callbacks))
    (is (= get-response (:on-success default-callbacks)))
    (is (= throw-error (:on-failure default-callbacks)))
    (is (= throw-exception (:on-exception default-callbacks)))))