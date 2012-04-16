(ns yu2.be.test
  (:use [yu2.be])
  (:require [compojure.handler :as handler])
  (:use [clojure.test]))

(defn request [uri-path web-app & options]
  "Request something from the web app, returning the structured response"
  (let [path-rest (re-seq #"[^?]+" uri-path)]
    (let [resource (first path-rest)
          query-string (apply str (interpose \? (rest path-rest)))]
      (web-app {:request-method :get
                :uri resource
                :query-string query-string}))))

(defn response-body [response]
  "Get the response body, keeping in mind that it may be a string or a file"
  (let [body (:body response)]
    (if (instance? java.io.File body)
      (slurp body)
      (str body))))

(defn request-body [resource web-app]
  (response-body (request resource web-app)))

(defn substring? [needle haystack]
  (.contains haystack needle))

(defn shorten-failure? [url web-app]
  (substring?
    "Could not determine"
    (request-body url web-app)))

(defn redirect-to-vid? [id resource web-app]
  (let [response (request resource web-app)]
    (and
      (= 302 (:status response))
      (= (str "/watch?v=" id)
         (let [uri (java.net.URI. (get-in response [:headers "Location"]))]
           (str (.getRawPath uri) "?" (.getRawQuery uri)))))))

(defn shortens-to-vid? [id resource web-app]
  (let [response (request resource web-app)]
    (and
      (= 200 (:status response))
      (let [body (response-body response)
            uri-path (re-find #"value=\"https?://[^/]+(/[^\"]+)\"" body)]
        (= (second uri-path) (str "/" id))))))


(let [web-app (handler/api yu2.be/yu2be)]

  (deftest test-home
    (is (= 200 (:status (request "/" web-app))))
    (is (substring?
          "This is just a YouTube URL shortener"
          (request-body "/" web-app))))

  (deftest test-redirects
    (is (redirect-to-vid? "abcdEFGH123" "/abcdEFGH123" web-app)))

  (deftest test-shorteners
    (is (shortens-to-vid? "abcdEFGH123" "/?v=abcdEFGH123" web-app))
    (is (shortens-to-vid? "abcdEFGH123" "/watch?v=abcdEFGH123" web-app))
    (is (shortens-to-vid? "abcdEFGH123" "/e/abcdEFGH123" web-app))
    (is (shortens-to-vid? "abcdEFGH123" "/v/abcdEFGH123" web-app)))

  (deftest test-failures
    (is (shorten-failure? "/watch?novid=abcd" web-app))
    (is (shorten-failure? "/badurl" web-app))))
