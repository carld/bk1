(ns bk1.db
  (:require [clojure.java.jdbc :as sql]
            [environ.core :as env]
                                        ;            [tentacles.repos :as repos])
            )
  (:import (java.util UUID))
  (:refer-clojure :exclude [find]))

(def db-spec {:connection-uri (env/env :database-url "jdbc:postgresql://localhost:5432/bk1")})

(defn initial-schema [db]
  (sql/db-do-commands db
                      [(sql/create-table-ddl "entities"
                                             [[:id :serial "PRIMARY KEY"]
                                              [:name :varchar]])
                       (sql/create-table-ddl  "accounts"
                                              [[:id :serial "PRIMARY KEY"]
                                               [:name :varchar "NOT NULL"]
                                               [:iban :varchar]
                                               [:entity_id :integer "NOT NULL REFERENCES entities(id)"]
                                               [:created_at :timestamp "NOT NULL" "DEFAULT CURRENT_TIMESTAMP"]])
                       (sql/create-table-ddl "transactions"
                                             [[:id :serial "PRIMARY KEY"]
                                              [:particulars :varchar]
                                              [:code :varchar]
                                              [:reference :varchar]
                                              [:amount :numeric "NOT NULL"]
                                              [:credit :integer "NOT NULL REFERENCES accounts(id)"]
                                              [:debit :integer "NOT NULL REFERENCES accounts(id)"]
                                              [:created_at :timestamp "NOT NULL" "DEFAULT CURRENT_TIMESTAMP"]])
                       (sql/create-table-ddl "users"
                                             [[:id :serial "PRIMARY KEY"]
                                              [:name :varchar "NOT NULL"]
                                              [:entity_id :integer "NOT NULL" "REFERENCES entities(id)"]])]))

(defn add-dummy-data [db]
  (sql/insert-multi! db :entities
                     [{:id 1 :name "Entity One"}])
  (sql/insert-multi! db :accounts
                     [{:id 1 :name "Test One" :entity_id 1}
                      {:id 2 :name "Test Two" :entity_id 1}])
  (sql/insert-multi! db :transactions
                     [{:amount 42.95 :credit 2 :debit 1}
                      {:amount 12.34 :credit 2 :debit 1}]))

(defn run-and-record [db migration]
  (println "Running migration:" (:name (meta migration)))
  (migration db)
  (sql/insert! db "migrations" [:name :created_at]
                     [(str (:name (meta migration)))
                      (java.sql.Timestamp. (System/currentTimeMillis))]))

(defn migrate [& migrations]
  (sql/with-db-connection [db db-spec]
    (try (sql/db-do-commands db
          (sql/create-table-ddl "migrations"
                                [[:name :varchar "NOT NULL"]
                                 [:created_at :timestamp "NOT NULL" "DEFAULT CURRENT_TIMESTAMP"]]))
         (catch Exception _))

    (sql/db-do-commands db
                        (let [has-run? (let [result (sql/query db ["SELECT name FROM migrations"])]
                                         (set (map :name result)))]
                          (doseq [m migrations
                                  :when (not (has-run? (str (:name (meta m)))))]
                            (run-and-record db m))))))

(defn -main []
  (migrate #'initial-schema
           #'add-dummy-data))
