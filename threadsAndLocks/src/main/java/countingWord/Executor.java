package countingWord;

import countingWord.counter.WordCounters;
import countingWord.parser.Page;
import countingWord.parser.PageParser;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Executor {

    private long timeout;
    private TimeUnit timeUnit;

    public Executor(long timeout, TimeUnit timeUnit) {
        this.timeout = timeout;
        this.timeUnit = timeUnit;
    }

    public void execute(BlockingQueue<Page> channel, WordCounters counters, WikiReader reader) throws InterruptedException {
        ExecutorService executor = Executors.newCachedThreadPool();
        for(WordCounter counter : counters) {
            executor.execute(counter);
        }
        Thread parserThread = new Thread(new PageParser(channel, reader));
        parserThread.start();
        parserThread.join();
        executor.shutdown();
        executor.awaitTermination(timeout, timeUnit);
    }
}
