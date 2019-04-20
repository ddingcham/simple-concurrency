package countingWord.counter;

import countingWord.WordCounter;
import countingWord.domain.Page;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

public class WordCounters implements Iterable<WordCounter> {
    private final WordCounter[] counters;

    public static WordCountersBuilder builder() {
        return new WordCountersBuilder();
    }

    private WordCounters(WordCounter[] counters) {
        this.counters = counters;
    }

    @Override
    public void forEach(Consumer<? super WordCounter> action) {
        Iterator<WordCounter> iterator = iterator();
        while(iterator.hasNext()) {
            action.accept(iterator.next());
        }
    }

    @Override
    public Spliterator<WordCounter> spliterator() {
        return Arrays.spliterator(counters);
    }

    @Override
    public Iterator<WordCounter> iterator() {
        return Arrays.stream(counters).iterator();
    }

    public static class WordCountersBuilder {
        private BlockingQueue<Page> channel;
        private Map<String, Integer> counts;
        private int numOfWordCounters;

        public WordCountersBuilder() {
            this.channel = new ArrayBlockingQueue<>(100);
            this.counts = new HashMap<>();
            this.numOfWordCounters = 1;
        }

        public WordCountersBuilder with(BlockingQueue<Page> channel) {
            if (channel == null) {
                throw new IllegalArgumentException("Channel can't be null");
            }
            this.channel = channel;
            return this;
        }

        public WordCountersBuilder with(Map<String, Integer> counts) {
            if (counts == null) {
                throw new IllegalArgumentException("Counts can't be null");
            }
            this.counts = counts;
            return this;
        }

        public WordCountersBuilder of(int numOfWordCounters) {
            if (numOfWordCounters < 0) {
                throw new IllegalArgumentException("numOfWordCounters must be more than 1");
            }
            this.numOfWordCounters = numOfWordCounters;
            return this;
        }

        public WordCounters buildWithLock() {
            ReentrantLock lock = new ReentrantLock();
            CounterViaLock[] counters = new CounterViaLock[numOfWordCounters];
            for(int i = 0; i < numOfWordCounters; ++i) {
                counters[i] = new CounterViaLock(channel, counts, lock);
            }
            return new WordCounters(counters);
        }

        public WordCounters buildWithConcurrentCollection() {
            if (isUnSupportedConcurrentCollection()) {
                throw new IllegalStateException("counts should implement ConcurrentCollection");
            }
            CounterViaConcurrentCollection[] counters = new CounterViaConcurrentCollection[numOfWordCounters];
            for(int i = 0; i < numOfWordCounters; ++i) {
                counters[i] = new CounterViaConcurrentCollection(channel, counts);
            }
            return new WordCounters(counters);
        }

        public WordCounters buildWithBatchMode() {
            if (isUnSupportedConcurrentCollection()) {
                throw new IllegalStateException("counts should implement ConcurrentCollection");
            }
            CounterViaBatchMode[] counters = new CounterViaBatchMode[numOfWordCounters];
            for(int i = 0; i< numOfWordCounters; ++i) {
                counters[i] = new CounterViaBatchMode(channel, counts);
            }
            return new WordCounters(counters);
        }

        private boolean isUnSupportedConcurrentCollection() {
            return !(counts instanceof ConcurrentMap);
        }

    }
}
