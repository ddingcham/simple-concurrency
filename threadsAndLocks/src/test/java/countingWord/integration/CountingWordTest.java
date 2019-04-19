package countingWord.integration;

import countingWord.PageParser;
import countingWord.WikiReader;
import countingWord.WordCounter;
import countingWord.XMLWikiReader;
import countingWord.counter.CounterViaBatchMode;
import countingWord.counter.CounterViaConcurrentCollection;
import countingWord.counter.CounterViaLock;
import countingWord.domain.Page;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

public class CountingWordTest {

    public static final String PATH = "enwiki-20190401-pages-articles-multistream14.xml";

    @Test
    public void report_perform() throws Exception {
        ArrayBlockingQueue<Page> channel = new ArrayBlockingQueue<>(100);
        long start = System.currentTimeMillis();
        Map<String, Integer> resultWithSingleConsumer = new HashMap<>();
        count_words_with_multiple_consumers(
                CounterViaLock.of(1, resultWithSingleConsumer, channel), channel);
        long end = System.currentTimeMillis();
        long elapsedTimeViaSingleConsumer = end - start;
        System.out.println("count_words_with_single_consumer: " + elapsedTimeViaSingleConsumer + "ms");
        synchronized (channel) {
            channel.clear();
        }

        int numOfConsumer = 4; // based-on-cpu-core : availableProcessor()
        start = System.currentTimeMillis();
        Map<String, Integer> resultWitMultipleConsumer = new HashMap<>();
        count_words_with_multiple_consumers(
                CounterViaLock.of(1, resultWithSingleConsumer, channel), channel);
        end = System.currentTimeMillis();
        long elapsedTimeViaMultipleConsumer = end - start;
        System.out.println("count_words_with_multiple_consumer(" + numOfConsumer + "): " + elapsedTimeViaMultipleConsumer + "ms");
        synchronized (channel) {
            channel.clear();
        }

        start = System.currentTimeMillis();
        Map<String, Integer> resultWitMultipleConsumerAndConcurrentCollection = new ConcurrentHashMap<>();
        count_words_with_multiple_consumers(
                generateCounters(numOfConsumer, CounterViaConcurrentCollection.class, resultWitMultipleConsumerAndConcurrentCollection, channel), channel);
        end = System.currentTimeMillis();
        long elapsedTimeViaMultipleConsumerWithConcurrentCollection = end - start;
        System.out.println("count_words_with_multiple_consumer_and_concurrent_collection(" + numOfConsumer + "): " + elapsedTimeViaMultipleConsumerWithConcurrentCollection + "ms");
        synchronized (channel) {
            channel.clear();
        }

        start = System.currentTimeMillis();
        Map<String, Integer> resultWitMultipleConsumerAndBatchModel = new ConcurrentHashMap<>();
        count_words_with_multiple_consumers(
                generateCounters(numOfConsumer, CounterViaBatchMode.class, resultWitMultipleConsumerAndBatchModel, channel), channel);
        end = System.currentTimeMillis();
        long elapsedTimeViaMultipleConsumerWithBatchModel = end - start;
        System.out.println("count_words_with_multiple_consumer_and_batch_model(" + numOfConsumer + "): " + elapsedTimeViaMultipleConsumerWithBatchModel + "ms");
        synchronized (channel) {
            channel.clear();
        }

        assertThat(resultWithSingleConsumer)
                .isEqualTo(resultWitMultipleConsumer)
                .isEqualTo(resultWitMultipleConsumerAndConcurrentCollection)
                .isEqualTo(resultWitMultipleConsumerAndBatchModel);
    }

    private void count_words_with_multiple_consumers(WordCounter[] counters, ArrayBlockingQueue<Page> channel) throws InterruptedException {
        WikiReader reader = new XMLWikiReader(PATH);
        ExecutorService executor = Executors.newCachedThreadPool();

        for (WordCounter counter : counters) {
            executor.execute(counter);
        }

        Thread parser = new Thread(new PageParser(channel, reader));
        parser.start();
        parser.join();
        channel.put(Page.POISON_PILL);
        executor.shutdown();
        executor.awaitTermination(10L, TimeUnit.SECONDS);
    }

    private WordCounter[] generateCounters(int numOfConsumer, Class<? extends WordCounter> counterType, Map<String, Integer> counts, ArrayBlockingQueue<Page> channel) throws Exception {
        WordCounter[] counters = new WordCounter[numOfConsumer];
        Constructor<? extends WordCounter> constructor = counterType.getConstructor(BlockingQueue.class, ConcurrentMap.class);

        for (int i = 0; i < numOfConsumer; ++i) {
            counters[i] = constructor.newInstance(channel, counts);
        }

        return counters;
    }
}
