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
