(ns tp3.reddit
  (:require [cheshire.core :as cheshire]
            [org.httpkit.client :as http]))

(def reddit-base-url "http://reddit.com")

(def relevant-comment-attributes [:body :guilded :score])

;;===========================================
;; Get post from subreddit

(defn- subreddit->url
  [subreddit-name]
  (str reddit-base-url "/r/" subreddit-name ".json"))

(defn- parse-posts
  [raw-post-json]
  (-> raw-post-json
      (cheshire/parse-string true)
      :data
      :children
      (#(map :data %))))

(defn posts-of-subreddit
  [subreddit-name]
  (-> subreddit-name
      subreddit->url
      http/get
      deref
      :body
      parse-posts))

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
  (map parse-comment-subtree
       (get-in raw-comment-map [:data :children])))

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

(defn comments-of-post
  [permalink]
  (-> permalink
      permalink->url
      http/get
      deref
      :body
      parse-comments))

(defn comment-subtree-seq
  [subtree]
  (tree-seq map?
            #(get % :replies)
            subtree))

(defn all-comments-seq
  [all-comment-subtrees]
  (mapcat comment-subtree-seq all-comment-subtrees))

(defn all-comments-flat-seq
  [all-comments-subtrees]
  (map #(select-keys % relevant-comment-attributes)
       (all-comments-seq all-comments-subtrees)))
