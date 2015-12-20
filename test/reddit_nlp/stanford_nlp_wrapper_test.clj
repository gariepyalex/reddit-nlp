(ns reddit-nlp.stanford-nlp-wrapper-test
  (:require [clojure.test :refer :all]
            [reddit-nlp.stanford-nlp-wrapper :refer :all]))

(def test-named-entity-vector
  [{:text "Yazidi", :pos "NNP", :ne "MISC"}
   {:text "UN", :pos "NNP", :ne "ORGANIZATION"}
   {:text "some", :pos "NNP", :ne "O"}
   {:text "words", :pos "NNP", :ne "O"}
   {:text "Coca", :pos "NNP", :ne "ORGANIZATION"}
   {:text "Cola", :pos "NNP", :ne "ORGANIZATION"}])

(def test-text "Young Yazidi woman begs UN to wipe out Isis as she reveals horrifying details. Life under the group's control is bad.")

(def test-analyzed-named-entities-answer 
  '({:text "Yazidi", :pos "NNP", :ne "MISC"} 
    {:text "UN", :pos "NNP", :ne "ORGANIZATION"} 
    {:text "Isis", :pos "NNP", :ne "ORGANIZATION"}))

(deftest group-entities-test
  (let [grouped-entities (group-entities test-named-entity-vector)]
    (is (= "Coca+Cola" (:text (last grouped-entities))))
    (is (= 4 (count grouped-entities)))))

(deftest named-entities-test
  (let [named-entities-only (named-entities test-named-entity-vector)]
    (is (= 4 (count named-entities-only)))
    (for [entity named-entities-only]
      (is (not= "O" (:ne entity))))))

(deftest analyze-named-entities-test
  (let [analyzed (analyze-named-entities test-text)]
    (is (= test-analyzed-named-entities-answer analyzed))))
