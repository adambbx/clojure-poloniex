(ns clojure-poloniex.request
  (:use [clojure-poloniex.callbacks]
        [clojure-poloniex.handlers]
        [clojure-poloniex.utils])
  (:require [http.async.client.request :as request]
            [http.async.client :as http]
            [clojure.string :as str]
            [pandect.algo.sha512 :as sign]
            [ring.util.codec :as codec]))

(defn get-params-type [http-method]
  (let [params-types {:post :body
                      :put  :body}]
    (get params-types http-method :query)))

(defn get-nonce-if-creds-are-provided []
  (System/currentTimeMillis))

(defn get-api-method-name [^String command]
  (let [tokens (str/split command #"-")
        first-token (first tokens)
        rest-tokens (rest tokens)]
    (str/join "" (cons first-token (capitalize-coll-items rest-tokens)))))

(defn format-currency-pair [currency-pair]
  (str/join "_" (take 2 (flatten [currency-pair]))))

(defn get-currency-pair [{:keys [currency-pair]}]
  (when currency-pair
    (hash-map :currency-pair (format-currency-pair currency-pair))))

(defn get-api-params [{:keys [command params]}]
  (let [api-method (get-api-method-name command)
        currency-pair-as-map (get-currency-pair params)
        api-params (merge params currency-pair-as-map {:command api-method})]
    (transform-map to-camelcase api-params)))

(defn prepare-api-params [{:keys [params creds command] :as arg-map}]
  (let [api-params (get-api-params arg-map)
        nonce-map (when (not-empty creds) {:nonce (get-nonce-if-creds-are-provided)})]
    (merge api-params nonce-map)))

(defn encode-api-param [[k v]]
  (let [key (name k)
        val (codec/url-encode v)]
    (str key "=" val)))

(defn to-urlencoded-string [api-params]
  (let [encoded-form-params
        (map #(encode-api-param %) api-params)]
    (str/join "&" encoded-form-params)))

(defn get-signature [secret api-params]
  (let [url-encoded-api-params (to-urlencoded-string api-params)]
    (sign/sha512-hmac url-encoded-api-params secret)))

(defn get-processed-creds [api-params creds]
  (let [signature (get-signature (:secret creds) api-params)]
    {:Key (:key creds) :Sign signature}))

(defn should-add-creds-headers [creds]
  (and (not (nil? creds)) (every? creds [:key :secret])))

(defn get-creds-headers [api-params creds]
  (when (should-add-creds-headers creds)
    (get-processed-creds api-params creds)))

(defn prepare-headers [api-params {:keys [headers creds]}]
  (let [creds-headers (get-creds-headers api-params creds)
        merged-headers (merge creds-headers headers)]
    (when (not-empty merged-headers)
      (hash-map :headers merged-headers))))

(defn get-query-or-body [http-method api-params arg-map]
  (let [params-type (get-params-type http-method)
        query-or-body (merge api-params (get arg-map params-type))]
    (when (not-empty query-or-body) (hash-map params-type query-or-body))))

(defn prepare-http-options [arg-map]
  (select-keys arg-map [:cookies :proxy :auth :timeout]))

(defn get-request-params [http-method & [arg-map]]
  (let [api-params (prepare-api-params arg-map)
        headers (prepare-headers api-params arg-map)
        query-or-body (get-query-or-body http-method api-params arg-map)
        http-options (prepare-http-options arg-map)]
    (merge query-or-body headers http-options)))

(defn get-request [url http-method arg-map]
  (let [request-params (get-request-params http-method arg-map)
        query (get request-params :query)
        body (get request-params :body)
        headers (get request-params :headers)
        cookies (get request-params :cookies)
        auth (get request-params :auth)
        proxy (get request-params :proxy)
        timeout (get request-params :timeout)]
    (request/prepare-request http-method url
                             :query query :body body
                             :headers headers :cookies cookies
                             :auth auth :proxy proxy :timeout timeout)))

(defn get-client []
  (http/create-client :follow-redirects false
                      :request-timeout -1))

(defn get-raw-response [client request callbacks]
  (apply request/execute-request
         client
         request
         (apply concat (emit callbacks))))

(defn get-callbacks [arg-map]
  (or (:callbacks arg-map) (get-default-callbacks)))

(defn execute-request [client request callbacks]
  (let [transform (get-response-transform-function callbacks)
        raw-response (get-raw-response client request callbacks)]
    (transform raw-response)))

(defn http-request [url http-method arg-map]
  (with-open [client (or (:client arg-map) (get-client))]
    (let [request (get-request url http-method arg-map)
          callbacks (get-callbacks arg-map)]
      (execute-request client request callbacks))))