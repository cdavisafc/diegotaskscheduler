(ns diegoscheduler.server
  (:require [diegoscheduler.diego :as diego]
            [org.httpkit.server :as http-kit]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.util.response :refer [resource-response]]
            [chord.http-kit :refer [wrap-websocket-handler]]
            [clojure.core.async :refer [<! >! put! close! go-loop go chan]])
  (:gen-class))

(def tasks (atom {:resolved []
                  :pending []}))
(defonce downch (chan))

(defn ws-handler [{:keys [ws-channel] :as req}]
  (go-loop []
    (when-let [{:keys [message error] :as msg} (<! ws-channel)]
      (if error
        (format "Error: '%s'." (pr-str msg))
        (diego/create-task message)
        )
      (recur)))
  (go-loop []
    (when-let [msg (<! downch)]
      (>! ws-channel msg)
      (recur))))

(defroutes app
  (GET "/" [] (resource-response "index.html" {:root "public"}))
  (GET "/ws" [] (-> ws-handler (wrap-websocket-handler)))
  (POST "/taskfinished" {body :body}
        (let [parsed-task (diego/parse-task (slurp body))]
          (put! downch
                (swap! tasks update-in [:resolved] conj parsed-task))
          {:status 200}))
  (route/resources "/")
  (route/not-found "<h1>Page not found</h1>"))

(defn -main []
  (http-kit/run-server app {:port 8080}))