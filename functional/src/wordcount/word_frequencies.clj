(ns wordcount.word_frequencies (:gen-class))

(defn word-frequencies [words]
  (reduce
    (fn [counts word] (assoc counts word (inc (get counts word 0))))
    {} words))

(defn -main []
  (println (word-frequencies ["one", "potato", "two", "potato", "three", "potato", "four"]))
  (println (frequencies ["one", "potato", "two", "potato", "three", "potato", "four"]))
)