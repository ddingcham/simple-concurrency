package countingWord.counter;

import countingWord.WordCounter;
import countingWord.parser.Page;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

public class CounterViaLock extends WordCounter {
    private final ReentrantLock lock;

    CounterViaLock(BlockingQueue<Page> queue, Map<String, Integer> counts, ReentrantLock lock) {
        super(queue, counts);
        this.lock = lock;
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
