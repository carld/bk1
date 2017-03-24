(ns bk1.db
  (:require [clojure.java.jdbc :as sql]
            [environ.core :as env]
                                        ;            [tentacles.repos :as repos])
            )
  (:import (java.util UUID))
  (:refer-clojure :exclude [find]))

(def db (env/env :database-url "postgres://localhost:5432/bk1"))

(defn initial-schema []
  (sql/create-table "accounts"
                    [:id :serial "PRIMARY KEY"]
                    [:name :varchar "NOT NULL"])
  (sql/create-table "transactions"
                    [:id :serial "PRIMARY KEY"]
                    [:amount :numeric "NOT NULL"]
                    [:credit :integer "NOT NULL REFERENCES accounts(id)"]
                    [:debit :integer "NOT NULL REFERENCES accounts(id)"]))

(defn run-and-record [migration]
  (println "Running migration:" (:name (meta migration)))
  (migration)
  (sql/insert-values "migrations" [:name :created_at]
                     [(str (:name (meta migration)))
                      (java.sql.Timestamp. (System/currentTimeMillis))]))

(defn migrate [& migrations]
  (sql/with-connection db
    (try (sql/create-table "migrations"
                           [:name :varchar "NOT NULL"]
                           [:created_at :timestamp "NOT NULL" "DEFAULT CURRENT_TIMESTAMP"])
         (catch Exception _))
    (sql/transaction
       (let [has-run? (sql/with-query-results run ["SELECT name FROM migrations"]
                        (set (map :name run)))]
         (doseq [m migrations
                 :when (not (has-run? (str (:name (meta m)))))]
           (run-and-record m))))))

(defn -main []
  (migrate #'initial-schema))
