package philosopher;

import lombok.AccessLevel;
import lombok.Setter;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class SignalingPhilosopher extends Thread{

    private static final int MINIMUM_NUMBER_OF_PHILOSOPHERS = 3;

    @Setter(AccessLevel.PACKAGE)
    private SignalingPhilosopher left;
    @Setter(AccessLevel.PACKAGE)
    private SignalingPhilosopher right;
    private boolean eating;
    private ReentrantLock table;
    private Condition condition;

    private SignalingPhilosopher(ReentrantLock table) {
        this.table = table;
        condition = table.newCondition();
        eating = false;
    }

    public static SignalingPhilosopher[] of(int numberOfPhilosophers) {
        if (numberOfPhilosophers < MINIMUM_NUMBER_OF_PHILOSOPHERS) {
            throw new IllegalArgumentException("numberOfPhilosophers must be more than " + MINIMUM_NUMBER_OF_PHILOSOPHERS);
        }
        SignalingPhilosopher[] philosophers = new SignalingPhilosopher[numberOfPhilosophers];
        ReentrantLock table = new ReentrantLock();

        for (int i = 0; i < numberOfPhilosophers; i++) {
            philosophers[i] = new SignalingPhilosopher(table);
        }

        for (int i = 0; i < numberOfPhilosophers; i++) {
            philosophers[i].left = philosophers[(i + numberOfPhilosophers - 1) % numberOfPhilosophers];
            philosophers[i].right = philosophers[(i + 1) % numberOfPhilosophers];
        }
        return philosophers;
    }

    @Override
    public void run() {
        try {
            while (true) {
                think();
                eat();
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
        Thread.sleep(500);
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
        Thread.sleep(500);
    }
}
