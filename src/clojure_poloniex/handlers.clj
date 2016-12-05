(ns clojure-poloniex.handlers
  (:require [http.async.client :as http]
            [clojure.data.json :as json]))

(defn handle-error-response [response {:keys [on-exception]}]
  (on-exception response (http/error response)))

(defn handle-correct-response [response {:keys [on-success on-failure]}]
  (cond
    (and (< (:code (http/status response)) 400))
    (on-success response)
    (and (>= (:code (http/status response)) 400))
    (on-failure response)))

(defn handle-response
  [response callbacks]
  (if (http/failed? response)
    (handle-error-response response callbacks)
    (handle-correct-response response callbacks)))

(defn get-response-transform-function
  [callbacks]
  #(handle-response (http/await %) callbacks))

(defn get-response
  [response & {:keys [to-json?] :or {to-json? true}}]
  (let [body-trans (if to-json? json/read-json identity)]
    (hash-map :headers (http/headers response)
              :status (http/status response)
              :body (body-trans (http/string response)))))

(defn to-map [response]
  (let [headers (http/headers response)
        status (http/status response)
        body (json/read-json (http/string response))]
    (hash-map :headers headers
              :status status
              :body body)))

(defn get-exception-message [response]
  (let [status (http/status response)
        code (:code status)
        msg (:msg status)]
    (format "An error %s '%s' occurred" code msg)))

(defn throw-error
  [response]
  (throw (Exception. (get-exception-message response))))

(defn throw-exception
  [throwable]
  (throw throwable))
