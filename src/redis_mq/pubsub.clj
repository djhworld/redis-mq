(ns redis-mq.pubsub
    (:use [redis-mq.core :as core])
    (:use [redis-mq.queue :as queue])
    (:use [clojure.string :as str :only [trim]])
    (:require [clj-redis.client :as redis])
    (:use [clojure.tools.logging :as l]))

(defmacro sub [connection-string channel dispatch]
  `(do
     (l/info ~"RMQ | PUB-SUB | Initializing...")
     (l/info ~"RMQ | PUB-SUB | Subscribing to channel" ~channel)
     (let [connection# (redis/init {:url ~connection-string })]
       (redis/subscribe connection# [~channel] (fn [ch# json-msg#]
                                                 (let [msg# (~core/unwrap-msg json-msg#)]
                                                   (l/info ~"RMQ | PUB-SUB | Received message" (:message-id msg#) "on channel" (~str/trim ~channel))
                                                   (try
                                                     (~dispatch (:body msg#))
                                                     (catch Exception e#
                                                       (l/error "RMQ | PUB-SUB | Caught exception attempting to process message " (:message-id msg#))
                                                       (queue/distribute-to-error-queue ~connection-string ~channel msg# (str e#))
                                                       ))))))))

(defmacro pub [connection-string channel msg-map]
    `(redis/publish (redis/init {:url ~connection-string }) ~channel (~core/wrap-msg ~msg-map)))
