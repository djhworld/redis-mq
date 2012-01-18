# redis-mq

A small collection of utilities to deploy message queue like functionality into your Clojure applications.

You will need an installation of [redis](http://redis.io) running to use this library.

## Justification

By no means is this a production ready product, or even that well coded, but I felt tools like the excellent [RabbitMQ](http://www.rabbitmq.com/) were too complex for small projects like the ones I tend to make.

So I made this as a compromise. It just allows you to get things up so you can prototype Clojure applications with pubsub/queue like functionality before dropping in more robust messaging solution.

## Installation 

Depend on `[redis-mq "0.0.3"]` in your `project.clj`

## Usage

### pub/sub
#### publisher
Drop this into your application to publish messages to a queue.

```clj
(:use [redis-mq.pubsub :as rmq])

(def db "redis://localhost:6379")

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
  (println "Hello " name + "!"))

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

## Errors

If an exception is thrown during message processing (on both 
`consumers` and `subscribers`) then the message and error message will be
put as a JSON message onto the queue `<queuename.error>`. E.g. for the
queue `queue.test`, the error queue will be called `queue.test.error` 

## Final note

This library is just a utility library that has been quite useful for
me in some of my apps. The actual legwork is being done by the
excellent [clj-redis](https://github.com/djhworld/clj-redis) library
which is a wrapper for [Jedis](https://github.com/xetorthio/jedis)

## License

Copyright (C) 2012 djhworld

Permission is hereby granted, free of charge, to any person obtaining
a copy of this software and associated documentation files (the
"Software"), to deal in the Software without restriction, including
without limitation the rights to use, copy, modify, merge, publish,
distribute, sublicense, and/or sell copies of the Software, and to
permit persons to whom the Software is furnished to do so, subject to
the following conditions:

The above copyright notice and this permission notice shall be
included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.iCopyright (c)
year copyright holders
