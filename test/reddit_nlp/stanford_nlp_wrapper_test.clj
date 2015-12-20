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

(deftest group-entities-test
  (let [grouped-entities (group-entities test-named-entity-vector)]
    (is (= "Coca+Cola" (:text (last grouped-entities))))
    (is (= 4 (count grouped-entities)))))

