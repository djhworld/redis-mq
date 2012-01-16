(ns redis-mq.core
    (:require [clj-time.core :as time])
    (:require [clj-time.format :as timefmt])
    (:use [cheshire.core :as json]))

(def date-formatter (timefmt/formatters :date-time))

(defn generate-id [date]
  "Crude message id generation function, uses the current date + a random number
  to generate an ID. Not guaranteed to be unique"
  (let [date-str (timefmt/unparse (timefmt/formatter "ddMMyyhhmmss") date)]
    (str date-str "-" (apply str (take 4 (drop 2 (str (rand))))))))

(defn wrap-msg [msg]
  "Wraps a message into a JSON string with additional metadata"
  (let [date (time/now)]
    (json/generate-string
        { :message-id (generate-id date)
          :time (timefmt/unparse date-formatter date)
          :body msg
        })))

(defn unwrap-msg [msg]
  "Parses JSON message into hash-map"
  (let [umsg (json/parse-string msg true)]
    umsg))


