package philosopher;

import org.junit.Test;
import philosopher.TimeOutablePhilosopher.ReentrantLockableChopstick;

import java.lang.reflect.Constructor;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
import static philosopher.DeadLockUtils.isDeadLocked;

public class PhilosopherTest {

    Thread[] philosophers;

    @Test
    public void deadLock() {
        initPhilosophers(DeadLockablePhilosopher.class
                , new Chopstick(1), new Chopstick(2)
                , 5);
        runPhilosophers();

        assertTrue(isDeadLocked(TimeUnit.MILLISECONDS, 5000));
    }

    @Test
    public void resolve_deadLock_with_global_lock_ordering_rule() {
        initPhilosophers(
                GlobalOrderingPhilosopher.class
                , new Chopstick(1), new Chopstick(2)
                , 5);
        runPhilosophers();

        assertFalse(isDeadLocked(TimeUnit.MILLISECONDS, 10000));
    }

    @Test
    public void resolve_deadLock_with_timeout() {
        initPhilosophers(TimeOutablePhilosopher.class
                , new ReentrantLockableChopstick(), new ReentrantLockableChopstick()
                , 5);
        runPhilosophers();

        assertFalse(isDeadLocked(TimeUnit.MILLISECONDS, 10000));
    }

    @Test
    public void resolve_deadLock_with_signal() {
        philosophers = SignalingPhilosopher.of(5);
        runPhilosophers();

        assertFalse(isDeadLocked(TimeUnit.MILLISECONDS, 10000));
    }

    @Test
    public void resolve_deadLock_with_signal_via_intrinsic_lock() {
        philosophers = IntrinsicSignalingPhilosopher.of(5);
        runPhilosophers();

        assertFalse(isDeadLocked(TimeUnit.MILLISECONDS, 10000));
    }

    private void initPhilosophers(Class<? extends PhilosopherUsingChopsticks> philosopher, Chopstick left, Chopstick right, int numOfPhilosophers) {
        philosophers = new PhilosopherUsingChopsticks[numOfPhilosophers];
        try {
            Constructor<? extends PhilosopherUsingChopsticks> philosopherConstructor = philosopher.getConstructor(left.getClass(), right.getClass());
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

    private void runPhilosophers() {
        for (Thread philosopher : philosophers) {
            philosopher.start();
        }
    }
}
