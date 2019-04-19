package countingWord.integration;

import countingWord.PageParser;
import countingWord.WikiReader;
import countingWord.WordCounter;
import countingWord.XMLWikiReader;
import countingWord.domain.Page;
import countingWord.domain.WordIterator;
import org.junit.Test;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

import static org.assertj.core.api.Assertions.assertThat;

public class CountingWordTest {

    public static final String PATH = "enwiki-20190401-pages-articles-multistream14.xml";

    @Test
    public void report_perform() throws InterruptedException {
        long start = System.currentTimeMillis();
        Map resultWithSingleConsumer = count_words_with_single_consumer();
        long end = System.currentTimeMillis();
        long elapsedTimeViaSingleConsumer = end - start;
        System.out.println("count_words_with_single_consumer: " + elapsedTimeViaSingleConsumer + "ms");

        int numOfConsumer = 4; // based-on-cpu-core : availableProcessor()
        start = System.currentTimeMillis();
        Map resultWitMultipleConsumer = count_words_with_multiple_consumer(numOfConsumer);
        end = System.currentTimeMillis();
        long elapsedTimeViaMultipleConsumer = end - start;
        System.out.println("count_words_with_multiple_consumer(" + numOfConsumer + "): " + elapsedTimeViaMultipleConsumer + "ms");

        start = System.currentTimeMillis();
        Map resultWitMultipleConsumerAndConcurrentCollection = count_words_with_multiple_consumer_and_concurrent_collection(numOfConsumer);
        end = System.currentTimeMillis();
        long elapsedTimeViaMultipleConsumerWithConcurrentCollection = end - start;
        System.out.println("count_words_with_multiple_consumer_and_concurrent_collection(" + numOfConsumer + "): " + elapsedTimeViaMultipleConsumerWithConcurrentCollection + "ms");

        assertThat(resultWithSingleConsumer)
                .isEqualTo(resultWitMultipleConsumer)
                .isEqualTo(resultWitMultipleConsumerAndConcurrentCollection);

    }

    private Map count_words_with_single_consumer() throws InterruptedException {
        ArrayBlockingQueue<Page> channel = new ArrayBlockingQueue<>(100);
        HashMap<String, Integer> counts = new HashMap<>();
        WikiReader reader = new XMLWikiReader(PATH);

        Thread counter = new Thread(new WordCounter(channel, counts) {
            @Override
            protected void countWord(String word) {
                counts.merge(word, 1, (a, b) -> a + b);
            }
        });
        Thread parser = new Thread(new PageParser(channel, reader));

        counter.start();
        parser.start();
        parser.join();
        channel.put(Page.POISON_PILL);
        counter.join();

        return counts;
    }

    private Map count_words_with_multiple_consumer(final int numOfConsumers) throws InterruptedException {
        ArrayBlockingQueue<Page> channel = new ArrayBlockingQueue<>(100);
        HashMap<String, Integer> counts = new HashMap<>();
        WikiReader reader = new XMLWikiReader(PATH);
        ExecutorService executor = Executors.newCachedThreadPool();
        ReentrantLock lock = new ReentrantLock();

        for (int i = 0; i < numOfConsumers; ++i) {
            executor.execute(new WordCounter(channel, counts) {
                ReentrantLock shared = lock;

                @Override
                protected void countWord(String word) {
                    shared.lock();
                    try {
                        counts.merge(word, 1, (a, b) -> a + b);
                    } finally {
                        shared.unlock();
                    }

                }
            });
        }
        Thread parser = new Thread(new PageParser(channel, reader));

        parser.start();
        parser.join();
        for (int i = 0; i < numOfConsumers; ++i) {
            channel.put(Page.POISON_PILL);
        }
        executor.shutdown();
        executor.awaitTermination(10L, TimeUnit.SECONDS);

        return counts;
    }

    private Map count_words_with_multiple_consumer_and_concurrent_collection(final int numOfConsumers) throws InterruptedException {
        ArrayBlockingQueue<Page> channel = new ArrayBlockingQueue<>(100);
        Map<String, Integer> counts = new ConcurrentHashMap<>();
        WikiReader reader = new XMLWikiReader(PATH);
        ExecutorService executor = Executors.newCachedThreadPool();

        for (int i = 0; i < numOfConsumers; ++i) {
            executor.execute(new WordCounter(channel, counts) {
                @Override
                protected void countWord(String word) {
                    ConcurrentHashMap<String, Integer> concurrentHashMap = (ConcurrentHashMap) counts;
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
            });
        }
        Thread parser = new Thread(new PageParser(channel, reader));

        parser.start();
        parser.join();
        for (int i = 0; i < numOfConsumers; ++i) {
            channel.put(Page.POISON_PILL);
        }
        executor.shutdown();
        executor.awaitTermination(7L, TimeUnit.SECONDS);

        return counts;
    }
}
