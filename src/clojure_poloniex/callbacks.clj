(ns clojure-poloniex.callbacks
  (:use [clojure-poloniex.handlers])
  (:require [http.async.client.request :as req])
  (:import (rx.functions Action1)))

(defprotocol Callback (emit [this]))

(defrecord SyncCallback [on-success
                         on-failure
                         on-exception]
  Callback
  (emit [_] req/*default-callbacks*))

(defrecord WampCallback [on-next on-error]
  Callback
  (emit [this] {:on-next  on-next
                :on-error on-error}))

(defrecord SubscribeCallback [callable]
  Action1
  (call [this pub-sub] (callable pub-sub)))

(defrecord OnErrorAction [ws-client]
  Action1
  (call [this throwable] (prn (format "Session ended with error: %s" throwable))))

(defrecord CallableAction
  [callable ws-client arg-map]
  Action1
  (call [this obj]
    (callable ws-client obj arg-map)))

(defn get-default-callbacks []
  (SyncCallback. get-response
                 throw-error
                 throw-exception))

(defn get-default-wamp-callbacks []
  (WampCallback. #(println (.arguments %)) #(println %)))