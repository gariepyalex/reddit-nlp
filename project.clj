(defproject reddit-nlp "0.1.0-SNAPSHOT"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [cheshire "5.5.0"]
                 [http-kit "2.1.18"]
                 [edu.stanford.nlp/stanford-corenlp "3.3.1"]]
                 :main ^:skip-aot reddit-nlp.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
