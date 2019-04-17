package philosopher;

import lombok.AccessLevel;
import lombok.Setter;

public class IntrinsicSignalingPhilosopher extends Thread {

    private static final int MINIMUM_NUMBER_OF_PHILOSOPHERS = 3;

    @Setter(AccessLevel.PACKAGE)
    private IntrinsicSignalingPhilosopher left;
    @Setter(AccessLevel.PACKAGE)
    private IntrinsicSignalingPhilosopher right;
    private boolean eating = false;
    private Object table;

    private IntrinsicSignalingPhilosopher(Object sharedTable) {
        this.table = sharedTable;
    }

    public static IntrinsicSignalingPhilosopher[] of(int numberOfPhilosophers) {
        if (numberOfPhilosophers < MINIMUM_NUMBER_OF_PHILOSOPHERS) {
            throw new IllegalArgumentException("numberOfPhilosophers must be more than " + MINIMUM_NUMBER_OF_PHILOSOPHERS);
        }
        IntrinsicSignalingPhilosopher[] philosophers = new IntrinsicSignalingPhilosopher[numberOfPhilosophers];

        Object sharedTable = new Object();

        for (int i = 0; i < numberOfPhilosophers; i++) {
            philosophers[i] = new IntrinsicSignalingPhilosopher(sharedTable);
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
        eating = false;
        System.out.println(this + " thinking ...");
        Thread.sleep(500);
    }

    private void eat() throws InterruptedException {
        synchronized (table) {
            while (left.eating || right.eating) {
                table.wait();
            }
            eating = true;
            System.out.println(this + " eating ...");
            Thread.sleep(1000);
            eating = false;
            System.out.println(this + " eating end ...");
            table.notifyAll();
        }
    }
}
