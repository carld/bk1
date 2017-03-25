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
                    [:name :varchar "NOT NULL"]
                    [:iban :varchar]
                    [:created_at :timestamp "NOT NULL" "DEFAULT CURRENT_TIMESTAMP"])
  (sql/create-table "transactions"
                    [:id :serial "PRIMARY KEY"]
                    [:particulars :varchar]
                    [:code :varchar]
                    [:reference :varchar]
                    [:amount :numeric "NOT NULL"]
                    [:credit :integer "NOT NULL REFERENCES accounts(id)"]
                    [:debit :integer "NOT NULL REFERENCES accounts(id)"]
                    [:created_at :timestamp "NOT NULL" "DEFAULT CURRENT_TIMESTAMP"]))

(defn add-dummy-accounts []
  (sql/with-connection db
    (sql/insert-record :accounts {:id 1 :name "Test One"})
    (sql/insert-record :accounts {:id 2 :name "Test Two"})))

(defn add-dummy-transactions []
  (sql/with-connection db
    (sql/insert-record :transactions {:amount 42.95 :credit 2 :debit 1})
    (sql/insert-record :transactions {:amount 12.34 :credit 2 :debit 1})))

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
  (migrate #'initial-schema)
  (add-dummy-accounts)
  (add-dummy-transactions))
