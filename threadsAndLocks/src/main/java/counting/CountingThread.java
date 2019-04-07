package counting;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class CountingThread extends Thread {

    public static final int COUNT_CONSTANT = 10000;
    Counter counter;

    @Override
    public void run() {
        for(int x = 0; x < COUNT_CONSTANT; ++x) {
            counter.increment();
        }
    }
}
