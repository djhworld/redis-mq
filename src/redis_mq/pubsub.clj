(ns redis-mq.pubsub
    (:use [redis-mq.core :as core])
    (:use [redis-mq.queue :as queue])
    (:use [clojure.string :as str :only [trim]])
    (:require [clj-redis.client :as redis])
    (:use [clojure.tools.logging :as l]))

(defmacro sub [connection channel dispatch]
  `(do
      (l/info ~"RMQ | PUB-SUB | Initializing...")
      (l/info ~"RMQ | PUB-SUB | Subscribing to channel" ~channel)
      (redis/subscribe ~connection [~channel] (fn [ch# json-msg#]
        (let [msg# (~core/unwrap-msg json-msg#)
              error-queue# (~str ~channel ".error")]
        (l/info ~"RMQ | PUB-SUB | Received message" (:message-id msg#) "on channel" (~str/trim ~channel))
          (try
            (~dispatch (:body msg#))
          (catch Exception e#
            (l/error "RMQ | PUB-SUB | Caught exception attempting to process message " (:message-id msg#) ", distributing to error queue: " error-queue#)
            (queue/produce ~connection error-queue# { :original-msg msg# :error (str e#) })
          )))))))

(defmacro pub [connection channel msg-map]
    `(redis/publish ~connection ~channel (~core/wrap-msg ~msg-map)))
