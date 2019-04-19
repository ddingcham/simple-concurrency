package countingWord.counter;

import countingWord.WordCounter;
import countingWord.domain.Page;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentMap;

public class CounterViaConcurrentCollection extends WordCounter {

    public CounterViaConcurrentCollection(BlockingQueue<Page> queue, ConcurrentMap<String, Integer> counts) {
        super(queue, counts);
    }

    @Override
    protected void countWord(String word) {
        ConcurrentMap<String, Integer> concurrentHashMap = (ConcurrentMap) counts;
        while (true) {
            Integer concurrentCount = concurrentHashMap.get(word);
            if (concurrentCount == null) {
                if (concurrentHashMap.putIfAbsent(word, 1) == null) {
                    break;
                }
            } else if (counts.replace(word, concurrentCount, concurrentCount + 1)) {
                break;
            }
        }
    }
}
