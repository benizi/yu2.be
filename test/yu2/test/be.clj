(ns yu2.test.be
  (:use [yu2.be])
  (:require [compojure.handler :as handler])
  (:use [clojure.test]))

(defn request [resource web-app & params]
  "Request something from the web app, returning the structured response"
  (web-app {:request-method :get
            :uri resource
            :params (first params)}))

(defn request-body [resource web-app & params]
  "Get the request body, keeping in mind that request can return either a Java String or a Java File"
  (let [body (:body (request resource web-app params))]
    (if (instance? java.io.File body)
      (slurp body)
      (str body))))

(defn substring? [needle haystack]
  (.contains haystack needle))

(let [web-app (handler/api yu2.be/yu2be)]

  (deftest test-home
    (is (= 200 (:status (request "/" web-app))))
    (is (substring? "This is just a Youtube URL shortener"
                   (request-body "/" web-app))))

  (deftest test-routes
    (is (substring? "Could not determine"
                   (request-body "/badurl" web-app)))))
