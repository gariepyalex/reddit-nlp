(ns tp3.core
  (:require [tp3.reddit :as reddit]))

(defn -main
  [& args]
  (let [comments (-> "worldnews"
                     reddit/posts-of-subreddit
                     first
                     reddit/comments-of-post)
        number-of-comments (count (reddit/all-comments-flat-seq comments))]
    (println (format "There is %d comments on the first post"
                     number-of-comments))))
    
