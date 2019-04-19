package countingWord.counter;

import countingWord.WordCounter;
import countingWord.domain.Page;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

public class CounterViaLock extends WordCounter {
    private final ReentrantLock lock;

    public CounterViaLock(BlockingQueue<Page> queue, Map<String, Integer> counts, ReentrantLock lock) {
        super(queue, counts);
        this.lock = lock;
    }

    public static WordCounter[] of(int numOfCounters, Map<String, Integer> resultWithSingleConsumer, ArrayBlockingQueue<Page> channel) {
        WordCounter[] counters = new WordCounter[numOfCounters];
        ReentrantLock lock = new ReentrantLock();

        for (int i = 0; i < numOfCounters; ++i) {
            counters[i] = new CounterViaLock(channel, resultWithSingleConsumer, lock);
        }

        return counters;
    }

    @Override
    protected void countWord(String word) {
        lock.lock();
        try {
            counts.merge(word, 1, (a, b) -> a + b);
        } finally {
            lock.unlock();
        }

    }
}
