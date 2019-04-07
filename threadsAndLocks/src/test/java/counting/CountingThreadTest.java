package counting;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class CountingThreadTest {

    @Test
    public void no_count_lock_with_couple_of_Threads() throws InterruptedException {
        Counter counter = new Counter();
        CountingThread t1 = new CountingThread(counter);
        CountingThread t2 = new CountingThread(counter);

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        assertNotEquals(CountingThread.COUNT_CONSTANT * 2, counter.getCount());
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
    public void count_lock_with_couple_of_Threads() throws InterruptedException {
        Counter counter = new SynchronizedCounter();
        CountingThread t1 = new CountingThread(counter);
        CountingThread t2 = new CountingThread(counter);

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        assertEquals(CountingThread.COUNT_CONSTANT * 2, counter.getCount());
    }

}
