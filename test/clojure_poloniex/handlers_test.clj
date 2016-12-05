(ns clojure-poloniex.handlers-test
  (:use [clojure.test]
        [clojure-poloniex.callbacks]
        [clojure-poloniex.handlers])
  (:require [http.async.client.request :as request]
            [http.async.client :as http]))

(defn get-test-raw-response [http-method url]
  (let [request (request/prepare-request http-method url)
        client (http/create-client)
        raw-response (request/execute-request client request)]
    (http/await raw-response)))

(deftest test-to-map
  (let [raw-response (get-test-raw-response :get "https://jsonplaceholder.typicode.com/posts/1")
        response (to-map raw-response)]
    (is (= {:code 200 :msg "OK" :protocol "HTTP/1.1" :major 1 :minor 1}
           (:status response)))
    (is (= {:userId 1 :id 1
            :title  "sunt aut facere repellat provident occaecati excepturi optio reprehenderit"
            :body   "quia et suscipit\nsuscipit recusandae consequuntur expedita et cum\nreprehenderit molestiae ut ut quas totam\nnostrum rerum est autem sunt rem eveniet architecto"}
           (:body response)))))

(deftest test-throw-error
  (let [raw-response (get-test-raw-response :get "https://jsonplaceholder.typicode.com/unknown")]
    (is (thrown-with-msg? Exception #"An error 404 'Not Found' occurred" (throw-error raw-response)))))

(deftest test-throw-exception
  (let [raw-response (get-test-raw-response :get "https://notexist.net")]
    (is (thrown? Exception (throw-exception (http/error raw-response))))))

(deftest test-get-exception-message
  (with-redefs-fn {#'http/status (fn [response] {:code 404 :msg "Not Found" :protocol "HTTP/1.1" :major 1 :minor 1})}
    #(is (= "An error 404 'Not Found' occurred"
            (get-exception-message {})))))