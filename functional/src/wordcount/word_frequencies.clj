(ns wordcount.word_frequencies (:gen-class))

(defn word-frequencies [words, implementor]
  (implementor words)
)

(defn frequency-reduce [words]
  (reduce
    (fn [counts word] (assoc counts word (inc (get counts word 0))))
    {} words))

(defn frequency-frequencies [words]
  (frequencies words))

(def words ["one" "potato" "two" "potato" "three" "potato" "four"])

(defn -main []
  (println (time (word-frequencies words, frequency-reduce)))
  (println (time (word-frequencies words, frequency-frequencies)))
)