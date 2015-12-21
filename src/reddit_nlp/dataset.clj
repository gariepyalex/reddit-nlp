(ns reddit-nlp.dataset
  (:require [clojure.java.io :as io]
            [reddit-nlp.reddit :as reddit]))

(def dataset-base-dir "resources/dataset/")

(defn- dataset-file
  [id]
  (let [file-name (str dataset-base-dir id)]
    (io/make-parents file-name)
    file-name))

(defn create-dataset!
  [post-seq]
  (doseq [post post-seq]
    (let [comments (reddit/comments-of-post post)
          file     (dataset-file (:id post))]
      (spit file {:post post
                  :comments comments}))))

(defn dataset
  []
  (let [files (filter #(not (.isDirectory %))
                      (file-seq (io/file (io/resource "dataset"))))]
    (for [file files]
      (read-string (slurp file)))))

;; The simplest way to run this is with 'lein run -m reddit-nlp.dataset subreddit-name'
(defn -main
  [subreddit & args]
  (let [posts (reddit/hot-posts-of-subreddit subreddit)]
    (create-dataset! posts))
  (println "dataset has been created"))
