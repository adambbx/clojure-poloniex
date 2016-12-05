# Unofficial Clojure Poloniex API wrapper

## Motivation

I wanted to create this wrapper because I enjoy programming in Clojure.
You can freely use this wrapper, but bare in mind that it is not bullet-proof.
I thought that I would use this wrapper to scrape some data for machine learning projects.

Besides, I wanted to:
* improve my skills in Clojure
* have better understading of a WAMP protocol
* eventually find some time to do something open-source
* have a good time!

## Usage

Add this to your project.clj:

```
[poloniex-api-wrapper "0.0.1"]
```

Simply require an API (public, trading, push)
For methods with parameters use, e.g. :params {:currency-pair "BTC_LTC"}

For trading methods, it is necessary to add :creds {:key "Your API key" :secret "Your secret key"}

For push methods, it is important to run them as scheduled job for a certain amount of time.
Supply your own callbacks for the push methods using WampCallback defrecord.

To get order book and trade updates for desired currency pair,
define an API method, as follows:

```clojure
(define-push-api-method "btc-xmr"
                        :scheduler (Schedulers/computation)
                        :wsurl *wsurl*
                        :realm *realm*)
```

## Examples

### Public API
```clojure
(return-ticker)

(return-chart-data :params {:currency-pair "BTC_LTC" :period 300})
```

### Trading API
```clojure
(return-balances :creds {:key "Your API key" :secret "Your API secret"})

(return-open-orders :creds {:key "Your API key" :secret "Your API secret"}
                    :params {:currencyPair "BTC_LTC"})
```

### Push API

```clojure
; Using Push API is a bit more complicated;
; Get data from a Push method you need to schedule running this method for a certain amount of time.

(ns poloniex-api.examples.wamp
  (:use
    [poloniex-api.callbacks]
    [poloniex-api.ws-client])
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

(run :push-method (btc-ltc) :callbacks (WampCallback. #(prn %) #(prn %))
                            :timeout-in-secs 7)
```

## Test

```
$ lein test
```