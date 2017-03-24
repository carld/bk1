(defproject bk1 "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [graphql-clj "0.1.20" :exclusions [org.clojure/clojure]]
                 [clojure-future-spec "1.9.0-alpha14"]
                 [postgresql "9.3-1102.jdbc41"]
                 [tentacles "0.5.1"]
                 [org.clojure/java.jdbc "0.2.1"]]
  :main ^:skip-aot bk1.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
