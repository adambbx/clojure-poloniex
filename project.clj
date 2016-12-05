(defproject clojure-poloniex "0.1.0-SNAPSHOT"
  :description "A Clojure wrapper for Poloniex API"
  :url "https://github.com/adambober/clojure-poloniex.git"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [clj-http "2.2.0"]
                 [http.async.client "1.0.1"]
                 [pandect "0.6.0"]
                 [org.clojure/data.json "0.2.6"]
                 [ring/ring-codec "1.0.1"]
                 [ws.wamp.jawampa/jawampa-core "0.4.2"]
                 [ws.wamp.jawampa/jawampa-netty "0.4.2"]])
