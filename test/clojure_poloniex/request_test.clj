(ns clojure-poloniex.request-test
  (:use [clojure.test]
        [clojure-poloniex.request])
  (:import (com.ning.http.client AsyncHttpClient RequestBuilderBase$RequestImpl)
           (clojure_poloniex.callbacks SyncCallback)))

(deftest test-get-params-type
  (is (= (:query (get-params-type :get))))
  (is (= (:body (get-params-type :post)))))

(deftest to-urlencoded-string-test
  (is (= "currencyPair=BTC_XMR&period=300"
         (to-urlencoded-string
           {:currencyPair "BTC_XMR"
            :period       300})))
  (is (= "currencyPair=BTC%3F_XMR%23&period=300"
         (to-urlencoded-string
           {:currencyPair "BTC?_XMR#"
            :period       300}))))

(deftest get-signature-test
  (is (= "ce154b8f371f1a7ee96cf66d7da0a8ebd2addc7e2412e9805bbfdcd2a7b4e2a1aab7c06ffee5de8369a02b65ca3d01aac74ecfac1eacb9ad96bebbb20e573a82"
         (get-signature "test-secret" {:currency "BTC" :amount 0.123}))))

(deftest test-get-nonce-if-creds-are-provided
  (is (integer? (get-nonce-if-creds-are-provided))))

(deftest test-get-currency-pair
  (is (= {:currency-pair "all"} (get-currency-pair {:currency-pair "all"})))
  (is (= {:currency-pair "BTC_LTC"} (get-currency-pair {:currency-pair ["BTC" "LTC"]})))
  )

(deftest test-get-api-params
  (is (= {:currencyPair "BTC_XMR"
          :amount       300
          :command      "returnTicker"}
         (get-api-params {:params  {:currency-pair ["BTC" "XMR"]
                                    :amount        300}
                          :command "return-ticker"})))
  (is (= {:currency "BTC"
          :command  "returnTicker"}
         (get-api-params {:params  {:currency "BTC"}
                          :command "return-ticker"}))))


(deftest test-prepare-api-params
  (is (= {:currencyPair "BTC_XMR"
          :command      "returnTicker"}
         (prepare-api-params {:params  {:currency-pair "BTC_XMR"}
                              :command "return-ticker"})))
  (let [api-params (prepare-api-params {:params  {:currency-pair "BTC_XMR"}
                                        :command "return-ticker"
                                        :creds   {:key "test-key" :secret "test-secret"}})]
    (is (and (contains? api-params :nonce) (integer? (:nonce api-params))))
    (is (= {:command      "returnTicker"
            :currencyPair "BTC_XMR"} (dissoc api-params :nonce)))))

(deftest test-get-processed-creds
  (is (= {:Sign "e957b9a5e48c5f68e86470147a00de9cae35ec5be1bf0646b12121d28fcb27cd544d3bd332028d658e74b17e9a39f392c9e3cb2e5a39367157d894b34a65c100"
          :Key  "test-key"}
         (get-processed-creds {:query {:currencyPair "BTC_XMR"}}
                              {:key "test-key" :secret "test-secret"}))))

(deftest test-get-creds-headers
  (is (= nil (get-creds-headers {:currencyPair "BTC_XMR"} nil)))
  (is (= {:Sign "7ca5e97168ea0e4fff09525d5610f9e4f08a2a2275ced3d634f7c4585b05fa1e4b2a08cd1ac35b307f3ccc1b93e392af20d68033c79338219cd23b9a3ab42c10"
          :Key  "test-key"}
         (get-creds-headers {:currencyPair "BTC_XMR"}
                            {:key "test-key" :secret "test-secret"}))))

(deftest test-prepare-headers
  (is (= nil (prepare-headers {:query {:currencyPair "BTC_XMR"}} {})))
  (is (= {:headers {:Sign              "e957b9a5e48c5f68e86470147a00de9cae35ec5be1bf0646b12121d28fcb27cd544d3bd332028d658e74b17e9a39f392c9e3cb2e5a39367157d894b34a65c100"
                    :Key               "test-key"
                    :some-header-param "some-header-value"}}
         (prepare-headers {:query {:currencyPair "BTC_XMR"}}
                          {:headers {:some-header-param "some-header-value"}
                           :creds   {:key "test-key" :secret "test-secret"}}))))

(deftest test-prepare-http-options
  (is (= {} (prepare-http-options nil)))
  (is (= {:cookies {:name "test-name" :domain "test-domain"}
          :proxy   {:host "test-proxy" :protocol "https"}
          :auth    {:type :digest :user "test-user" :password "test-password"}
          :timeout 100}
         (prepare-http-options {:cookies {:name "test-name" :domain "test-domain"}
                                :proxy   {:host "test-proxy" :protocol "https"}
                                :auth    {:type :digest :user "test-user" :password "test-password"}
                                :timeout 100}))))

(deftest test-get-client
  (is (= (instance? AsyncHttpClient (get-client)))))


(deftest test-get-callbacks
  (let [test-callback #(println "test")
        default-callbacks (get-callbacks {})
        callbacks (get-callbacks {:callbacks (SyncCallback. test-callback test-callback test-callback)})]
    (is (instance? SyncCallback default-callbacks))
    (is (not (= default-callbacks callbacks)))))

(deftest test-get-request-params
  (testing "Testing priority of body or query over api params"
    (is (= {:query {:currencyPair "BTC_XMR"
                    :some-param   "test-value"
                    :command      "returnTicker"}}
           (get-request-params
             :get
             {:params  {:currency-pair ["BTC" "XMR"]}
              :command "return-ticker"
              :query   {:some-param "test-value" :currencyPair "BTC_XMR"}}))))
  (testing "Testing preparing and merging api params with query"
    (is (= {:query {:currencyPair "BTC_ETC"
                    :some-param   "test-value"
                    :command      "returnTicker"}}
           (get-request-params
             :get
             {:params  {:currency-pair "BTC_ETC"}
              :command "return-ticker"
              :query   {:some-param "test-value"}}))))
  (testing "Testing preparing and merging api params with body while ignoring query params"
    (is (= {:body {:currencyPair    "BTC_ETC"
                   :some-body-param "hello world"
                   :command         "returnTicker"}}
           (get-request-params
             :post
             {:params  {:currency-pair "BTC_ETC"}
              :body    {:some-body-param "hello world"}
              :command "returnTicker"
              :query   {:some-param "test-value"}}))))
  (testing "Testing priority of body or query over api params"
    (is (= {:body {:currencyPair "XMR_ETH"
                   :command      "returnTicker"}}
           (get-request-params
             :post
             {:params  {:currency-pair "BTC_ETC"}
              :command "return-ticker"
              :body    {:currencyPair "XMR_ETH"}}))))
  (testing "Testing preparing headers with api params with creds"
    (let [headers (:headers (get-request-params
                              :post
                              {:params  {:currency-pair "BTC_ETC"}
                               :command "return-ticker"
                               :creds   {:key "test-key" :secret "test-secret"}
                               :headers {:some-header-param "some-header-value"}}))
          sign (get headers :Sign)]
      (is (= (dissoc headers :Sign) {:Key "test-key" :some-header-param "some-header-value"}))
      (is (and (string? sign) (re-matches #"[a-z0-9]{128}" sign)))))
  (testing "Testing preparing the rest of http request options"
    (is (= {:cookies {:name "test-name" :domain "test-domain"}
            :proxy   {:host "test-proxy" :protocol "https"}
            :auth    {:type :digest :user "test-user" :password "test-password"}
            :timeout 100
            :query   {:command "returnTicker"}}
           (get-request-params
             :get
             {:command "return-ticker"
              :cookies {:name "test-name" :domain "test-domain"}
              :proxy   {:host "test-proxy" :protocol "https"}
              :auth    {:type :digest :user "test-user" :password "test-password"}
              :timeout 100})))))

(deftest test-get-request
  (testing "Testing GET request"
    (let [request (get-request "https://poloniex.com"
                               :get
                               {:params  {:currency-pair ["BTC" "ETC"]}
                                :command "return-ticker"})]
      (is (instance? RequestBuilderBase$RequestImpl request))
      (is (= "https://poloniex.com?currencyPair=BTC_ETC&command=returnTicker" (.getUrl request)))))
  (testing "Testing POST request"
    (let [request (get-request "https://poloniex.com"
                               :post
                               {:params  {:currency-pair ["BTC" "ETC"]}
                                :command "return-ticker"})
          form-params (.getFormParams request)]
      (is (instance? RequestBuilderBase$RequestImpl request))
      (is (= "https://poloniex.com" (.getUrl request)))
      (is (= 2 (count form-params)))
      (is (= "currencyPair" (.getName (nth form-params 0))))
      (is (= "BTC_ETC" (.getValue (nth form-params 0))))
      (is (= "command" (.getName (nth form-params 1))))
      (is (= "returnTicker" (.getValue (nth form-params 1)))))))