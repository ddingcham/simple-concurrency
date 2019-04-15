package philosopher;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

public abstract class PhilosopherUsingChopsticks extends Thread {
    @Getter(AccessLevel.PROTECTED) @Setter(AccessLevel.PROTECTED)
    private Chopstick left;
    @Getter(AccessLevel.PROTECTED) @Setter(AccessLevel.PROTECTED)
    private Chopstick right;

    public PhilosopherUsingChopsticks(Chopstick left, Chopstick right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public abstract void run();
}
