(ns simulator.log
  (:require [simulator.config]))

#_(defn append-prefix
  [prefix text]
  (str prefix text))

#_(defn append-postfix
  [])


(defn debug
  [& texts]
  (spit "simulator.log" 
        (str "[DEBUG]" (apply str texts) "\n")))

(comment 
  (concat '(1 2 3 4) '(3))
  )