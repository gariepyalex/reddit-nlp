(ns reddit-nlp.web
  (:require [org.httpkit.server :as httpkit]
            [hiccup.page :as hiccup]
            [compojure.core :refer [defroutes GET]]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]))

(defn mdl-card
  [text]
  [:div.mdl-card.mdl-cell.mdl-cell--6-col.mdl-shadow--2dp
   [:div.mdl-card__title
    [:h4.mdl-card__title-text text]]])

(defn sentiment-analysis-page
  [cards]
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
        [:h1 "Reddit - NLP"]]
       (for [i cards]
         (mdl-card (str i " " i)))]]]]))

(defroutes app-routes
  (GET "/" [] (sentiment-analysis-page (range 20)))
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
