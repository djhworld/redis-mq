# redis-mq

A small collection of utilities to deploy message queue like
functionality into your applications. By no means is this a production
ready product, or even that well coded, but I felt tools like the
excellent RabbitMQ were too complex for small projects like the ones I
tend to make.

So I made this as a compromise. It just allows you to get things up
and running with your redis installation so you can prototype Clojure
applications with pubsub/queue like functionality before dropping in
more robust messaging system

## Usage

### pub/sub
#### publisher

    (:use [redis-mq.pubsub :as rmq]
          [clj-redis.client :as redis])

    (def db (redis/init)) ;localhost
    (rmq/pub db "pubsub.test" "daniel")

#### subscriber

    (:use [redis-mq.pubsub :as rmq]
          [clj-redis.client :as redis])

    (def db (redis/init)) ;localhost
    (rmq/sub db "pubsub.test" (fn [name] (println "Hello " name)))

### produce/consume
#### queue-produce

    (:use [redis-mq.queue :as rmq]
          [clj-redis.client :as redis])

    (def db (redis/init)) ;localhost
    (rmq/produce db "queue.test" 1)

#### queue-consume

    (:use [redis-mq.queue :as rmq]
          [clj-redis.client :as redis])

    (def db (redis/init)) ;localhost

    ;will block until queue is empty then the method will exit
    (rmq/consume db "queue.test" (fn [x] (println (+ x 1))))

#### queue-consume-wait

    (:use [redis-mq.queue :as rmq]
          [clj-redis.client :as redis])

    (def db (redis/init)) ;localhost

    ;will block all the time. When a message arrives it will process
    ;it and then wait for the next one (never exits)
    ;you specify the consumption rate at the end (i.e. time to wait
    ;after processing each message)
    (rmq/consume-wait db "queue.test" (fn [x] (println (+ x 1))) 5)

## License

Copyright (C) 2012 djhworld

Distributed under the Eclipse Public License, the same as Clojure.
