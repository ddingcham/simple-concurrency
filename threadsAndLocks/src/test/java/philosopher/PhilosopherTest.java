package philosopher;

import org.junit.Test;

import java.lang.reflect.Constructor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.Assert.fail;

public class PhilosopherTest {

    Philosopher[] philosophers;

    @Test(timeout = 15000)
    public void deadLock() throws InterruptedException {
        initPhilosophers(Philosopher.class
                , new DefaultChopStick(1), new DefaultChopStick(2)
                , 5);
        runPhilosophers();
    }

    static class ResolvingDeadLockViaGlobalOrderingRule extends Philosopher {
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

    @Test(timeout = 10000)
    public void resolve_deadLock_with_global_lock_ordering_rule() throws InterruptedException {
        initPhilosophers(
                ResolvingDeadLockViaGlobalOrderingRule.class
                , new DefaultChopStick(1), new DefaultChopStick(2)
                , 5);
        runPhilosophers();
    }

    static class ReentrantLockableChopstick extends ReentrantLock implements Chopstick {
        @Override
        public int getId() {
            return 0;
        }
    }

    static class ResolvingDeadLockViaTimeout extends Philosopher {
        public ResolvingDeadLockViaTimeout(Chopstick left, Chopstick right) {
            super(left, right);
        }

        @Override
        public void run() {
            if (!canConvertChopstickToReentrantLock()) {
                fail("Chopstick must be able to be converted ReentrantLock");
            }

            ReentrantLock firstLock = (ReentrantLock) getFirst();
            ReentrantLock secondLock = (ReentrantLock) getSecond();
            try {
                while (true) {
                    Thread.sleep(random.nextInt(1000));
                    firstLock.lock();
                    try {
                        if (secondLock.tryLock(1000, TimeUnit.MILLISECONDS)) {
                            try {
                                Thread.sleep(random.nextInt(1000));
                            } finally {
                                secondLock.unlock();
                            }
                        } else {
                            System.out.println(this + " can't eat so thinking again");
                        }
                        System.out.println(this + " eat with " + firstLock + " , " + secondLock);
                    } finally {
                        firstLock.unlock();
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        private boolean canConvertChopstickToReentrantLock() {
            return getFirst() instanceof ReentrantLock && getSecond() instanceof ReentrantLock;
        }
    }

    @Test(timeout = 10000)
    public void resolve_deadLock_with_timeout() throws InterruptedException {
        initPhilosophers(ResolvingDeadLockViaTimeout.class
                , new ReentrantLockableChopstick(), new ReentrantLockableChopstick()
                , 5);
        runPhilosophers();
    }

    private void initPhilosophers(Class<? extends Philosopher> philosopher, Chopstick left, Chopstick right, int numOfPhilosophers) {
        philosophers = new Philosopher[numOfPhilosophers];
        try {
            Constructor<? extends Philosopher> philosopherConstructor = philosopher.getConstructor(Chopstick.class, Chopstick.class);
            for (int i = 0; i < numOfPhilosophers; i++) {
                if (i < numOfPhilosophers / 2) {
                    philosophers[i] = philosopherConstructor.newInstance(left, right);
                } else {
                    philosophers[i] = philosopherConstructor.newInstance(right, left);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    private void runPhilosophers() throws InterruptedException {
        for (Philosopher philosopher : philosophers) {
            philosopher.start();
        }
        for (Philosopher philosopher : philosophers) {
            philosopher.join();
        }
    }

    static class DefaultChopStick implements Chopstick {
        private final int id;

        public DefaultChopStick(int id) {
            this.id = id;
        }

        @Override
        public int getId() {
            return id;
        }
    }
}
