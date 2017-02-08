(ns clojure-poloniex.ws-client-test
  (:use [clojure.test]
        [clojure-poloniex.ws-client]
        [clojure-poloniex.assertions])
  (:import (ws.wamp.jawampa WampClient WampRouterBuilder)
           (java.net URI)
           (rx.schedulers Schedulers)
           (ws.wamp.jawampa.transport.netty SimpleWampWebsocketListener)
           (rx.functions Action0 Action1)
           (java.util.concurrent TimeUnit ScheduledThreadPoolExecutor)
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

(defn get-test-wamp-server []
  (let [router (get-test-router)
        server-uri (URI/create test-wsurl)]
    (SimpleWampWebsocketListener. router server-uri nil)))

(deftest test-build-client
  (let [wsurl "wss://notexistingwsurl.com"
        ws-client (build-client {:wsurl wsurl :realm test-realm})]
    (is (instance? WampClient ws-client))
    (is (= test-realm (.realm ws-client)))
    (is (= (URI. wsurl) (.routerUri ws-client)))))

(defrecord TestPublishEvent [ws-client]
  Action0
  (call [this] (.publish ws-client test-feed (object-array ["hello world!" 23.2 "abc" 33]))))

(defn start-publishing [publishing-client]
  (let [initial-delay 100
        period 500
        worker (.createWorker (Schedulers/computation))
        event (TestPublishEvent. publishing-client)]
    (.schedulePeriodically worker event initial-delay period (TimeUnit/MILLISECONDS))))

(defn publish-test-events [publishing-client]
  (let [subscription (start-publishing publishing-client)]
    (Thread/sleep 3000)
    (.unsubscribe subscription)))

(defrecord TestPublishAction [message]
  Action1
  (call [this var] (prn message)))

(defrecord TestPublishErrorAction []
  Action1
  (call [this err] (prn err)))

(defn open-test-session [server poloniex-ws-client publishing-client]
  (do (.start server)
      (open poloniex-ws-client)
      (.open publishing-client)))

(defn close-test-session [server poloniex-ws-client publishing-client]
  (do (.close publishing-client)
      (close poloniex-ws-client)
      (.stop server)))

(defn get-test-arg-map []
  (let [callbacks (WampCallback. #(swap! tmp-db conj (.arguments %)) #(prn %))]
    (hash-map :wsurl test-wsurl :realm test-realm :feed test-feed :callbacks callbacks)))

(deftest test-get-poloniex-ws-client
  (let [server (get-test-wamp-server)
        arg-map (get-test-arg-map)
        poloniex-ws-client (get-poloniex-ws-client arg-map)
        publishing-client (build-client arg-map)]
    (open-test-session server poloniex-ws-client publishing-client)
    (publish-test-events publishing-client)
    (close-test-session server poloniex-ws-client publishing-client)
    (is-db-not-empty @tmp-db)))