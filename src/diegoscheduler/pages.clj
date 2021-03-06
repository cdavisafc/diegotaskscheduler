(ns diegoscheduler.pages
  (:require [hiccup.page :refer [html5]]))

(defn- styles [& uris]
  (for [uri uris]
    [:link {:href uri :rel "stylesheet" :type "text/css"}]))

(defn index [{ws-url :ws-url}]
  (html5
   [:head
    (styles "https://cdnjs.cloudflare.com/ajax/libs/meyer-reset/2.0/reset.css"
            "css/style.css")]
   [:body
    [:div#app
     [:h2 "Loading..."]]
    [:script {:type "text/javascript"} "window.wsUrl = '" ws-url "';"]
    [:script {:src "/js/application.js" :type "text/javascript"}]]))
