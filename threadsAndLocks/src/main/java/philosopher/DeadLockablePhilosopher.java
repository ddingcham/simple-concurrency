package philosopher;

public class DeadLockablePhilosopher extends PhilosopherUsingChopsticks {

    public DeadLockablePhilosopher(Chopstick left, Chopstick right) {
        super(left, right);
    }

    @Override
    public void run() {
        try {
            while (true) {
                Thread.sleep(500);
                synchronized (getLeft()) {
                    synchronized (getRight()) {
                        Thread.sleep(500);
                    }
                }
                System.out.println(this + "left id : " + getLeft().getId() + " / right id : " + getRight().getId());
            }
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
    }
}
