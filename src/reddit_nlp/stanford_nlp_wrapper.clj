(ns reddit-nlp.stanford-nlp-wrapper
  (:import (java.util Properties)
           (edu.stanford.nlp.pipeline StanfordCoreNLP Annotation)
           (edu.stanford.nlp.util CoreMap)
           (edu.stanford.nlp.trees Tree)
           (edu.stanford.nlp.ling CoreAnnotations$SentencesAnnotation
                                  CoreAnnotations$TokensAnnotation
                                  CoreAnnotations$TextAnnotation
                                  CoreAnnotations$PartOfSpeechAnnotation
                                  CoreAnnotations$NamedEntityTagAnnotation)
           (edu.stanford.nlp.sentiment SentimentCoreAnnotations$SentimentAnnotatedTree)
           (edu.stanford.nlp.neural.rnn RNNCoreAnnotations)))

(def annotators "tokenize, ssplit, pos, lemma, ner, parse, sentiment")

(def stanford-nlp-pipeline (StanfordCoreNLP.
                            (doto (Properties.)
                              (.setProperty "annotators" annotators))))

(defn- annotate
  [^String text]
  (let [document (Annotation. text)]
    (.annotate stanford-nlp-pipeline document)
    document))

(defn- sentiment-description
  [score]
  ((keyword (str score)) {:0 "Very negative"
                          :1 "Negative"
                          :2 "Neutral"
                          :3 "Positive"
                          :4 "Very negative"}))

(defn- annotate-tokens
  [^CoreMap sentence]
  (for [token (.get sentence CoreAnnotations$TokensAnnotation)]
    {:text (.get token CoreAnnotations$TextAnnotation)
     :pos (.get token CoreAnnotations$PartOfSpeechAnnotation)
     :ne (.get token CoreAnnotations$NamedEntityTagAnnotation)}))

(defn analyze
  [^String text]
  (for [sentence (.get (annotate text) CoreAnnotations$SentencesAnnotation)]
    (let [tree (.get sentence SentimentCoreAnnotations$SentimentAnnotatedTree)]
      {:text (.toString sentence)
       :tokens (annotate-tokens sentence)
       :sentiment (sentiment-description (Integer. (RNNCoreAnnotations/getPredictedClass tree)))})))

(defn- analyze-for-each
  [^String text symb]
  (for [sentence (analyze text)]
    (symb sentence)))

(defn analyze-tokens
  [^String text]
  (analyze-for-each text :tokens))

(defn analyze-sentiment
  [^String text]
  (analyze-for-each text :sentiment))

(defn named-entities
  [entities]
  (remove nil?
          (for [token entities]
            (if (not= (:ne token) "O") token))))

(defn- conj-entity
  [entities entity]
  (let [last-entity (last entities)
        last-entity-index (dec (count entities))]
    (if (= (:ne entity)
           (:ne last-entity))
      (assoc entities last-entity-index
             {:ne (:ne last-entity)
              :pos (str (:pos last-entity) "+" (:pos entity))
              :text (str (:text last-entity) "+" (:text entity))})
      (conj entities entity))))

(defn group-entities
  [named-entities]
  (reduce conj-entity [] named-entities))
