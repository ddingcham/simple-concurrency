package philosopher;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.Random;

public class Philosopher extends Thread {
    @Getter(AccessLevel.PROTECTED) @Setter(AccessLevel.PROTECTED)
    private Chopstick first;
    @Getter(AccessLevel.PROTECTED) @Setter(AccessLevel.PROTECTED)
    private Chopstick second;
    protected Random random;

    public Philosopher(Chopstick left, Chopstick right) {
        init(left, right);
        random = new Random();
    }

    protected void init(Chopstick left, Chopstick right) {
        this.first = left;
        this.second = right;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Thread.sleep(random.nextInt(1000));
                synchronized (first) {
                    synchronized (second) {
                        Thread.sleep(random.nextInt(1000));
                    }
                }
                System.out.println(this + "first id : " + first.getId() + " / second id : " + second.getId());
            }
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
    }
}
