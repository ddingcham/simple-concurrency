package linkedlist;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.concurrent.locks.ReentrantLock;

public class Node<T extends Comparable<T>> extends ReentrantLock {

    private T value;

    @Setter(value = AccessLevel.PACKAGE, onParam_ = @NonNull)
    @Getter(value = AccessLevel.PACKAGE)
    private Node<T> prev;

    @Setter(value = AccessLevel.PACKAGE, onParam_ = @NonNull)
    @Getter(value = AccessLevel.PACKAGE)
    private Node<T> next;

    Node() {
    }

    Node(T value, Node<T> prev, Node<T> next) {
        this.value = value;
        this.prev = prev;
        this.next = next;
    }

    public boolean compareWith(T otherValue) {
        return this.value != null && this.value.compareTo(otherValue) > 0;
    }

    public boolean compareWith(Node<T> otherNode) {
        return compareWith(otherNode.value);
    }

    public boolean isNotAfter(Node otherNode) {
        return this.prev != otherNode;
    }
}
