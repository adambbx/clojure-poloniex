(ns clojure-poloniex.ws-client
  (:use [clojure-poloniex.subscription])
  (:import (ws.wamp.jawampa WampClientBuilder
                            WampClient
                            WampError)
           (ws.wamp.jawampa.transport.netty NettyWampClientConnectorProvider)
           (java.util.concurrent TimeUnit)
           (clojure_poloniex.callbacks WampCallback SubscribeAction OnErrorAction)))

(defprotocol IPoloniexClient
  (open [this])
  (close [this]))

(defrecord PoloniexClient [ws-client]
  IPoloniexClient
  (open [this] (.open ws-client))
  (close [this] (.close ws-client)))

(defn build-client [wsurl realm]
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

(defn get-default-wamp-callbacks []
  (WampCallback. #(println (.arguments %)) #(prn %)))

(defn get-subscribe-action [ws-client {:keys [feed scheduler callbacks]}]
  (SubscribeAction. subscribe ws-client feed scheduler (or callbacks (get-default-wamp-callbacks))))

(defn get-error-action [ws-client]
  (OnErrorAction. ws-client))

(defn get-scheduled-observable [observable {:keys [scheduler]}]
  (if scheduler (.observeOn observable scheduler) observable))

(defn try-to-build-client [{:keys [wsurl realm] :as arg-map}]
  (try
    (let [^WampClient ws-client (build-client wsurl realm)
          observable (.statusChanged ws-client)
          scheduled-observable (get-scheduled-observable observable arg-map)
          subscribe-action (get-subscribe-action ws-client arg-map)
          on-error-action (get-error-action ws-client)]
      (.subscribe scheduled-observable subscribe-action on-error-action) ws-client)
    (catch WampError e (println "Caught exception: " (.getMessage e)))))

(defn get-poloniex-ws-client [arg-map]
  (let [ws-client (or (:client arg-map) (try-to-build-client arg-map))]
    (PoloniexClient. ws-client)))