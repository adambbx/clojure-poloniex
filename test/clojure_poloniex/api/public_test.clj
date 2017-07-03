(ns clojure-poloniex.api.public-test
  (:use [clojure.test]
        [clojure-poloniex.assertions]
        [clojure-poloniex.api.public]))

(deftest test-return-ticker
  (is-response-valid (return-ticker)))

(deftest test-return-order-book
  (is-response-valid (return-order-book :params {:currency-pair "BTC_LTC"})))

(deftest test-return-trade-history
  (is-response-valid (return-trade-history :params {:currency-pair "BTC_LTC"})))

(deftest test-return-chart-data
  (is-response-valid (return-chart-data :params {:currency-pair "BTC_ETC"
                                                 :period        300
                                                 :start         (quot (System/currentTimeMillis) 1000)})))

(deftest test-return-currencies
  (is-response-valid (return-currencies)))

(deftest test-return-loan-orders
  (is-response-valid (return-loan-orders :params {:currency "BTC"})))