(ns ring-app.core
  (:require [reitit.ring :as reitit]
            [muuntaja.middleware :as muuntaja]
            [ring.adapter.jetty :as jetty]
            [ring.util.http-response :as response]
            [clojure.pprint :as pp]
            [ring.middleware.reload :refer [wrap-reload]]) ; pulls var into ns
  (:gen-class))

(defn json-handler [request]
  (response/ok
   {:result (get-in request [:body-params :id])}))

(defn response-handler [request-map]
  "Simple handler. Returns caller's IP"
  (response/ok
   (str "<html><body> Hello Ana, your IP is "
        (:remote-addr request-map)
        "</body></html>")))

(defn wrap-formats [handler]
  (-> handler
      (muuntaja/wrap-format)))

(def routes
  [["/" {:get response-handler
         :post response-handler}]
   ["/echo/:id"
    {:get
     (fn [{{:keys [id]}  :path-params}]
       (response/ok (str "<p> the value is: " id "</p")))}]
   ["/api"
    {:middleware [wrap-formats]} ; this lets us use wrap-format only for api
    ["/multiply"
     {:post
      (fn [{{:keys [a b]} :body-params}] ; see
        (response/ok {:result (* a b)}))}]]])

(def handler
  (reitit/routes
   (reitit/ring-handler
    (reitit/router routes))
   (reitit/create-resource-handler
    {:path "/"})
   (reitit/create-default-handler
    {:not-found
     (constantly (response/not-found "404 - Page not found motherfucker"))
     :method-not-allowed
     (constantly (response/method-not-allowed "405 - Not Allowed"))
     :not-acceptable
     (constantly (response/not-acceptable "406 - Not acceptable"))})))

(defn wrap-nocache [http-handler]
  "uses assoc-in to add a new header and header value to response-map"
  (fn [request]
    (-> request
        handler ; function passed in, returns a response map
        (assoc-in [:headers "Pragma"] "no-cache")))) ; adds header to map


(defn -main []
  (jetty/run-jetty
   (-> #'handler ; pass a reference to the handler, don't eval it yet
       wrap-nocache ; threader macro passes handler to wrapper
       wrap-reload) ; does handler get re-evaluated in here?
   {:port 3000
    :join? false}))
