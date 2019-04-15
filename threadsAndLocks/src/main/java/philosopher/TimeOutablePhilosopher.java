package philosopher;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class TimeOutablePhilosopher extends PhilosopherUsingChopsticks {

    public TimeOutablePhilosopher(ReentrantLockableChopstick left, ReentrantLockableChopstick right) {
        super(left, right);
    }

    @Override
    public void run() {
        ReentrantLockableChopstick left = (ReentrantLockableChopstick) getLeft();
        ReentrantLockableChopstick right = (ReentrantLockableChopstick) getRight();
        try {
            while (true) {
                Thread.sleep(500);
                left.lock();
                try {
                    if (right.tryLock(500, TimeUnit.MILLISECONDS)) {
                        try {
                            Thread.sleep(500);
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

    static class ReentrantLockableChopstick extends Chopstick {
        private ReentrantLock lock;
        public ReentrantLockableChopstick() {
            super(0);
            lock = new ReentrantLock();
        }

        void lock() {
            lock.lock();
        }

        void unlock() {
            lock.unlock();
        }

        boolean tryLock(long timeout, TimeUnit unit) throws InterruptedException {
            return lock.tryLock(timeout, unit);
        }
    }
}
