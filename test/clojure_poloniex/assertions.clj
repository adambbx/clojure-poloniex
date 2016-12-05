(ns clojure-poloniex.assertions
  (:require [clojure.test :refer [is]]))

(defn is-status-ok [response]
  (is (= 200 (:code (:status response)))))

(defn is-not-error [response]
  (is (not (contains? (:body response) :error))))

(defn is-response-valid [response]
  (do (is-status-ok response)
      (is-not-error response)))

(defn is-db-not-empty [db]
  (is (< 0 (count db))))