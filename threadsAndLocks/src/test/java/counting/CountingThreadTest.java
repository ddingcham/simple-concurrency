package counting;

import counting.CountingThread.*;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class CountingThreadTest {

    @Test
    public void no_count_lock_with_couple_of_threads() throws InterruptedException {
        Counter counter = new SimpleCounter();
        int numberOfCounts = 100000;
        CountingThread t1 = new CountingThread(numberOfCounts, counter);
        CountingThread t2 = new CountingThread(numberOfCounts, counter);

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        assertNotEquals(numberOfCounts * 2, counter.getCount());
        /*
            race condition
            * byte code
              getfield #2
              iconst_1
              iadd
              putfield #2
            * scenario
              (field == 1)
              t1.getfield -> t2.getfield -> t1.putfield(field + 1) -> t2.putfield(field + 1)
              (field == 2, field != 3)
            * scenario -> non-determinant
         */
    }

    @Test
    public void count_lock_with_couple_of_threads() throws InterruptedException {
        Counter counter = new SynchronizedCounter();
        int numberOfCounts = 10000;
        CountingThread t1 = new CountingThread(numberOfCounts, counter);
        CountingThread t2 = new CountingThread(numberOfCounts, counter);

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        assertEquals(numberOfCounts * 2, counter.getCount());
    }

    @Test
    public void atomic_count_with_couple_of_threads() throws InterruptedException {
        Counter counter = new AtomicCounter();
        int numberOfCounts = 10000;
        CountingThread t1 = new CountingThread(numberOfCounts, counter);
        CountingThread t2 = new CountingThread(numberOfCounts, counter);

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        assertEquals(numberOfCounts * 2, counter.getCount());
    }
}
