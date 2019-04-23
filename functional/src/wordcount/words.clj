(ns wordcount.words (:gen-class))

(defn get-words [text]
  (re-seq #"\w+" text))

(def pages ["one two potato three" "four five potato six" "potato seven eight nine"])

(defn -main []
  (println pages)
  (println (time (map get-words pages)))
  (println (time (mapcat get-words pages)))
)