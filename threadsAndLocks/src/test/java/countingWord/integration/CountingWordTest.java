package countingWord.integration;

import countingWord.PageParser;
import countingWord.WikiReader;
import countingWord.WordCounter;
import countingWord.XMLWikiReader;
import countingWord.counter.WordCounters;
import countingWord.domain.Page;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

public class CountingWordTest {

    public static final String PATH = "enwiki-20190401-pages-articles-multistream14.xml";
    BlockingQueue<Page> channel = new ArrayBlockingQueue<>(100);

    @Test
    public void report_perform() {
        Map<String, Integer> resultWithSingleConsumer = new HashMap();
        System.out.println("<count_words_with_single_consumer>");
        runWithMarkingElapsedTime(
                WordCounters.builder()
                        .with(channel)
                        .with(resultWithSingleConsumer)
                        .buildWithLock()
                , channel);

        int numOfConsumer = 4; // based-on-cpu-core : availableProcessor()

        Map<String, Integer> resultWitMultipleConsumer = new HashMap<>();
        channel.clear();
        System.out.println("<count_words_with_multiple_consumer(" + numOfConsumer + ")>");
        runWithMarkingElapsedTime(
                WordCounters.builder()
                        .with(channel)
                        .with(resultWitMultipleConsumer)
                        .of(numOfConsumer)
                        .buildWithLock()
                , channel);


        Map<String, Integer> resultWitMultipleConsumerAndConcurrentCollection = new ConcurrentHashMap<>();
        channel.clear();
        System.out.println("<count_words_with_multiple_consumer_and_concurrent_collection(" + numOfConsumer + ")> ");
        runWithMarkingElapsedTime(
                WordCounters.builder()
                        .with(channel)
                        .with(resultWitMultipleConsumerAndConcurrentCollection)
                        .of(numOfConsumer)
                        .buildWithConcurrentCollection()
                , channel);

        Map<String, Integer> resultWitMultipleConsumerAndBatchModel = new ConcurrentHashMap<>();
        channel.clear();
        System.out.println("count_words_with_multiple_consumer_and_batch_model(" + numOfConsumer + ")>");
        runWithMarkingElapsedTime(
                WordCounters.builder()
                        .with(channel)
                        .with(resultWitMultipleConsumerAndBatchModel)
                        .of(numOfConsumer)
                        .buildWithBatchMode()
                , channel);

        assertThat(resultWithSingleConsumer)
                .isEqualTo(resultWitMultipleConsumer)
                .isEqualTo(resultWitMultipleConsumerAndConcurrentCollection)
                .isEqualTo(resultWitMultipleConsumerAndBatchModel);
    }

    private void runWithMarkingElapsedTime(WordCounters counters, BlockingQueue<Page> channel) {
        long start = System.currentTimeMillis();
        WikiReader reader = new XMLWikiReader(PATH);
        try {
            executes(counters, new PageParser(channel, reader));
        } catch (InterruptedException ignore) {
            ignore.printStackTrace();
            fail();
        }
        long end = System.currentTimeMillis();
        long elapsedTime = end - start;
        System.out.println("elapsedTime : " + elapsedTime + "ms");
    }

    private void executes(WordCounters counters, PageParser parser) throws InterruptedException {
        ExecutorService executor = Executors.newCachedThreadPool();
        for(WordCounter counter : counters) {
            executor.execute(counter);
        }
        Thread parserThread = new Thread(parser);
        parserThread.start();
        parserThread.join();
        executor.shutdown();
        executor.awaitTermination(10L, TimeUnit.SECONDS);
    }
}
