package download;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;

public class Downloader extends Thread {

    private InputStream in;
    private OutputStream out;
    protected ArrayList<ProgressListener> listeners;

    public Downloader(URL url, String outputFileName) throws IOException {
        in = url.openConnection().getInputStream();
        out = new FileOutputStream(outputFileName);
        listeners = new ArrayList<>();
    }

    public synchronized void addListener(ProgressListener listener) {
        listeners.add(listener);
    }

    public synchronized void removeListener(ProgressListener listener) {
        listeners.remove(listener);
    }

    protected synchronized void updateProgress(int n) {
        Collections.shuffle(listeners);
        for (ProgressListener listener : listeners) {
            listener.onProgress(n);
        }
    }

    public void run() {
        int n = 0;
        int total = 0;
        byte[] buffer = new byte[1024];

        try {
            while((n = in.read(buffer)) != -1) {
                out.write(buffer, 0, n);
                total += n;
                updateProgress(total);
            }
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
