package countingWord;

import countingWord.parser.Page;
import countingWord.parser.WordIterator;
import lombok.RequiredArgsConstructor;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

@RequiredArgsConstructor
public abstract class WordCounter implements Runnable {

    private final BlockingQueue<Page> queue;
    protected final Map<String, Integer> counts;

    @Override
    public void run() {
        try {
            while (true) {
                Page page = queue.take();
                if (page == Page.POISON_PILL) {
                    queue.put(Page.POISON_PILL);
                    break;
                }

                Iterator<String> words = new WordIterator(page.getText());
                while (words.hasNext()) {
                    countWord(words.next());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected abstract void countWord(String word);
}
