(ns reddit-nlp.duckduckgo
  (:require [cheshire.core :as cheshire]
            [org.httpkit.client :as http]))

(def ddgo-base-url "http://api.duckduckgo.com/")

(defn- encode-request
  [named-entities]
  (reduce #(str %1 "+" %2) (map :text named-entities)))

(defn- instant-answer
  [query]
  (-> (str ddgo-base-url "?q=" query "&format=json")
      http/get
      deref
      :body
      slurp
      (cheshire/parse-string true)))

(defn instant-answers
  [named-entities]
  (for [entity named-entities]
    (instant-answer (:text entity))))

(defn structured-instant-answers
  [named-entities]
  (for [el (instant-answers named-entities)]
    {:topic (:Heading el)
     :abstract (:Abstract el)
     :related (map :FirstURL (:RelatedTopics el))}))
