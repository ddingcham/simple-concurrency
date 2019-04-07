package counting;

import lombok.Getter;

public class Counter {
    @Getter
    private int count = 0;
    public void increment() { ++count; }
}
