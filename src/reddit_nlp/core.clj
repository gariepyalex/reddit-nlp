(ns reddit-nlp.core
  (:require [reddit-nlp.reddit :as reddit]
            [reddit-nlp.stanford-nlp-wrapper :as nlp]
            [reddit-nlp.duckduckgo :as ddgo]))

(defn- count-or-two
  [elements]
  (let [n (count elements)]
    (if (> n 0) n 2)))

(defn- sentiment-mean
  [elements]
  (let [n (count elements)]
    (if (> n 0) (/ (reduce + elements) n) 2)))

(defn comments-average-sentiment
  [comments]
  (sentiment-mean (flatten (for [comment (take 5 (sort-by :score > comments))] 
                             (nlp/analyze-sentiment (:body comment))))))

(defn cards
  [subreddit n]
  (let [posts (reddit/hot-posts-of-subreddit subreddit)]
    (take n (for [post posts]
              {:title (reddit/title-of-post post)
               :sentiment (comments-average-sentiment (reddit/flatten-comments (reddit/comments-of-post post)))
               :topics (ddgo/structured-instant-answers (nlp/analyze-named-entities (reddit/title-of-post post)))
               }))))

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
