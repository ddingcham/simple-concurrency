package download;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

public class DownloadTest {

    URL from;
    ProgressListener progressListener;

    @Before
    public void setUp() throws IOException {
        from = new URL("http://download.wikimedia.org/enwiki/latest/enwiki-latest-pages-articles.xml.bz2");
        progressListener = new ProgressListener() {
            @Override
            public synchronized void onProgress(int current) {
                try {
                    Thread.sleep(1000);
                } catch(InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.print("\r" + current);
                System.out.flush();
            }
        };
    }

    @Test
    public void long_lock_time_via_nonCloning() throws Exception {
        Downloader[] downloaders = new Downloader[]{
                new Downloader(from, "sample.out"),
                new Downloader(from, "sample.out"),
                new Downloader(from, "sample.out"),
                new Downloader(from, "sample.out"),
                new Downloader(from, "sample.out")
        };

        for (Downloader downloader : downloaders) {
            downloader.start();
            downloader.addListener(progressListener);
        }

        for (Downloader downloader : downloaders) {
            downloader.join();
        }
    }

    @Test
    public void short_lock_time_via_cloning() throws Exception {
        Downloader[] downloaders = new Downloader[]{
                new ShortLockDownloader(from, "sample.out"),
                new ShortLockDownloader(from, "sample.out"),
                new ShortLockDownloader(from, "sample.out"),
                new ShortLockDownloader(from, "sample.out"),
                new ShortLockDownloader(from, "sample.out")
        };

        for (Downloader downloader : downloaders) {
            downloader.start();
            downloader.addListener(progressListener);
        }

        for (Downloader downloader : downloaders) {
            downloader.join();
        }
    }

    class ShortLockDownloader extends Downloader {
        public ShortLockDownloader(URL url, String outputFileName) throws IOException {
            super(url, outputFileName);
        }

        @Override
        protected void updateProgress(int n) {
            ArrayList<ProgressListener> listenersCopy;
            synchronized (this) {
                listenersCopy = (ArrayList<ProgressListener>) listeners.clone();
            }
            for (ProgressListener listener : listenersCopy) {
                listener.onProgress(n);
            }
        }
    }

}
