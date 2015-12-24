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

(def webdataset-base-dir "resources/webdataset/")

(defn- webdataset-file
  [id]
  (let [file-name (str webdataset-base-dir id)]
    (io/make-parents file-name)
    file-name))

(defn create-webdataset!
  [subreddit cards]
  (doseq [card cards]
    (let [file (webdataset-file subreddit)]
      (spit file {:cards cards}))))

(defn dataset
  []
  (let [files (filter #(not (.isDirectory %))
                      (file-seq (io/file (io/resource "dataset"))))]
    (for [file files]
      (read-string (slurp file)))))

(defn webdataset
  [subreddit]
  (let [file (io/resource (str "webdataset/" subreddit))]
    (if file
      (read-string (slurp file))
      {:cards []})))

