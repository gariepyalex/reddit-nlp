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

(defn- named-entities-of-analyzed-sentence
  [sentence]
  (remove nil?
          (for [token (:tokens sentence)]
            (if (not= (:ne token) "O") token))))

(defn named-entities-per-sentence
  [text]
  (for [sentence (analyze text)]
    (named-entities-of-analyzed-sentence sentence)))

(defn named-entities
  [text]
  (apply concat (named-entities-per-sentence text)))

(defn- conj-entity
  [ent1 ent2]
  (if (= (:ne ent1) (:ne ent2)) 
    (list {:ne (:ne ent1)
      :pos (str (:pos ent1) "+" (:pos ent2))
      :text (str (:text ent1) "+" (:text ent2))
      }) 
    (list ent1 ent2)))

(defn group-entities
  [named-entities]
  (for [ne named-entities]
    (flatten (reduce conj-entity named-entities))))

