(ns reddit-nlp.duckduckgo
  (:require [cheshire.core :as cheshire]
            [org.httpkit.client :as http]))

(def ddgo-base-url "http://api.duckduckgo.com/")

(defn encode-request
  [named-entities]
  (reduce #(str %1 "+" %2) (map :text named-entities)))

(defn instant-answer
  [raw-request]
  (-> (str ddgo-base-url "?q=" (encode-request raw-request) "&format=json")
      http/get
      deref
      :body
      slurp
      (cheshire/parse-string true)))
