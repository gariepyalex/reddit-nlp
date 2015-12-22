(ns reddit-nlp.web
  (:require [reddit-nlp.dataset :as dataset]
            [org.httpkit.server :as httpkit]
            [hiccup.page :as hiccup]
            [compojure.core :refer [defroutes GET]]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]))

(def colors ["#673ab7"
             "#3f51b5"
             "#2196f3"
             "#4caf50"
             "#8bc34a"
             "#cddc39"
             "#ffc107"
             "#ff9800"
             "#ff5722"])

(def subreddits ["news"
                 "worldnews"
                 "johncena"])

(defn color-picker-generator
  [sentiments]
  (let [min-sentiment (apply min sentiments)
        max-sentiment (apply max sentiments)
        last-index    (dec (count colors))
        delta         (/ last-index (- max-sentiment min-sentiment))
        b             (- (* delta min-sentiment))]
    (fn [sentiment]
      (let [index (int (+ b (* delta sentiment)))]
        (nth colors (min index last-index))))))

(defn- popup-links
  [id topic]
  [:ul.mdl-menu.mdl-menu--bottom-left.mdl-js-menu.mdl-js-ripple-effect
   {:for id}
   (for [link (:related topic)]
     [:li.mdl-menu__item
      [:a {:href link :target "_blank"} link]])])

(defn- mdl-card
  [color-picker {:keys [title sentiment topics]}]
  [:div.mdl-card.mdl-cell.mdl-cell--6-col.mdl-shadow--2dp
   {:style "z-index:auto; overflow:visible;"}
   [:div.mdl-card__title
    {:style (str "background-color:" (color-picker sentiment) ";color:#fff;")}
    [:h4.mdl-card__title-text title]]
   [:div.mdl-card__supporting-text
    [:p [:strong "Sentiment"]]
    [:p (format "%.2f" (float sentiment))]]
   [:div.mdl-card__actions.mdl-card--border
    (for [topic topics]
      (let [id (str title (:topic topic))]
        [:div [:a.mdl-button.mdl-button--colored.mdl-js-button.mdl-js-ripple-effect
               {:id id}
               (:topic topic)]
         (popup-links id topic)
         [:div.mdl-card__supporting-text (:abstract topic)]]))]])

(defn- sentiment-analysis-page
  [subreddit]
  (let [cards (:cards (dataset/webdataset subreddit))]
    (hiccup/html5
     [:head
      [:meta {:charset "UTF-8"}]
      [:meta {:name "viewport"
              :content "width=device-width, initial-scale=1"}]
      [:title "NLP - TP3"]
      (hiccup/include-css "/material.css")
      (hiccup/include-js "/material.js")]
     [:body
      [:div.mdl-layout.mdl-js-layout
       [:main.mdl-layout__content
        [:div.mdl-grid
         [:div.mdl-cell.mdl-cell--12-col
          [:h1 "Reddit - NLP"]
          (for [sub subreddits]
            [:a.mdl-button.mdl-button--colored.mdl-js-button.mdl-js-ripple-effect
             {:href (str "/" sub)} sub])]
         (when (not (empty? cards))
           (let [color-picker (color-picker-generator (map :sentiment cards))]
             (for [c cards]
               (mdl-card color-picker c))))]]]])))

(def default-subreddit "news")
(defroutes app-routes
  (GET "/" [] (sentiment-analysis-page default-subreddit))
  (GET "/:subreddit"
       [subreddit]
       (sentiment-analysis-page subreddit))
  (route/resources "/")
  (route/not-found "404 - these are not the droid you're looking for."))

(def app
  (wrap-defaults app-routes site-defaults))

(def port 8000)
(defonce server (atom nil))

(defn run-server
  []
  (reset! server (httpkit/run-server app {:port port}))
  (println (str "server now running on port " port)))

(defn stop-server
  []
  (when-not (nil? @server)
    (@server :timeout 100)
    (reset! server nil)))

(defn -main
  [& args]
  (run-server))
