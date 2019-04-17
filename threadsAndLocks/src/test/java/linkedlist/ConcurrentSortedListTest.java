package linkedlist;

import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.*;

public class ConcurrentSortedListTest {

    private static final int DEFAULT_NUMBER_OF_OPERATIONS = 10000;

    @Test
    public void simple_run_with_threads_via_reentrantLock() {

        final ReentrantLockableConcurrentSortedList<Integer> list = new ReentrantLockableConcurrentSortedList<>();
        final int numberOfThreads = 3;


        runTestThreads(list, numberOfThreads, DEFAULT_NUMBER_OF_OPERATIONS);

        assertEquals(DEFAULT_NUMBER_OF_OPERATIONS * numberOfThreads, list.size());
        assertTrue(list.isSorted());
    }

    @Test
    public void simple_run_with_threads_via_single_lock() {

        final SingleLockConcurrentSortedList<Integer> list = new SingleLockConcurrentSortedList<>();
        final int numberOfThreads = 3;

        runTestThreads(list, numberOfThreads, DEFAULT_NUMBER_OF_OPERATIONS);

        assertEquals(DEFAULT_NUMBER_OF_OPERATIONS * numberOfThreads, list.size());
        assertTrue(list.isSorted());
    }

    /*
        (sample)
        *******Depends on NumberOfThreads(5) & NumberOfTasks(10000)*******
     */
    @Test
    public void compare_performance_with_threads_5_tasks_10000() {
        final ReentrantLockableConcurrentSortedList<Integer> reentrantLock = new ReentrantLockableConcurrentSortedList<>();
        final SingleLockConcurrentSortedList<Integer> singleLock = new SingleLockConcurrentSortedList<>();
        final int numberOfThreads = 5;

        long startReentrantLock = System.currentTimeMillis();
        runTestThreads(reentrantLock, numberOfThreads, DEFAULT_NUMBER_OF_OPERATIONS);
        long endReentrantLock = System.currentTimeMillis();

        long startSingleLock = System.currentTimeMillis();
        runTestThreads(singleLock, numberOfThreads, DEFAULT_NUMBER_OF_OPERATIONS);
        long endSingleLock = System.currentTimeMillis();

        System.out.println("runWithReentrantLock : " + (endReentrantLock - startReentrantLock));
        System.out.println("runWithSingleLock : " + (endSingleLock - startSingleLock));

        assertTrue(endReentrantLock - startReentrantLock < endSingleLock - startSingleLock);
    }

    /*
        (sample)
        *******Depends on NumberOfThreads(5) & NumberOfTasks(100)*******
     */
    @Test
    public void compare_performance_with_threads_5_tasks_100() {
        final ReentrantLockableConcurrentSortedList<Integer> reentrantLock = new ReentrantLockableConcurrentSortedList<>();
        final SingleLockConcurrentSortedList<Integer> singleLock = new SingleLockConcurrentSortedList<>();
        final int numberOfThreads = 5;
        final int numberOfOperations = 100;

        long startReentrantLock = System.currentTimeMillis();
        runTestThreads(reentrantLock, numberOfThreads, numberOfOperations);
        long endReentrantLock = System.currentTimeMillis();

        long startSingleLock = System.currentTimeMillis();
        runTestThreads(singleLock, numberOfThreads, numberOfOperations);
        long endSingleLock = System.currentTimeMillis();

        System.out.println("runWithReentrantLock : " + (endReentrantLock - startReentrantLock));
        System.out.println("runWithSingleLock : " + (endSingleLock - startSingleLock));

        assertTrue(endReentrantLock - startReentrantLock > endSingleLock - startSingleLock);

    }

    private void runTestThreads(Insertable target, int numberOfThreads, int numberOfOperations) {
        Thread[] threads = new Thread[numberOfThreads];
        try {
            for (int i = 0; i < numberOfThreads; i++) {
                threads[i] = new TestThread(target, numberOfOperations);
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
        private final int numberOfOperations;

        public TestThread(Insertable<Integer> insertable, int numberOfOperations) {
            this.insertable = insertable;
            this.numberOfOperations = numberOfOperations;
        }

        public void run() {
            for (int i = 0; i < numberOfOperations; ++i) {
                insertable.insert(RANDOM.nextInt());
            }
        }
    }
}
