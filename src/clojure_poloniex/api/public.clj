(ns clojure-poloniex.api.public
  (:use [clojure-poloniex.core]))

(def ^:dynamic *public-api* (create-api-context "https" "poloniex.com" "public"))

(defmacro define-public-poloniex-method
  [command default-http-method & rest]
  `(define-poloniex-method ~command
                           ~default-http-method
                           :api *public-api*
                           ~@rest))

(define-public-poloniex-method "return-ticker" :get
                               :doc "Returns the ticker for all markets")
(define-public-poloniex-method "return-24-volume" :get
                               :doc "Returns the 24-hour volume for all markets,
                                      plus totals for primary currencies")

(define-public-poloniex-method "return-order-book" :get
                               :doc "Returns the order book for a given market,
                                      as well as a sequence number for use with the Push API
                                      and an indicator specifying whether the market is frozen.
                                      You may set currencyPair to \"all\"to get the order books of all markets")

(define-public-poloniex-method "return-trade-history" :get
                               :doc "Returns the past 200 trades for a given market,
                                     or up to 50,000 trades between a range
                                     specified in UNIX timestamps by the \"start\" and \"end\" GET parameters.")

(define-public-poloniex-method "return-chart-data" :get
                               :doc "Returns candlestick chart data.
                                      Required GET parameters are:
                                      \"currencyPair\",
                                      \"period\"
                                      (candlestick period in seconds;
                                      valid values are 300, 900, 1800, 7200, 14400, and 86400),
                                      \"start\", and \"end\".
                                      \"Start\" and \"end\" are given in UNIX timestamp format
                                      and used to specify the date range for the data returned")

(define-public-poloniex-method "return-currencies" :get
                               :doc "Returns information about currencies")

(define-public-poloniex-method "return-loan-orders" :get
                               :doc "Returns the list of loan offers and demands for a given currency,
                                      specified by the \"currency\" GET parameter")