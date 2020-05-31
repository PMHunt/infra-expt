(ns ring-app.core
  (:require [muuntaja.middleware :as muuntaja]
            [ring.adapter.jetty :as jetty]
            [ring.util.http-response :as response]
            [clojure.pprint :as pp]
            [ring.middleware.reload :refer [wrap-reload]]) ; pulls var into ns
  (:gen-class))

(defn json-handler [request]
  (response/ok
   {:result (get-in request [:body-params :id])}))

(defn http-handler [request-map]
  "Simple handler. Returns caller's IP"
  (response/ok
   (str "<html><body> Hello Ana, your IP is "
        (:remote-addr request-map)()
        "</body></html>")))

(def handler json-handler)

(defn wrap-nocache [http-handler]
  "uses assoc-in to add a new header and header value to response-map"
  (fn [request]
    (-> request
        handler ; function passed in, returns a response map
        (assoc-in [:headers "Pragma"] "no-cache")))) ; adds header to map

(defn wrap-formats [handler]
  (-> handler
      (muuntaja/wrap-format)))

(defn -main []
  (jetty/run-jetty
   (-> #'handler ; pass a reference to the handler, don't eval it yet
       wrap-nocache ; threader macro passes handler to wrapper
       wrap-formats
       wrap-reload) ; FIXME does handler get re-evaluated in here?
   {:port 3000
    :join? false}))
