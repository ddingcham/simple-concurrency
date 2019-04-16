package linkedlist;

import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.*;

public class ReentrantLockableConcurrentSortedListTest {

    private static final int NUMBER_OF_OPERATIONS = 10000;

    @Test
    public void simple_run_with_threads_via_reentrantLock() {

        final ReentrantLockableConcurrentSortedList<Integer> list = new ReentrantLockableConcurrentSortedList<>();
        final int numberOfThreads = 3;


        runTestThreads(list, numberOfThreads);

        assertEquals(NUMBER_OF_OPERATIONS * numberOfThreads, list.size());
        assertTrue(list.isSorted());
    }

    @Test
    public void simple_run_with_threads_via_single_lock() {

        final SingleLockConcurrentSortedList<Integer> list = new SingleLockConcurrentSortedList<>();
        final int numberOfThreads = 3;

        runTestThreads(list, numberOfThreads);

        assertEquals(NUMBER_OF_OPERATIONS * numberOfThreads, list.size());
        assertTrue(list.isSorted());
    }

    /*
        (default)
        *******Depends on NumberOfThreads(5) & NumberOfTasks(10000)*******
     */
    @Test
    public void compare_performance() {
        final ReentrantLockableConcurrentSortedList<Integer> reentrantLock = new ReentrantLockableConcurrentSortedList<>();
        final SingleLockConcurrentSortedList<Integer> singleLock = new SingleLockConcurrentSortedList<>();
        final int numberOfThreads = 5;

        long startReentrantLock = System.currentTimeMillis();
        runTestThreads(reentrantLock, numberOfThreads);
        long endReentrantLock = System.currentTimeMillis();

        long startSingleLock = System.currentTimeMillis();
        runTestThreads(singleLock, numberOfThreads);
        long endSingleLock = System.currentTimeMillis();

        System.out.println("runWithReentrantLock : " + (endReentrantLock - startReentrantLock));
        System.out.println("runWithSingleLock : " + (endSingleLock - startSingleLock));

        assertTrue(endReentrantLock - startReentrantLock < endSingleLock - startSingleLock);
    }

    private void runTestThreads(Insertable target, int numberOfThreads) {
        Thread[] threads = new Thread[numberOfThreads];
        try {
            for (int i = 0; i < numberOfThreads; i++) {
                threads[i] = new TestThread(target);
                threads[i].start();
            }
            for (Thread thread : threads) {
                thread.join();
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    static class TestThread extends Thread {
        private static final Random RANDOM = new Random();
        private final Insertable<Integer> insertable;

        public TestThread(Insertable<Integer> insertable) {
            this.insertable = insertable;
        }

        public void run() {
            for (int i = 0; i < NUMBER_OF_OPERATIONS; ++i) {
                insertable.insert(RANDOM.nextInt());
            }
        }
    }
}
