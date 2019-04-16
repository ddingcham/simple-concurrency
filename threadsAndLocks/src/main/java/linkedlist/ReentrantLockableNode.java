package linkedlist;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.locks.ReentrantLock;

@RequiredArgsConstructor
public class ReentrantLockableNode<T extends Comparable<T>> extends ReentrantLock {

    private final Node<T> node;

    ReentrantLockableNode() {
        this.node = new Node<>();
    }

    ReentrantLockableNode(T value, ReentrantLockableNode<T> prev, ReentrantLockableNode<T> next) {
        this.node = new Node<>(value, prev.node, next.node);
    }

    public boolean compareWith(T otherValue) {
        return node.compareWith(otherValue);
    }

    public boolean compareWith(ReentrantLockableNode<T> otherNode) {
        return node.compareWith(otherNode.node);
    }

    public boolean isNotAfter(ReentrantLockableNode<T> otherNode) {
        return node.isNotAfter(otherNode.node);
    }

    public void setNext(ReentrantLockableNode<T> next) {
        node.setNext(next.node);
    }

    public void setPrev(ReentrantLockableNode<T> prev) {
        node.setPrev(prev.node);
    }

    public ReentrantLockableNode<T> getNext() {
        return new ReentrantLockableNode<>(node.getNext());
    }

    public ReentrantLockableNode<T> getPrev() {
        return new ReentrantLockableNode<>(node.getPrev());
    }

    public boolean equals(@NonNull ReentrantLockableNode<T> other) {
        return this.node == other.node;
    }
}
