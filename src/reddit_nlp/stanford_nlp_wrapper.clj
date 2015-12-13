(ns reddit-nlp.stanford-nlp-wrapper
  (:import (java.util Properties)
           (edu.stanford.nlp.pipeline StanfordCoreNLP Annotation)
           (edu.stanford.nlp.util CoreMap)
           (edu.stanford.nlp.trees Tree)
           (edu.stanford.nlp.ling CoreAnnotations$SentencesAnnotation)
           (edu.stanford.nlp.sentiment SentimentCoreAnnotations$SentimentAnnotatedTree)
           (edu.stanford.nlp.neural.rnn RNNCoreAnnotations)))

(def annotators "tokenize, ssplit, parse, sentiment")

(def stanford-nlp-pipeline (StanfordCoreNLP.
                            (doto (Properties.)
                              (.setProperty "annotators" annotators))))

(defn- annotate
  [^String text]
  (let [document (Annotation. text)]
    (.annotate stanford-nlp-pipeline document)
    document))

(defn analyze
  [^String text]
  (for [sentence (.get (annotate text) CoreAnnotations$SentencesAnnotation)]
    (let [tree (.get sentence SentimentCoreAnnotations$SentimentAnnotatedTree)]
      {:text (.toString sentence)
       :score (Integer. (RNNCoreAnnotations/getPredictedClass tree))})))
