(ns redis-mq.queue
    (:use [redis-mq.core :as core])
    (:use [clojure.string :as str :only [trim]])
    (:require [clj-redis.client :as redis])
    (:use [clojure.tools.logging :as l]))

(defn inspect-queue [connection queue]
  (if (redis/exists connection queue)
    (let [size (redis/llen connection queue)]
      (map (fn [i] (:body (core/unwrap-msg i))) (redis/lrange connection queue 0 size)))))

(defmacro produce [connection queue message]
    `(redis/lpush ~connection ~queue (~core/wrap-msg ~message)))

(defn distribute-to-error-queue [connection queue msg error]
  (let [error-queue (str queue ".error")]
    (l/error "RMQ | QUEUE | Caught exception attempting to process message " (:message-id msg) ", distributing to error queue: " error-queue)
    (produce connection error-queue { :original-msg msg :destination queue :error error })))


(defmacro consume [connection queue dispatch consumption-rate]
  `(do
     (l/info ~"RMQ | QUEUE | Initializing...")
     (l/info ~"RMQ | QUEUE | Listening to messages on queue " ~queue)
     (loop [x# (redis/llen ~connection ~queue)]
       (if(<= x# 0)
         (do (l/info "RMQ | QUEUE | No messages to consume, goodbye!") nil)
         (let [[q# message#] (redis/brpop ~connection [~queue] 0)]
           (let [msg# (~core/unwrap-msg message#)]
             (l/info ~"RMQ | QUEUE | Received message" (:message-id msg#) "from queue" (~str/trim ~queue))
             (try
               (~dispatch (:body msg#))
               (catch Exception e#
                 (distribute-to-error-queue ~connection ~queue msg# (str e#))))
             (. ~Thread sleep (* ~consumption-rate 1000)))
         (recur (dec x#)))))))

(defmacro consume-wait [connection queue dispatch consumption-rate]
  `(do
      (l/info ~"RMQ | QUEUE | Initializing...")
      (l/info ~"RMQ | QUEUE | Listening to messages on queue " ~queue)
      (loop [x# 0]
        (let [[q# message#] (redis/brpop ~connection [~queue] 0)]
          (let [msg# (~core/unwrap-msg message#)]
            (l/info ~"RMQ | QUEUE | Received message" (:message-id msg#) "from queue" (~str/trim ~queue))
            (try
              (~dispatch (:body msg#))
            (catch Exception e#
              (distribute-to-error-queue ~connection ~queue msg# (str e#))))
              (. ~Thread sleep (* ~consumption-rate 1000))))
        (recur (inc x#)))))
