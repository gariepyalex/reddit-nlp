(defproject reddit-nlp "0.1.0-SNAPSHOT"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [cheshire "5.5.0"]
                 [http-kit "2.1.18"]
                 [hiccup "1.0.5"]
                 [compojure "1.4.0"]
                 [ring/ring-defaults "0.1.5"]]
  :resource-paths ["resources"]
  :main ^:skip-aot reddit-nlp.web
  :test-paths ["test"]
  :target-path "target/%s"
  :profiles {:dev {:dependencies [[edu.stanford.nlp/stanford-corenlp "3.5.2"]]
                   :resource-paths ["lib/stanford-corenlp-models-current.jar"]}
             :uberjar {:aot [reddit-nlp.web]}})
