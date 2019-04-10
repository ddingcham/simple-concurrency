package philosopher;

import org.junit.Before;
import org.junit.Test;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class PhilosopherTest {

    Chopstick left;
    Chopstick right;

    @Before
    public void setUp() {
        left = new Chopstick(1);
        right = new Chopstick(2);
    }

    @Test
    public void deadLock() throws InterruptedException {
        Philosopher philosopher1 = new Philosopher(left, right);
        Philosopher philosopher2 = new Philosopher(right, left);
        Philosopher philosopher3 = new Philosopher(left, right);
        Philosopher philosopher4 = new Philosopher(right, left);
        Philosopher philosopher5 = new Philosopher(left, right);

        philosopher1.start();
        philosopher2.start();
        philosopher3.start();
        philosopher4.start();
        philosopher5.start();
        philosopher1.join();
        philosopher2.join();
        philosopher3.join();
        philosopher4.join();
        philosopher5.join();
    }

    @Test
    public void deadLock_with_num_of_threads() throws InterruptedException {
        final int numOfPhilosophers = 4;
        Philosopher[] philosophers = new Philosopher[numOfPhilosophers];
        for (int i = 0; i < numOfPhilosophers; i++) {
            if (i < numOfPhilosophers / 2) {
                philosophers[i] = new Philosopher(left, right);
            } else {
                philosophers[i] = new Philosopher(right, left);
            }
        }
        for (Philosopher philosopher : philosophers) {
            philosopher.start();
        }
        for (Philosopher philosopher : philosophers) {
            philosopher.join();
        }
    }

    @Test
    public void resolve_deadLock_with_global_lock_ordering_rule() throws InterruptedException {
        class ResolvingDeadLockViaGlobalOrderingRule extends Philosopher {
            public ResolvingDeadLockViaGlobalOrderingRule(Chopstick left, Chopstick right) {
                super(left, right);
            }

            @Override
            protected void init(Chopstick left, Chopstick right) {
                if (left.getId() < right.getId()) {
                    setFirst(left);
                    setSecond(right);
                } else {
                    setFirst(right);
                    setSecond(left);
                }
            }
        }
        Philosopher philosopher1 = new ResolvingDeadLockViaGlobalOrderingRule(left, right);
        Philosopher philosopher2 = new ResolvingDeadLockViaGlobalOrderingRule(right, left);
        Philosopher philosopher3 = new ResolvingDeadLockViaGlobalOrderingRule(left, right);
        Philosopher philosopher4 = new ResolvingDeadLockViaGlobalOrderingRule(right, left);
        Philosopher philosopher5 = new ResolvingDeadLockViaGlobalOrderingRule(left, right);

        philosopher1.start();
        philosopher2.start();
        philosopher3.start();
        philosopher4.start();
        philosopher5.start();
        philosopher1.join();
        philosopher2.join();
        philosopher3.join();
        philosopher4.join();
        philosopher5.join();
    }

    @Test
    public void resolve_deadLock_with_timeout() throws InterruptedException {
        class ResolvingDeadLockViaTimeout extends Thread {
            private ReentrantLock left;
            private ReentrantLock right;
            private Random random;

            public ResolvingDeadLockViaTimeout(ReentrantLock left, ReentrantLock right) {
                this.left = left;
                this.right = right;
                this.random = new Random();
            }

            @Override
            public void run() {
                try {
                    while (true) {
                        Thread.sleep(random.nextInt(1000));
                        left.lock();
                        try {
                            if (right.tryLock(1000, TimeUnit.MILLISECONDS)) {
                                try {
                                    Thread.sleep(random.nextInt(1000));
                                } finally {
                                    right.unlock();
                                }
                            } else {
                                System.out.println(this + " can't eat so thinking again");
                            }
                            System.out.println(this + " eat with " + left + " , " + right);
                        } finally {
                            left.unlock();
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        ReentrantLock leftChopstick = new ReentrantLock();
        ReentrantLock rightChopstick = new ReentrantLock();
        Thread philosopher1 = new ResolvingDeadLockViaTimeout(leftChopstick, rightChopstick);
        Thread philosopher2 = new ResolvingDeadLockViaTimeout(rightChopstick, leftChopstick);
        Thread philosopher3 = new ResolvingDeadLockViaTimeout(leftChopstick, rightChopstick);
        Thread philosopher4 = new ResolvingDeadLockViaTimeout(rightChopstick, leftChopstick);
        Thread philosopher5 = new ResolvingDeadLockViaTimeout(leftChopstick, rightChopstick);

        philosopher1.start();
        philosopher2.start();
        philosopher3.start();
        philosopher4.start();
        philosopher5.start();
        philosopher1.join();
        philosopher2.join();
        philosopher3.join();
        philosopher4.join();
        philosopher5.join();
    }


}
