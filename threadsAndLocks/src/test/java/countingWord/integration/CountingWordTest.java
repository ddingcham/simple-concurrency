package countingWord.integration;

import countingWord.PageParser;
import countingWord.WikiReader;
import countingWord.WordCounter;
import countingWord.XMLWikiReader;
import countingWord.domain.Page;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.Assert.assertTrue;

public class CountingWordTest {

    public static final String PATH = "enwiki-20190401-pages-articles-multistream14.xml";

    @Test
    public void report_perform() throws InterruptedException {
        int numOfConsumer = 2;
        long start = System.currentTimeMillis();
        Map resultWitMultipleConsumer = count_words_with_multiple_consumer(numOfConsumer);
        long end = System.currentTimeMillis();
        System.out.println("count_words_with_multiple_consumer(" + numOfConsumer + "): " + (end - start) + "ms");

        start = System.currentTimeMillis();
        Map resultWithSingleConsumer = count_words_with_single_consumer();
        end = System.currentTimeMillis();
        System.out.println("count_words_with_single_consumer: " + (end - start) + "ms");


        assertTrue(resultWitMultipleConsumer.equals(resultWithSingleConsumer));
    }

    public synchronized Map count_words_with_single_consumer() throws InterruptedException {
        ArrayBlockingQueue<Page> channel = new ArrayBlockingQueue<>(100);
        HashMap<String, Integer> counts = new HashMap<>();
        WikiReader reader = new XMLWikiReader(PATH);

        Thread counter = new Thread(new WordCounter(channel, counts){
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

    public synchronized Map count_words_with_multiple_consumer(final int numOfConsumers) throws InterruptedException {
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
                        counts.merge(word, 1, (a,b) -> a+b);
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
        executor.awaitTermination(5L, TimeUnit.SECONDS);

        return counts;
    }
}
