package philosopher;

import org.junit.Test;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
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

    //TODO Refactor Philosopher's Expansion Strategy
    static class ResolvingDeadLockViaSignal extends Philosopher {

        private boolean eating;
        private ResolvingDeadLockViaSignal left;
        private ResolvingDeadLockViaSignal right;
        private ReentrantLock table;
        private Condition condition;
        private Random random;

        public ResolvingDeadLockViaSignal(ReentrantLock table) {
            super(null, null);
            eating = false;
            this.table = table;
            condition = table.newCondition();
            random = new Random();
        }

        public void setLeft(ResolvingDeadLockViaSignal left) {
            this.left = left;
        }

        public void setRight(ResolvingDeadLockViaSignal right) {
            this.right = right;
        }

        @Override
        public void run() {
            try {
                while (true) {
                    think();
                    System.out.println("complete thinking : " + this);
                    eat();
                    System.out.println("complete eating : " + this);
                }
            } catch (InterruptedException e) {
                System.out.println(this + "is interrupted");
            }
        }

        private void think() throws InterruptedException {
            table.lock();
            try {
                eating = false;
                left.condition.signal();
                right.condition.signal();
            } finally {
                table.unlock();
            }
            Thread.sleep(1000);
        }

        private void eat() throws InterruptedException {
            table.lock();
            try {
                while (left.eating || right.eating) {
                    condition.await();
                }
                eating = true;
            } finally {
                table.unlock();
            }
            Thread.sleep(1000);
        }
    }

    @Test
    public void resolve_deadLock_with_signal() throws InterruptedException {
        ResolvingDeadLockViaSignal[] philosophers = new ResolvingDeadLockViaSignal[5];
        ReentrantLock sharedTable = new ReentrantLock();
        Random random = new Random();

        for (int i = 0; i < philosophers.length; i++) {
            philosophers[i] = new ResolvingDeadLockViaSignal(sharedTable);
        }

        for (int i = 0; i < philosophers.length; i++) {
            philosophers[i].setLeft(philosophers[(i + 4) % 5]);
            philosophers[i].setRight(philosophers[(i + 1) % 5]);
            philosophers[i].start();
        }

        for (ResolvingDeadLockViaSignal philosopher : philosophers) {
            philosopher.join();
        }
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
