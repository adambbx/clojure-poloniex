# Unofficial Clojure Poloniex API wrapper

[![Build Status](https://travis-ci.org/adambober/clojure-poloniex.svg?branch=master)](https://travis-ci.org/adambober/clojure-poloniex)

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
[clojure-poloniex "0.1.0-SNAPSHOT"]
```

Simply require an API (public, trading, push)
For methods with parameters use :params {:currency-pair ["BTC" "LTC"]}

For trading methods, it is necessary to add :creds {:key "Your API key" :secret "Your secret key"}

For push methods, it is important to run them as scheduled job for a certain amount of time.
Supply your own callbacks for the push methods using WampCallback.

```clojure
;the first argument is for the on-next event
;the second one is for the on-error
(WampCallback. #(println %) #(println %))
```

To get order book and trade updates for desired currency pair, define an API method.
Default callbacks only print the received responses. Provide your own callbacks by passing :callbacks argument:

```clojure
(define-push-api-method "btc-xmr"
                        :scheduler (Schedulers/computation)
                        :callbacks (WampCallback. #(println %) #(println %))
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
; Using Push API is a bit more complicated.

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

; Schedule a push method for a given amount of time to collect data.
(run :push-method (btc-ltc) :timeout-in-secs 7)

; Schedule
(run :push-method (btc-ltc :callbacks (WampCallback. #(println %) #(println %))) :timeout-in-secs 7)
```

## Test

```
$ lein test
```