(ns metadata-crawler.util
  (:require [noir.io :as io]
            [markdown.core :as md]
            [clojure.java.jdbc :as jdbc]))

(defn md->html
  "reads a markdown file from public/md and returns an HTML string"
  [filename]
  (md/md-to-html-string (io/slurp-resource filename)))

(defn get-db-metadata [type ip port user password db table-name]
  (if (not (.contains (list "mysql" "sqlserver") type))
    {:success false :info (str "type: " type " not mysql or sqlserver!")}
    (let [db-spec (condp = type
                    "mysql" {:subprotocol type
                             :subname (str "//" ip ":" port "/" db)
                             :user user
                             :password password}
                    "sqlserver" {:subprotocol type
                                 :subname (str "//" ip ":" port ";DatabaseName=" db)
                                 :user user
                                 :password password})]
      (try
        {:success true
         :info (jdbc/with-db-metadata [md db-spec]
                 {:columns-info (jdbc/metadata-result (.getColumns md nil nil table-name nil))
                  :pk-info (condp = type
                             "mysql" (jdbc/metadata-result (.getPrimaryKeys md nil nil table-name))
                             "sqlserver" (jdbc/metadata-result (.getPrimaryKeys md nil nil table-name)))})}
        (catch Exception e
          {:success false :info (.toString e)})))))
