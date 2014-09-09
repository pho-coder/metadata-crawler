(ns metadata-crawler.routes.crawler
  (:require [compojure.core :refer :all]
            [metadata-crawler.layout :as layout]
            [metadata-crawler.util :as util]
            [taoensso.timbre :as timbre]))

(defn check-params [params]
  (cond
   (not (contains? params "type")) {:success false :info "params type not exists!"}
   (not (contains? params "ip")) {:success false :info "params ip not exists!"}
   (not (contains? params "port")) {:success false :info "params port not exists!"}
   (not (contains? params "user")) {:success false :info "params user not exists!"}
   (not (contains? params "password")) {:success false :info "params password not exists!"}
   (not (contains? params "db")) {:success false :info "params db not exists!"}
   (not (contains? params "table")) {:success false :info "params table not exists!"}
   :else {:success true :info "data source params check ok!"}))

(defn parse-metadata [table metadata]
  (if (empty? table)
    {:success false :info (str "table: " table " is empty!")}
    (let [table-map {:table-name table
                     :columns []
                     :pk []}
          columns-info (reduce (fn [m v]
                                 (if (= (v :table_name) table)
                                   (update-in m [:columns] conj {:column-name (v :column_name)
                                                                 :type-name (v :type_name)
                                                                 :column-size (v :column_size)})
                                   m)) table-map (:columns-info metadata))
          _ (timbre/info (:pk-info metadata))
          table-info (reduce (fn [m v]
                               (if (= (v :table_name) table)
                                 (update-in m [:pk] conj (v :column_name))
                                 m)) columns-info (:pk-info metadata))]
      {:success true :info table-info})))

(defn get-metadata [params]
  (let [_ (timbre/info params)
        check-params-result (check-params params)]
    (if (not (:success check-params-result))
      {:code 1 :info (:info check-params-result)}
      (let [get-db-metadata-result (util/get-db-metadata (params "type") (params "ip") (params "port") (params "user") (params "password") (params "db") (params "table"))]
        (if (not (:success get-db-metadata-result))
          {:code 2 :info (:info get-db-metadata-result)}
          (let [parse-metadata-result (parse-metadata (params "table") (:info get-db-metadata-result))]
            (if (not (:success parse-metadata-result))
              {:code 3 :info (:info parse-metadata-result)}
              {:code 0 :info (:info parse-metadata-result)})))))))

(defroutes crawler-routes
  (POST "/get-metadata" {form-params :form-params}
        {:body (get-metadata form-params)}))
