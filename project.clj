(defproject bk1 "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [graphql-clj "0.1.20" :exclusions [org.clojure/clojure]]
                 [ring "1.5.1"]
                 [ring/ring-json "0.4.0"]
                 [ring/ring-defaults "0.2.3"]
                 [ring-cors "0.1.9"]
                 [compojure "1.5.2"]
                 [org.clojure/core.match "0.3.0-alpha4"]
                 [clojure-future-spec "1.9.0-alpha14"]
                 [postgresql "9.3-1102.jdbc41"]
                 [tentacles "0.5.1"]
                 [org.clojure/java.jdbc "0.6.1"]
                 ; https://github.com/barend/java-iban.git
                 [nl.garvelink.oss/iban "1.5.0"]]
  :main ^:skip-aot bk1.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}}
  :plugins [[lein-ring "0.9.7"]
            [cider/cider-nrepl "0.13.0-SNAPSHOT"]]
  :ring {:handler bk1.handler/app
         :auto-reload? true
         :port 3002
         :nrepl {:start? true}})
