(ns metadata-crawler.routes.crawler
  (:require [compojure.core :refer :all]
            [metadata-crawler.layout :as layout]
            [metadata-crawler.util :as util]))

(defn home-page []
  (layout/render
    "home.html" {:content (util/md->html "/md/docs.md")}))

(defn about-page []
  (layout/render "about.html"))

(defroutes crawler-routes
  (GET "/" [] (home-page))
  (GET "/about" [] (about-page)))
