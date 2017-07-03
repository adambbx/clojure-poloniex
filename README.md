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

```clojure
[clojure-poloniex "0.1.0-SNAPSHOT"]
```

Simply require an API namespace (public, trading, push)
API parameters are passed with :params, for example:
```clojure
 (return-order-book :params {:currency-pair ["BTC" "LTC"]})
```

For trading methods, it is necessary to add :creds, for example:
```clojure
(return-balances :creds {:key "Your API key" :secret "Your secret key"})
```

If Push API methods are not scheduled, they would run infinitely.
See examples below, how to run a Push API method with timeout.
Some API methods are pre-defined (trollbox, ticker).
To define your own PUSH methods use:
```clojure
(define-push-api-method "btc-xmr")
```

Supply your own callbacks for the push methods using WampCallback.

```clojure
;the first argument is for the on-next event
;the second one is for the on-error
(trollbox :callbacks (WampCallback. #(println %) #(println %)))
```

To get order book and trade updates for desired currency pair, define an API method.
Default callbacks only print the received responses.

```clojure
(define-push-api-method "btc-xmr")

; The url and realm are predefined but can be overriden:
(define-push-api-method "btc-ltc" :wsurl "your url" :realm "your realm")
```
## Examples

### Public API
```clojure
(return-ticker)

(return-chart-data :params {:currency-pair ["BTC" "LTC"] :period 300})
```

### Trading API
```clojure
(return-balances :creds {:key "Your API key" :secret "Your API secret"})

(return-open-orders :creds {:key "Your API key" :secret "Your API secret"}
                    :params {:currency-pair ["BTC" "LTC"] })
```

### Push API

```clojure
; Running a push method infinitely with custom callbacks
(btc-ltc :callbacks (WampCallback. #(println %) #(println %)))

; Run a push method for a given amount of time with default callbacks.
(with-open [push-method (trollbox)]
  (Thread/sleep 10000))
```

## Test

```
$ lein test
```