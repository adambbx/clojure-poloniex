(ns clojure-poloniex.utils
  (:require [clojure.string :as str]))

(defn format-feed-name [feed]
  (if (str/includes? feed "-")
    (-> feed (str/upper-case) (str/replace "-" "_"))
    feed))

(defn transform-map [transform-function input-map]
  (let [list-of-tuples (map transform-function input-map)]
    (into {} list-of-tuples)))

(defn camelcase [key]
  (let [tokens (str/split key #"-")
        capitalized-tokens (mapv str/capitalize (rest tokens))]
    (str/join (cons (first tokens) capitalized-tokens))))

(defn to-camelcase [[k v]]
  (let [key (name k)
        camelcased-key (keyword (camelcase key))]
    (vector camelcased-key v)))

(defn capitalize-key [[k v]]
  (let [key (name k)
        capitalized-key (keyword (str/capitalize key))]
    (vector capitalized-key v)))

(defn capitalize-coll-items [coll]
  (map #(str/capitalize %1) coll))

(defn join-currencies [currencyA currencyB]
  (if (not (string? currencyB))
    currencyA
    (str currencyA "_" currencyB)))
