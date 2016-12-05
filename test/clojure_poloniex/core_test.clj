(ns clojure-poloniex.core-test
  (:use [clojure.test]
        [clojure-poloniex.core])
  (:import (clojure_poloniex.core ApiContext)))

(deftest test-create-url
  (let [api-context (ApiContext. "https" "poloniex.com" "public")]
    (is (= "https://poloniex.com/public"
           (create-uri api-context)))))



