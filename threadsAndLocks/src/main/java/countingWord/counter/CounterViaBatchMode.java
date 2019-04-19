package countingWord.counter;

import countingWord.WordCounter;
import countingWord.domain.Page;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class CounterViaBatchMode extends WordCounter {
    private HashMap<String, Integer> localCounts;

    public CounterViaBatchMode(BlockingQueue<Page> queue, ConcurrentMap<String, Integer> counts) {
        super(queue, counts);
        localCounts = new HashMap<>();
    }

    @Override
    public void run() {
        super.run();
        mergeCounts();
    }

    @Override
    protected void countWord(String word) {
        localCounts.merge(word, 1, (a, b) -> a + b);
    }

    private void mergeCounts() {
        for (Map.Entry<String, Integer> localEntry : localCounts.entrySet()) {
            String word = localEntry.getKey();
            Integer count = localEntry.getValue();
            ConcurrentHashMap<String, Integer> concurrentHashMap = (ConcurrentHashMap) counts;
            while (true) {
                Integer concurrentCount = concurrentHashMap.get(word);
                if (concurrentCount == null) {
                    if (concurrentHashMap.putIfAbsent(word, count) == null) {
                        break;
                    }
                } else if (counts.replace(word, concurrentCount, concurrentCount + count)) {
                    break;
                }
            }
        }
    }
}

