package linkedlist;

import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.*;

public class ConcurrentSortedListTest {

    @Test
    public void simple_run_with_threads() throws InterruptedException {

        final ConcurrentSortedList<Integer> list = new ConcurrentSortedList<>();
        final Random random = new Random();

        class TestThread extends Thread {
            public void run() {
                for (int i = 0; i < 10; ++i) {
                    list.insert(random.nextInt());
                }
            }
        }

        class CountingThread extends Thread {
            public void run() {
                while (!interrupted()) {
                    System.out.print("\r" + list.size());
                    System.out.flush();
                }
            }
        }

        Thread t1 = new TestThread();
        Thread t2 = new TestThread();
        Thread t3 = new CountingThread();

        t1.start();
        t2.start();
        t3.start();

        t1.join();
        t2.join();
        t3.interrupt();

        System.out.println("\r" + list.size());

        assertSame(20, list.size());
        assertTrue(list.isSorted());
    }
}
