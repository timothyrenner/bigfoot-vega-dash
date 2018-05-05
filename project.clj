(defproject bigfoot-vega-dash "0.1.0-SNAPSHOT"
  :description "Bigfoot Dashboard with Vega"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [
    [org.clojure/clojure "1.8.0"]
    [org.clojure/data.csv "0.1.4"]
    [metasoarous/oz "1.3.1"]
    [hiccup "1.0.5"]]
  :main ^:skip-aot bigfoot-vega-dash.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
