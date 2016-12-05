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

(defrecord SubscribeAction
  [callable ws-client feed scheduler callbacks]
  Action1
  (call [this state]
    (callable ws-client feed scheduler state callbacks)))

(defn get-default-callbacks []
  (SyncCallback. get-response
                 throw-error
                 throw-exception))