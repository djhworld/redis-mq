# redis-mq

A small collection of utilities to deploy message queue like functionality into your Clojure applications.

You will need an installation of [redis](http://redis.io) running to use this library.

## Justification

By no means is this a production ready product, or even that well coded, but I felt tools like the excellent [RabbitMQ](http://www.rabbitmq.com/) were too complex for small projects like the ones I tend to make.

So I made this as a compromise. It just allows you to get things up so you can prototype Clojure applications with pubsub/queue like functionality before dropping in more robust messaging solution.

## Usage

### pub/sub
#### publisher
Drop this into your application to publish messages to a queue.

```clj
(:use [redis-mq.pubsub :as rmq])
(def db (redis/init)) ;localhost
(rmq/pub db "pubsub.test" "daniel")
```

#### subscriber
This subscribes to a channel and forwards received messages to a dispatch function.

> **A note about dispatch functions**
>
> When a message is received it will send the contents of the mesasge as
> the first argument to a given dispatch function, e.g. `(fn [message-body] (println message-body))`

```clj
(:use [redis-mq.pubsub :as rmq])

(def db "redis://localhost:6379")

(defn say-hello [name]
  (println "Hello " name))

(rmq/sub db "pubsub.test" say-hello))
```

### produce/consume
#### queue-produce

```clj
(:use [redis-mq.pubsub :as rmq])

(def db "redis://localhost:6379")
(rmq/produce db "queue.test" 1)
(rmq/produce db "queue.test" 2)
(rmq/produce db "queue.test" 3)
```

#### queue-consume

```clj
(:use [redis-mq.pubsub :as rmq])

(def db "redis://localhost:6379")

;message process fn
(defn double-print [x] (println (* x 2)))

;will consume until queue is empty then the method will exit.
;the optional flag :consumption-rate tells the consumer to wait 2
;seconds between each message
(rmq/consume db "queue.test" double-print :consumption-rate 2)
```

#### queue-consume-wait

```clj
(:use [redis-mq.pubsub :as rmq])

(def db "redis://localhost:6379")

;message process fn
(defn double-print [x] (println (* x 2)))

;will block all the time. When a message arrives it will process
;it and then wait for the next one (never exits). 
;the optional flag :consumption-rate tells the consumer to wait 2
;seconds between each message
(rmq/consume-wait db "queue.test" double-print :consumption-rate 2)
```

## License

Copyright (C) 2012 djhworld

Distributed under the Eclipse Public License, the same as Clojure.
