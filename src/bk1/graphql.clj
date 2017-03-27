(ns bk1.graphql
  (:require [graphql-clj.parser :as parser]
            [graphql-clj.type :as type]
            [graphql-clj.resolver :as resolver]
            [graphql-clj.executor :as executor]
            [graphql-clj.validator :as validator]
            [graphql-clj.introspection :as introspection]
            [clojure.core.match :as match]
            [clojure.java.jdbc :as sql]
            [environ.core :as env]))

(def db-spec {:connection-uri  (env/env :database-url "jdbc:postgresql://localhost:5432/bk1")})

; http://localhost:3002/graphql?query={transactions(account_id:1){id+amount+created_at}}
; http://localhost:3002/graphql?query={account(id:1){id+name+transactions+{id+amount+created_at}}}

(def bk1-schema "
type Account {
  id: Int!
  name: String
  iban: String
  transactions: [Transaction]
}

type Transaction {
  id: Int!
  particulars: String
  code: String
  reference: String
  amount: Int
  created_at: String
}

type Query {
  transactions(account_id: Int!): [Transaction]
  transaction(id: Int!): Transaction
  accounts: [Account]
  account(id: Int!): Account
}

type Mutation {

}

schema {
  query: Query
  mutation: Mutation
}
")

(defn get-transactions [account-id]
  (sql/with-db-connection [db db-spec]
    (try (sql/query db
                    ["SELECT * FROM transactions WHERE credit = ? OR debit = ?"
                     account-id account-id])
         (catch Exception _))))

(defn get-account [account-id]
  (sql/with-db-connection [db db-spec]
    (try
      (let [account (sql/get-by-id db :accounts account-id :id)]
        (if account
          (assoc account :transactions
                 (sql/query db
                            ["SELECT * FROM transactions WHERE credit = ? or debit = ?" account-id account-id]))))
      (catch Exception e
        (println e)))))

(defn starter-resolver-fn [type-name field-name]
  (match/match
   [type-name field-name]
   ["Query" "transactions"] (fn [context parent args]
                              (get-transactions (get args "account_id")))
   ["Query" "account"] (fn [context parent args]
                         (get-account (get args "id")))
   :else nil))

(def parsed-schema (parser/parse bk1-schema))

(defn execute
  [query variables]
  (println query variables)
  (let [type-schema (validator/validate-schema parsed-schema)
        context nil]
    (executor/execute context type-schema starter-resolver-fn query variables)))
