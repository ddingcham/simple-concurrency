(ns wordcount.count_words_sequential
  (:gen-class)
  (:require [wordcount.words :as words]
            [wordcount.word_frequencies :as freq]))

(defn wordcount [pages] (freq/word-frequencies (mapcat words/get-words pages), freq/frequency-reduce))

(defn -main []
  (println (time (wordcount words/pages)))
)