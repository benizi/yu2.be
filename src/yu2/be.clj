(ns yu2.be
  (:use [compojure.core])
  (:require [compojure.route :as route]
            [compojure.handler :as handler])
  (:use [ring.adapter.jetty :only [run-jetty]])
  (:use [ring.util.response :as response]))

(def youtube-id #"[A-Za-z0-9\-._]{11}")

(defroutes yu2be
  (GET "/" [] (response/file-response "index.html" {:root "public"}))
  (GET ["/:id", :id youtube-id] [id]
       (response/redirect (str "http://youtube.com/watch?v=" id)))
  (GET "/*" {params :params}
       (if (params :v)
         (str "<html><body onload=\"document.getElementById('url').focus()\"><form><label for=\"url\">Short URL:</label><input id=\"url\" type=\"text\" value=\"http://yu2.be/" (params :v) "\"/></form></body></html>")
         "Could not determine video from URL"))
  (route/not-found "Page not found"))

(defn -main [port]
  (run-jetty (handler/api yu2be) {:port (Integer. port)}))
