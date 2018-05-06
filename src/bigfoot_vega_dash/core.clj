(ns bigfoot-vega-dash.core
  (:require 
    [hiccup.page :refer [html5 include-css include-js]]
    [clojure.data.json :as json]
    [bigfoot-vega-dash.visualization :as bigv])
  (:gen-class))

(def vega-cdn "https://cdn.jsdelivr.net/npm")

(defn -main
  "Builds the bigfoot dashboard."
  [& args]
  (let [
    data-file (first args)
    polygon-file (second args)
    output-file (if-let [f (get args 2)] f "bigfoot.html")
    bigfoot-data (bigv/csv-to-maps data-file)
    usa (slurp polygon-file)
    ;; Debug - just the map for now since it's the hardest.
    visualization (bigv/bigfoot-dashboard bigfoot-data)]
    (spit output-file
      (html5
        {:lang "en"}
        [:head
          [:meta {:charset "utf-8"}]
          [:title "Bigfoot Sightings"]
          (include-js (str vega-cdn "/vega@3.3.1/build/vega.js"))
          (include-js (str vega-cdn "/vega-lite@2.4.1/build/vega-lite.js"))
          (include-js (str vega-cdn "/vega-embed@3.7.1/build/vega-embed.js"))]
        [:body
          [:h1 "Bigfoot Sightings"]
          [:div#vis]
          [:script (str
            "var bigfootSpec = "
            (json/write-str (merge {:$schema "https://vega.github.io/schema/vega-lite/v2.0.json"} visualization))
            "; vegaEmbed(\"#vis\", bigfootSpec);")]]))))