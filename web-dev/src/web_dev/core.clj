(ns web-dev.core
  (:require [ring.adapter.jetty :as jetty]
            [clojure.pprint :as pprint]
            [compojure.core :as comp]
            [compojure.route :as route]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [selmer.parser :as selmer]))

(selmer/set-resource-path! "var/html/templates")

(defn foo
  "I don't do a whole lot."
  [x]
  (println x "Hello, World!"))

(defonce server (atom nil)) ;; Make a place to store the server object

(comp/defroutes routes
  (comp/GET "/" [] {:status 200
                    :body (selmer/render-file "homepage.html" {})
                    :headers {"Content-Type" "text/html; charset=UTF-8"}})
  (comp/ANY "/echo" req {:status 200
                         :body (with-out-str (pprint/pprint req))
                         :headers {"Content-Type" "text/plain"}})
  (comp/GET "/greeting" [] {:status 200
                            :body "Hello, World!"
                            :headers {"Content-Type" "text/plain"}})
  (comp/GET "/playground" [] {:status 200
                              :body (selmer/render-file "playground.html" {})
                              :headers {"Content-Type" "text/html; charset=UTF-8"}})
  (comp/GET "/exhibits" [] {:status 200
                            :body (selmer/render-file "exhibits.html" {})
                            :headers {"Content-Type" "text/html; charset=UTF-8"}})
  ;; Serve resources from resources/public
  (route/resources "/")
  
  (route/not-found {:status 404
                    :body "Not Found."
                    :headers {"Content-Type" "text/plain"}}))

(def app (-> (fn [req] (routes req))
             wrap-keyword-params
             wrap-params))

(defn start-server []
  (reset! server
          (jetty/run-jetty (fn [req] (app req))
                   {:port 3001 ;; Listen on port 3001
                    :join? false}))) ;; Dont block the main thread
(defn stop-server []
  (when-some [s @server]
    (.stop s)
    (reset! server nil)))
