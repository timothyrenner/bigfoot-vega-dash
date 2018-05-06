(ns bigfoot-vega-dash.visualization
  (:require [clojure.data.csv :as csv]
            [clojure.string :as str]))

;;;; From clojure data.csv readme.
(defn- csv-data->maps [csv-data]
  (map zipmap
    (->> (first csv-data) ;; First row is the header
         (map keyword) ;; Drop if you want string keys instead
         repeat)
	  (rest csv-data)))

(defn csv-to-maps [filename]
  (let [file (-> filename slurp csv/read-csv doall)]
    (csv-data->maps file)))

(defn- pluck [fields list-of-dicts]
    (map #(select-keys % fields) list-of-dicts))

(defn- remove-blanks [field data]
  "Removes blank string fields from the list of maps."
  (filter #(not (str/blank? ((keyword field) %))) data))

(defn- data-spec [data]
  "Return the specifier for the data based on whether it's a name or sequence."
  (if (coll? data)
    {:data {:values data}}
    {:data {:name data}}))

(defn- bar-chart-categorical [field data]
  (let [
    plot {
      :mark "bar"
      :encoding {
        :x {:field field :type "nominal"}
        :y {:aggregate "count" :type "quantitative"}}}]
    (merge (data-spec data) plot)))

(defn- histogram [field data & {:keys [step] :or {step nil}}]
  (let [
    plot {
      :mark "bar"
      :encoding {
        :x {:field field :type "quantitative" :bin {:step step}}
        :y {:aggregate "count" :type "quantitative"}}}]
    (merge (data-spec data) plot)))

(defn- line [field data]
  (let [
    plot {
      :mark "line"
      :encoding {
        :x {:field field :type "quantitative"}
        :y {:aggregate "count" :type "quantitative"}}}]
  (merge (data-spec data) plot)))

(defn- geo-polygon
  [polygon feature & 
    {
      :keys [fill stroke format projection] 
      :or {
        fill "lightgrey" 
        stroke "white" 
        format "topojson" 
        projection "albersUsa"}}]
  (merge
    (merge-with merge 
      (data-spec polygon) {:data {:format {:type format :feature feature}}})
    {
      :projection {:type projection}
      :mark {
        :type "geoshape"
        :fill fill
        :stroke stroke}}))

(defn- geo-point
  [point lon-field lat-field & 
    {
      :keys [projection size]
      :or {
        projection "albersUsa"
        size 7}}]
  (merge
    (data-spec point)
    {
      :projection {:type projection}
      :mark "circle"
      :encoding {
        :size {:value size}
        :longitude {:field lon-field :type "quantitative"}
        :latitude {:field lat-field :type "quantitative"}}}))

(defn bigfoot-sightings-by-year [bigfoot-data]
  "Draws a line chart of the number of bigfoot sightings each year."
  (merge-with 
    merge 
    (line "date" bigfoot-data) 
    {:encoding {:x {:field "date" :type "temporal" :timeUnit "year"}}}))

(defn bigfoot-sightings-by-classification [bigfoot-data]
  "Draws a bar chart of the number of bigfoot sightings by report 
   classification."
   (bar-chart-categorical "classification" bigfoot-data))

(defn bigfoot-sightings-by-state [bigfoot-data]
  "Draws a bar chart of the number of bigfoot sightings by state."
  (bar-chart-categorical "state" bigfoot-data))

(defn bigfoot-sightings-by-temperature [bigfoot-data]
  "Draws a histogram of the bigfoot sightings by temperature."
  (histogram "temperature_mid" bigfoot-data :step 5))

(defn bigfoot-sightings-by-visibility [bigfoot-data]
  "Draws a histogram of the bigfoot sightings by visibility."
  (histogram "visibility" bigfoot-data))

(defn bigfoot-sightings-by-humidity [bigfoot-data]
  "Draws a histogram of the bigfoot sightings by humidity."
  (histogram "humidity" bigfoot-data))

(defn bigfoot-sightings-by-wind-speed [bigfoot-data]
  "Draws a histogram of the bigfoot sightings by wind speed."
  (histogram "wind_speed" bigfoot-data :step 1))

(defn bigfoot-sightings-map [bigfoot-data usa & 
  {
    :keys [width height] 
    :or {width 800 height 600}}]
  "Draws a map of the bigfoot sightings."
  {
    :width width
    :height height
    :layer [
      (geo-polygon usa "states")
      (geo-point bigfoot-data "longitude" "latitude")]})

(defn bigfoot-dashboard [bigfoot-data]
  (let [width 1200
        autosize-spec {:autosize {:type "fit" :contains "padding"}}]
    {
      :datasets {
        :bigfoot-data 
          (map #(dissoc % :summary :observed :location_details) bigfoot-data)
      }
      :vconcat [
        (merge 
          (bigfoot-sightings-by-year "bigfoot-data") 
          {:width width} 
          autosize-spec)
        (merge 
          (bigfoot-sightings-by-state "bigfoot-data") 
          {:width width} 
          autosize-spec)
        {:hconcat [
          ;; We have to divide by five to account for the padding between
          ;; the plots in the layout.
          ;; NOTE: The "encouraged" way to do this is to use repeat.
          (merge 
            (bigfoot-sightings-by-humidity "bigfoot-data") 
            {:width (/ width 5)}
            autosize-spec)
          (merge 
            (bigfoot-sightings-by-temperature "bigfoot-data")
            {:width (/ width 5)}
            autosize-spec)
          (merge
            (bigfoot-sightings-by-wind-speed "bigfoot-data")
            {:width (/ width 5)}
            autosize-spec)
          (merge
            (bigfoot-sightings-by-visibility "bigfoot-data")
            {:width (/ width 5)}
            autosize-spec)]}]}))