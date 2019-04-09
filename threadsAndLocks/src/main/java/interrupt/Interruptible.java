package interrupt;

import java.util.concurrent.locks.ReentrantLock;

public abstract class Interruptible {

    protected final ReentrantLock o1;
    protected final ReentrantLock o2;
    protected Thread t1;
    protected Thread t2;

    public Interruptible() {
        this.o1 = new ReentrantLock();
        this.o2 = new ReentrantLock();
        initThreads();
    }

    protected void initThreads() {
        t1 = new Thread(() -> {
            try {
                synchronized (o1) {
                    Thread.sleep(1000);
                    synchronized (o2) {
                    }
                }
            } catch (InterruptedException e) {
                System.out.println("t1 interrupted");
            }
        });
        t2 = new Thread(() -> {
            try {
                synchronized (o2) {
                    Thread.sleep(1000);
                    synchronized (o1) {
                    }
                }
            } catch (InterruptedException e) {
                System.out.println("t2 interrupted");
            }
        });
    }
    public abstract void run() throws InterruptedException ;
}
