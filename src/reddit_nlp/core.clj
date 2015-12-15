(ns reddit-nlp.core
  (:require [reddit-nlp.reddit :as reddit]
            [reddit-nlp.stanford-nlp-wrapper :as nlp]))

(defn -main
  [& args]
  (let [comments (-> (str (first args))
                     reddit/hot-posts-of-subreddit
                     second
                     reddit/comments-of-post)
        number-of-comments (count (reddit/all-comments-flat-seq comments))
        text (:body (first comments))]
    (println (format "There is %d comments on the first post"
                     number-of-comments))
    (println text)
    (println (nlp/analyze text))))
