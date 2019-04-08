package philosopher;

import org.junit.Before;
import org.junit.Test;

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
        for(int i = 0 ; i < numOfPhilosophers ; i++) {
            if (i < numOfPhilosophers/2) {
                philosophers[i] = new Philosopher(left, right);
            }
            else {
                philosophers[i] = new Philosopher(right, left);
            }
        }
        for(Philosopher philosopher : philosophers) {
            philosopher.start();
        }
        for(Philosopher philosopher : philosophers) {
            philosopher.join();
        }
    }

    @Test
    public void resolve_deadLock_with_global_lock_ordering_rule() throws InterruptedException {
        Philosopher philosopher1 = new ResolvingDeadLock(left, right);
        Philosopher philosopher2 = new ResolvingDeadLock(right, left);
        Philosopher philosopher3 = new ResolvingDeadLock(left, right);
        Philosopher philosopher4 = new ResolvingDeadLock(right, left);
        Philosopher philosopher5 = new ResolvingDeadLock(left, right);

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

    class ResolvingDeadLock extends Philosopher {
        public ResolvingDeadLock(Chopstick left, Chopstick right) {
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
}
