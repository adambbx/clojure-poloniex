(ns clojure-poloniex.api.trading-test
  (:use [clojure.test]
        [clojure-poloniex.assertions]
        [clojure-poloniex.api.trading]))

; it is important to know if it is possible to get connection with poloniex trading api
; we cannot supply an API key for testing which would be necessary to test the format of responses
; but if the status is 200, it is okay
; i suggest to test actual content of response in your final app.

(deftest test-return-balances
  (is-status-ok (return-balances)))

(deftest test-return-balances
  (is-status-ok (return-open-orders)))

(deftest test-return-open-orders
  (is-status-ok (return-balances)))

(deftest test-return-deposits-withdrawals
  (is-status-ok (return-deposits-withdrawals)))

(deftest test-generate-new-address
  (is-status-ok (generate-new-address)))

(deftest test-return-complete-balances
  (is-status-ok (return-complete-balances)))

(deftest test-return-trade-history
  (is-status-ok (return-trade-history)))

(deftest test-return-active-loans
  (is-status-ok (return-active-loans)))

(deftest test-return-open-loan-offers
  (is-status-ok (return-open-loan-offers)))

(deftest test-return-tradable-balances
  (is-status-ok (return-tradable-balances)))

(deftest test-get-margin-position
  (is-status-ok (get-margin-position)))

(deftest test-return-tradable-balances
  (is-status-ok (return-tradable-balances)))

(deftest test-return-open-loan-offers
  (is-status-ok (return-open-loan-offers)))

(deftest test-return-order-trades
  (is-status-ok (return-order-trades)))

(deftest test-return-margin-account-summary
  (is-status-ok (return-margin-account-summary)))