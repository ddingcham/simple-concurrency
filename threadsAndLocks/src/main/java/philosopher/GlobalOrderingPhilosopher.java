package philosopher;

public class GlobalOrderingPhilosopher extends PhilosopherUsingChopsticks {
    public GlobalOrderingPhilosopher(Chopstick left, Chopstick right) {
        super(left, right);
    }

    @Override
    public void run() {
        Chopstick first;
        Chopstick second;
        if (getLeft().getId() < getRight().getId()) {
            first = getLeft();
            second = getRight();
        } else {
            first = getRight();
            second = getLeft();
        }
        try {
            while (true) {
                Thread.sleep(500);
                synchronized (first) {
                    synchronized (second) {
                        Thread.sleep(500);
                    }
                }
                System.out.println(this + "first id : " + first + " / second id : " + second.getId());
            }
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
    }
}
