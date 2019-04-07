package counting;

public class SynchronizedCounter extends Counter {
    @Override
    public synchronized void increment() {
        super.increment();
    }
}
