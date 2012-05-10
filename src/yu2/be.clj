(ns yu2.be
  (:use [compojure.core])
  (:require [compojure.route :as route]
            [compojure.handler :as handler])
  (:use [hiccup.core])
  (:use [ring.adapter.jetty :only [run-jetty]])
  (:use [ring.util.response :as response]))

(def youtube-id #"[A-Za-z0-9\-._]{11}")

(defn vid [id]
  (response/redirect (str "http://youtube.com/watch?v=" id)))

(defn shortener [id & options]
  "An HTML page showing the shortened URL for the given ID"
  (let [host (or (:host (first options)) "yu2.be")
        short-url (str "http://" host "/" id)
        thumb-url (str "http://i3.ytimg.com/vi/" id "/hqdefault.jpg")]
    (html
      [:html
       [:head
        [:title "YouTube short URL - yu2.be"]]
       [:body {:onload "document.getElementById('url').focus()"}
        [:form
         [:label {:for "url"} "Short URL"]
         [:input {:id "url"
                  :type "text"
                  :size (.length short-url)
                  :value short-url}]
         [:div
          [:a {:href "/"} "What is this page?"]]]
        [:img {:src thumb-url}]]])))

(defroutes yu2be
  (GET "/" {params :params}
       (if (params :v)
         :next
         (response/file-response "index.html" {:root "public"})))
  (GET ["/:id" :id youtube-id] [id]
       (vid id))
  (GET ["/:short/:id" :short #"[ev]" :id youtube-id] [_ id]
       (shortener id))
  (GET "/*" {params :params
             {host "host"} :headers}
       (if (params :v)
         (shortener (params :v) {:host host})
         :next))
  (route/not-found "Could not determine video from URL"))

(defn -main [port]
  (run-jetty (handler/api yu2be) {:port (Integer. port)}))
