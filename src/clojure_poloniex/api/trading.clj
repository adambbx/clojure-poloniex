(ns clojure-poloniex.api.trading
  (:use [clojure-poloniex.core]))

(def ^:dynamic *trading-api* (create-api-context "https" "poloniex.com" "tradingApi"))

(defmacro define-trading-poloniex-method
  [command default-http-method & rest]
  `(define-poloniex-method ~command
                           ~default-http-method
                           :api *trading-api*
                           ~@rest))

(define-trading-poloniex-method "return-balances" :post
                                :doc "Returns all of your available balances
                                       You need to supply :creds with :key and :secret")

(define-trading-poloniex-method "return-complete-balances" :post
                                :doc "Returns all of your balances, including available balance, balance on orders,
                                        and the estimated BTC value of your balance.
                                        By default, this call is limited to your exchange account;
                                        set the \"account\" POST parameter to \"all\" to include
                                        your margin and lending accounts.
                                        You need to supply :creds with :key and :secret")

(define-trading-poloniex-method "generate-new-address" :post
                                :doc "Generates a new deposit address for the currency
                                        specified by the \"currency\" POST parameter.
                                        You need to supply :creds with :key and :secret")

(define-trading-poloniex-method "return-deposits-withdrawals" :post
                                :doc "Returns your deposit and withdrawal history within a range,
                                        specified by the \"start\" and \"end\" POST parameters,
                                        both of which should be given as UNIX timestamps.
                                        You need to supply :creds with :key and :secret\"")

(define-trading-poloniex-method "return-open-orders" :post
                                :doc "Returns your open orders for a given market,
                                        specified by the \"currencyPair\" POST parameter, e.g. \"BTC_XCP\".
                                        Set \"currencyPair\" to \"all\" to return open orders for all markets.
                                        You need to supply :creds with :key and :secret\"")

(define-trading-poloniex-method "return-trade-history" :post
                                :doc "Returns your trade history for a given market,
                                        specified by the \"currencyPair\" POST parameter.
                                        You may specify \"all\" as the currencyPair
                                        to receive your trade history for all markets.
                                        You may optionally specify a range via \"start\" and/or \"end\" POST parameters,
                                        given in UNIX timestamp format; if you do not specify a range,
                                        it will be limited to one day.
                                        You need to supply :creds with :key and :secret\"")

(define-trading-poloniex-method "return-order-trades" :post
                                :doc "Returns all trades involving a given order,
                                        specified by the \"orderNumber\" POST parameter.
                                        If no trades for the order have occurred or you specify an order
                                        that does not belong to you, you will receive an error.
                                        You need to supply :creds with :key and :secret\"")

(define-trading-poloniex-method "return-active-loans" :post
                                :doc "Returns your active loans for each currency.
                                        You need to supply :creds with :key and :secret\"")

(define-trading-poloniex-method "return-open-loan-offers" :post
                                :doc "Returns your open loan offers for each currency.
                                        You need to supply :creds with :key and :secret\"")

(define-trading-poloniex-method "return-lending-history" :post
                                :doc "Returns your lending history within a time range,
                                        specified by the \"start\" and \"end\" POST parameter as UNIX timestamps.
                                        You may specify \"limit\" to limit the number of rows returned.
                                        You need to supply :creds with :key and :secret\"")

(define-trading-poloniex-method "return-tradable-balances" :post
                                :doc "Returns your current tradable balances for each currency
                                        in each market for which margin trading is enabled.
                                        Please note that these balances may vary continually with market conditions.
                                        You need to supply :creds with :key and :secret\"")

(define-trading-poloniex-method "return-margin-account-summary" :post
                                :doc "Returns a summary of your entire margin account.
                                        This is the same information you will find
                                        in the Margin Account section of the Margin Trading page,
                                        under the Markets list.
                                        You need to supply :creds with :key and :secret\"")

(define-trading-poloniex-method "get-margin-position" :post
                                :doc "Returns information about your margin position in a given market,
                                        specified by the \"currencyPair\" POST parameter.
                                        You may set \"currencyPair\" to \"all\"
                                        if you wish to fetch all of your margin positions at once.
                                        If you have no margin position in the specified market,
                                        \"type\" will be set to \"none\". \"liquidationPrice\" is an estimate,
                                        and does not necessarily represent the price
                                        at which an actual forced liquidation will occur.
                                        If you have no liquidation price, the value will be -1.
                                        You need to supply :creds with :key and :secret\"")


(define-trading-poloniex-method "return-available-account-balances" :post
                                :doc "Returns your balances sorted by account.
                                        You may optionally specify the \"account\" POST parameter
                                        if you wish to fetch only the balances of one account.
                                        Please note that balances in your margin account may not be accessible
                                        if you have any open margin positions or orders.
                                        You need to supply :creds with :key and :secret\"")

(define-trading-poloniex-method "return-fee-info" :post
                                :doc "If you are enrolled in the maker-taker fee schedule,
                                        returns your current trading fees and trailing 30-day volume in BTC.
                                        This information is updated once every 24 hours.
                                        You need to supply :creds with :key and :secret\"")

(define-trading-poloniex-method "buy" :post
  :doc "Places a limit buy order in a given market. Required POST parameters are \"currencyPair\", \"rate\", and \"amount\". If successful, the method will return the order number.")

(define-trading-poloniex-method "sell" :post
  :doc "Places a sell order in a given market. Required POST parameters are \"currencyPair\", \"rate\", and \"amount\". If successful, the method will return the order number.")

(define-trading-poloniex-method "cancel-order" :post
  :doc "Cancels an order you have placed in a given market. Required POST parameter is \"orderNumber\".")
