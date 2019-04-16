package counting;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.atomic.AtomicInteger;

@RequiredArgsConstructor
public class CountingThread extends Thread {

    private final int numberOfCounts;
    private final Counter counter;

    @Override
    public void run() {
        for (int x = 0; x < numberOfCounts; ++x) {
            counter.increment();
        }
    }

    static class SimpleCounter implements Counter {

        @Getter
        private int count = 0;

        @Override
        public void increment() {
            ++count;
        }
    }

    static class SynchronizedCounter extends SimpleCounter {
        @Getter
        private int count = 0;

        @Override
        public synchronized void increment() {
            ++count;
        }
    }

    static class AtomicCounter implements Counter {

        private AtomicInteger count = new AtomicInteger();

        @Override
        public void increment() {
            // ++count
            count.incrementAndGet();
            // count++ : getAndIncrement
        }

        @Override
        public int getCount() {
            return count.get();
        }
    }

}
