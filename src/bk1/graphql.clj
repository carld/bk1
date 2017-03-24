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

(def db (env/env :database-url "postgres://localhost:5432/bk1"))

; http://localhost:3002/graphql?query={transactions(account_id:1){id+amount+created_at}}

(def bk1-schema "
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
  txn(id: Int!): Transaction
}

type Mutation {

}

schema {
  query: Query
  mutation: Mutation
}
")

(defn get-transactions [account-id]
  (sql/with-connection db
    (try (sql/with-query-results txns
                                 ["SELECT * FROM transactions WHERE credit = ? OR debit = ?"
                                  account-id account-id]
;           (println txns)
           (doall txns))
         (catch Exception _))))

(defn starter-resolver-fn [type-name field-name]
  (match/match
   [type-name field-name]
   ["Query" "transactions"] (fn [context parent args]
                              (get-transactions (get args "account_id")))
   :else nil))

(def parsed-schema (parser/parse bk1-schema))

(defn execute
  [query variables]
  (let [type-schema (validator/validate-schema parsed-schema)
        context nil]
    (executor/execute context type-schema starter-resolver-fn query variables)))
