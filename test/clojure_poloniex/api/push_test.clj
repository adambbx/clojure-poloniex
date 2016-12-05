(ns clojure-poloniex.api.push-test
  (:use [clojure.test]
        [clojure-poloniex.ws-client]
        [clojure-poloniex.api.push]
        [clojure-poloniex.assertions])
  (:import (ws.wamp.jawampa WampClient WampRouterBuilder)
           (java.net URI)
           (rx.schedulers Schedulers)
           (ws.wamp.jawampa.transport.netty SimpleWampWebsocketListener)
           (rx.functions Action0)
           (java.util.concurrent TimeUnit)
           (clojure_poloniex.callbacks WampCallback)))

(def tmp-db (atom []))
(def test-wsurl "ws://0.0.0.0:8080/ws1")
(def test-realm "realm1")
(def test-feed "test-feed")

(defn get-test-router []
  (let [router-builder (WampRouterBuilder.)]
    (-> router-builder
        (.addRealm test-realm)
        (.build))))

(defn get-test-wamp-server [router]
  (let [server-uri (URI/create test-wsurl)]
    (SimpleWampWebsocketListener. router server-uri nil)))

(defrecord TestPublishEvent [ws-client]
  Action0
  (call [this] (.publish ws-client test-feed (object-array ["hello worl!" 23.2 "abc" 33]))))

(defn start-publishing [publishing-client]
  (let [initial-delay 2
        period 2
        worker (.createWorker (Schedulers/computation))
        event (TestPublishEvent. publishing-client)]
    (.schedulePeriodically worker event initial-delay period (TimeUnit/SECONDS))))

(defn close-test-session [server router poloniex-ws-client publishing-client]
  (.close publishing-client)
  (close poloniex-ws-client)
  (.close router)
  (.stop server))

(defn open-test-session [server poloniex-ws-client publishing-client]
  (.start server)
  (.open publishing-client)
  (open poloniex-ws-client))

(deftest test-build-client
  (let [wsurl "wss://notexistingwsurl.com"
        ws-client (build-client wsurl test-realm)]
    (is (instance? WampClient ws-client))
    (is (= test-realm (.realm ws-client)))
    (is (= (URI. wsurl) (.routerUri ws-client)))))

(defn publish-test-events [publishing-client millis]
  (let [worker (start-publishing publishing-client)]
    (Thread/sleep (.toMillis TimeUnit/SECONDS millis))
    (.unsubscribe worker)))

(deftest test-get-poloniex-ws-client
  (let [callbacks (WampCallback. #(swap! tmp-db conj (.arguments %)) #(prn %))
        router (get-test-router)
        server (get-test-wamp-server router)
        poloniex-ws-client (get-poloniex-ws-client
                             {:feed      test-feed
                              :wsurl     test-wsurl
                              :realm     test-realm
                              :scheduler (Schedulers/computation)
                              :callbacks callbacks})
        publishing-client (build-client test-wsurl test-realm)]
    (is (satisfies? IPoloniexClient poloniex-ws-client))
    (open-test-session server poloniex-ws-client publishing-client)
    (publish-test-events publishing-client 10)
    (close-test-session server router poloniex-ws-client publishing-client)
    (is-db-not-empty @tmp-db)))