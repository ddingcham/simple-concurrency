(ns parallelSum.core (:gen-class)
  (:require [clojure.core.reducers :as r]))

(defn sum [numbers]
  (reduce + numbers))

(defn parallel-sum [numbers]
  (r/fold + numbers))

(def numbers (into [] (range 0 10000000)))

(defn -main []
  (println (time (sum numbers)))
  (println (time (sum numbers)))
  (println (time (parallel-sum numbers)))
  (println (time (parallel-sum numbers)))
)