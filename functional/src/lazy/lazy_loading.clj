(ns lazy.lazy-loading
  (:gen-class))

(defn -main []
  (time (take 10 (range 0 100000000)))
  (time (take 10 (iterate inc 0)))
  (time (take 10 (iterate (partial + 2) 0)))
  (time (take-last 5 (range 0 100000000)))                  ; no waste memory
)
