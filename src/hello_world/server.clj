(ns hello-world.server
  (:require
   [org.httpkit.server]
   [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
   [ring.util.response :refer [resource-response content-type]]))


(defn handler [req]
  (or
   (when (= "/" (:uri req))
     (some-> (resource-response "index.html" {:root "public"})
             (content-type "text/html; charset=utf-8")))
   {:status 404
    :headers {"Content-Type" "text/html"}
    :body "Not found"}))

(org.httpkit.server/run-server (wrap-defaults handler site-defaults)
                               {:port 4000})
