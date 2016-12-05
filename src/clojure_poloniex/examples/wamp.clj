(ns clojure-poloniex.examples.wamp
  (:use
    [clojure-poloniex.callbacks]
    [clojure-poloniex.ws-client])
  (:import (java.util.concurrent Executors TimeUnit)))

(defn run [& {:keys [push-method timeout-in-secs]}]
  (let [executor (Executors/newSingleThreadExecutor)]
    (try
      (open push-method)
      (.awaitTermination executor timeout-in-secs (TimeUnit/SECONDS))
      (close push-method)
      (catch Exception e
        (println "Caught exception: " e))
      (finally (close push-method)))))