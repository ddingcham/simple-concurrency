package countingWord;

import countingWord.domain.Page;
import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.concurrent.BlockingQueue;

@RequiredArgsConstructor
public class PageParser implements Runnable {
    private final BlockingQueue<Page> queue;
    private final WikiReader reader;

    public void run() {
        Optional<Page> page;
        while((page = reader.nextPage()).isPresent()) {
             page.ifPresent(this::putPage);
        }
    }

    private void putPage(Page page) {
        try {
            queue.put(page);
        } catch(Exception interrupted) {
            interrupted.printStackTrace();
        }
    }
}
