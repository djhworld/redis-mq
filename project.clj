(defproject redis-mq "0.0.2"
  :description "Redis Message Queue Utilities"
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [clj-redis "0.0.12"]
                 [clj-time "0.3.2"]
                 [cheshire "2.0.2"]
                 [org.clojure/tools.logging "0.2.3"]
                 [log4j "1.2.16" :exclusions [javax.mail/mail
                                              javax.jms/jms
                                              com.sun.jdmk/jmxtools
                                              com.sun.jmx/jmxri]]])
