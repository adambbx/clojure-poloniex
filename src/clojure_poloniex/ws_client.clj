(ns clojure-poloniex.ws-client
  (:use [clojure-poloniex.callbacks])
  (:import (ws.wamp.jawampa WampClientBuilder
                            WampClient
                            WampError WampClient$ConnectedState)
           (ws.wamp.jawampa.transport.netty NettyWampClientConnectorProvider)
           (java.util.concurrent TimeUnit)
           (clojure_poloniex.callbacks WampCallback OnErrorAction SubscribeCallback CallableAction)
           (rx.schedulers Schedulers)))

(defprotocol IPoloniexClient
  (open [this])
  (close [this]))

(defn try-to-build-client [wsurl realm]
  (let [builder (WampClientBuilder.)
        connector-provider (NettyWampClientConnectorProvider.)]
    (-> builder
        (.withConnectorProvider connector-provider)
        (.withUri wsurl)
        (.withRealm realm)
        (.withCloseOnErrors true)
        (.withInfiniteReconnects)
        (.withReconnectInterval 5 (TimeUnit/SECONDS))
        (.build))))

(defn subscribe-feed [ws-client feed scheduler {:keys [on-next on-error]}]
  (let [subscription (.makeSubscription ws-client feed)
        on-next-action (SubscribeCallback. on-next)
        on-error-action (SubscribeCallback. on-error)
        with-scheduler (.observeOn subscription scheduler)]
    (.subscribe with-scheduler on-next-action on-error-action)
    (println (format "Feed '%s' subscription opened" feed))))

(defn on-next-callable [ws-client state {:keys [feed scheduler callbacks]
                                         :or   {callbacks (get-default-wamp-callbacks) scheduler (Schedulers/computation)}}]
  (println (format "Session status changed to: %s" state))
  (cond (instance? WampClient$ConnectedState state)
        (subscribe-feed ws-client feed scheduler callbacks)))

(defn get-on-next-action [ws-client arg-map]
  (CallableAction. on-next-callable ws-client arg-map))

(defn get-error-action [ws-client]
  (OnErrorAction. ws-client))

(defn get-scheduled-observable [observable {:keys [scheduler]}]
  (if scheduler (.observeOn observable scheduler)
                (.observeOn observable (Schedulers/computation))))

(defn build-client [{:keys [wsurl realm] :as arg-map}]
  (try
    (try-to-build-client wsurl realm)
    (catch WampError e (println "Caught exception: " (.getMessage e)))))

(defn subscribe-channel [ws-client arg-map]
  (let [observable (.statusChanged ws-client)
        scheduled-observable (get-scheduled-observable observable arg-map)
        subscribe-action (get-on-next-action ws-client arg-map)
        on-error-action (get-error-action ws-client)]
    (.subscribe scheduled-observable subscribe-action on-error-action)))

(defrecord PoloniexClient [ws-client]
  IPoloniexClient
  (open [this]
    (doto ws-client (.open)))
  (close [this]
    (.last (.toBlocking (.close ws-client)))))

(defn get-poloniex-ws-client [arg-map]
  (let [ws-client (or (:client arg-map) (build-client arg-map))
        subscription (subscribe-channel ws-client arg-map)]
    (PoloniexClient. ws-client)))