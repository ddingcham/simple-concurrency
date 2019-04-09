package interrupt;

import org.junit.Test;

public class InterruptibleTest {

    @Test(timeout = 7000L)
    public void can_not_interrupt_when_deadLock() throws InterruptedException {
        new Interruptible() {
            @Override
            public void run() throws InterruptedException {
                t1.start();
                t2.start();
                Thread.sleep(2000);
                t1.interrupt();
                t2.interrupt();
                t1.join();
                t2.join();
            }
        }.run();
    }

    @Test
    public void can_interrupt_when_deadLock() throws InterruptedException {
        new Interruptible() {
            @Override
            protected void initThreads() {
                t1 = new Thread(() -> {
                    try {
                        o1.lockInterruptibly();
                        Thread.sleep(1000);
                        o2.lockInterruptibly();
                    } catch (InterruptedException e) {
                        System.out.println("t1 interrupted");
                    }
                });
                t2 = new Thread(() -> {
                    try {
                        o2.lockInterruptibly();
                        Thread.sleep(1000);
                        o1.lockInterruptibly();
                    } catch (InterruptedException e) {
                        System.out.println("t2 interrupted");
                    }
                });
            }

            @Override
            public void run() throws InterruptedException {
                t1.start();
                t2.start();
                Thread.sleep(3000);

                // interrupt ordering test
                t2.interrupt();
                t1.interrupt();

                t1.join();
                t2.join();
            }
        }.run();
    }
}
