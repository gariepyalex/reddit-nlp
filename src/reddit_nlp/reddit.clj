(ns reddit-nlp.reddit
  (:require [cheshire.core :as cheshire]
            [org.httpkit.client :as http]))

(def reddit-base-url "http://reddit.com")

(def relevant-post-attributes [:id :permalink :url :score])
(def relevant-comment-attributes [:body :guilded :score])

;;===========================================
;; Get post from subreddit

(defn- subreddit->hot-url
  [subreddit-name]
  (str reddit-base-url "/r/" subreddit-name ".json"))

(defn- extract-post-attributes
  [post-list]
  (->> post-list
       (map :data)
       (map #(select-keys % relevant-post-attributes))))

(defn- parse-posts
  [raw-post-json]
  (-> raw-post-json
      (cheshire/parse-string true)
      :data
      :children
      extract-post-attributes))

(defn- fetch-posts
  [url]
  (-> url
      http/get
      deref
      :body
      parse-posts))

(defn hot-posts-of-subreddit
  [subreddit-name]
  (-> subreddit-name
      subreddit->hot-url
      fetch-posts))

(def time-filters {:hour  "&t=hour"
                   :day   "&t=day"
                   :week  "&t=week"
                   :month "&t=month"
                   :year  "&t=year"})

(defn- subreddit->top-url
  [subreddit time-filter]
  (str reddit-base-url "/r/" subreddit "/top/.json?sort=top" (get time-filters time-filter)))

(defn top-posts-of-subreddit
  [subreddit time-filter]
  (-> subreddit
      (subreddit->top-url time-filter)
      fetch-posts))

;;===========================================
;; Comment tree parsing

(defn- extract-relevant-attributes
  [comment]
  (let [comment-data (:data comment)]
    (select-keys comment-data relevant-comment-attributes)))

(defn- parse-comment-subtree
  [subtree]
  (assoc (extract-relevant-attributes subtree)
         :replies
         (into []
               (map parse-comment-subtree
                    (get-in subtree [:data :replies :data :children])))))

(defn- parse-all-comment-subtrees
  [raw-comment-map]
  (->> (get-in raw-comment-map [:data :children])
       (filter #(= "t1" (:kind %)))
       (map parse-comment-subtree)))

;;===========================================
;; Get comments from posts

(defn- permalink->url
  [permalink]
  (str reddit-base-url permalink ".json"))

(defn- parse-comments
  [raw-comments-json]
  (-> raw-comments-json
      (cheshire/parse-string true)
      second
      parse-all-comment-subtrees))

(defn- body-of-post
  [post]
  (-> post
      :permalink
      permalink->url
      http/get
      deref
      :body))

(defn- parse-title
  [raw-title-json]
  (-> raw-title-json
      (cheshire/parse-string true)
      first
      :data
      :children
      first
      :data
      :title))

(defn title-of-post
  [post]
  (-> (body-of-post post)
      (parse-title)))

(defn comments-of-post
  [post]
  (-> (body-of-post post)
      parse-comments))

(defn comment-subtree-seq
  [subtree]
  (tree-seq #(empty? (get % :replies))
            #(get % :replies)
            subtree))

(defn all-comments-seq
  [all-comment-subtrees]
  (mapcat comment-subtree-seq all-comment-subtrees))

(defn all-comments-flat-seq
  [all-comments-subtrees]
  (map #(select-keys % relevant-comment-attributes)
       (all-comments-seq all-comments-subtrees)))
