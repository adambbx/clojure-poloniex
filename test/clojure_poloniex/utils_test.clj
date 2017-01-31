(ns clojure-poloniex.utils_test
  (:use [clojure.test]
        [clojure-poloniex.utils]))

(deftest test-format-feed-name
  (is (= "BTC_XMR" (format-feed-name "btc-xmr")))
  (is (= "trollbox" (format-feed-name "trollbox"))))

(deftest test-transform-map
  (is (= {:currencyPair "BTC_XMR"}
         (transform-map to-camelcase {:currency-pair "BTC_XMR"})))
  (is (= {:Key "test-key"}
         (transform-map capitalize-key {:key "test-key"}))))

(deftest test-to-camelcase
  (is (= [:currencyPair "BTC_XMR"] (to-camelcase [:currency-pair "BTC_XMR"]))))

(deftest test-capitalize-key
  (is (= [:Key "test-key"] (capitalize-key [:key "test-key"]))))