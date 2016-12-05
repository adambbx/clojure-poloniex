(ns clojure-poloniex.subscription
  (:use [clojure-poloniex.callbacks])
  (:import (ws.wamp.jawampa WampClient$DisconnectedState)
           (clojure_poloniex.callbacks SubscribeCallback)))

(defn get-subscription [ws-client feed scheduler {:keys [on-next on-error]}]
  (let [subscription (.makeSubscription ws-client feed)
        on-next-action (SubscribeCallback. on-next)
        on-error-action (SubscribeCallback. on-error)
        with-scheduler (if scheduler (.observeOn subscription scheduler) subscription)]
    (.subscribe with-scheduler on-next-action on-error-action)))

(defn close-subscription [subscription]
  (if (not (nil? subscription))
    (do (.unsubscribe subscription)
        (println "Subscription closed"))))

(defn subscribe [ws-client feed scheduler state callbacks]
  (let [subscription (get-subscription ws-client feed scheduler callbacks)]
    (println (format "Session status changed to: %s" state))
    (cond (instance? WampClient$DisconnectedState state)
          (close-subscription subscription))))