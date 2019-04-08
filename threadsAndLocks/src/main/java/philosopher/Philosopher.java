package philosopher;

import java.util.Random;

public class Philosopher extends Thread {
    private Chopstick first;
    private Chopstick second;
    private Random random;

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

    void setFirst(Chopstick first) {
        this.first = first;
    }

    void setSecond(Chopstick second) {
        this.second = second;
    }
}
