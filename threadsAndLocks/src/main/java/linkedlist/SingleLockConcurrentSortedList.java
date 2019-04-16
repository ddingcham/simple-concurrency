package linkedlist;

public class SingleLockConcurrentSortedList<T extends Comparable<T>> implements Insertable<T> {
    private final Node<T> head;
    private final Node<T> tail;

    public SingleLockConcurrentSortedList() {
        head = new Node<>();
        tail = new Node<>();
        head.setNext(tail);
        tail.setPrev(head);
    }

    @Override
    public synchronized void insert(T value) {
        Node current = head;
        Node next = current.getNext();
        while (true) {
            if (next == tail || next.compareWith(value)) {
                Node node = new Node(value, current, next);
                next.setPrev(node);
                current.setNext(node);
                return;
            }
            current = next;
            next = current.getNext();
        }
    }

    public synchronized int size() {
        Node current = tail;
        int count = 0;
        while (current.isNotAfter(head)) {
            ++count;
            current = current.getPrev();
        }
        return count;
    }

    public boolean isSorted() {
        Node current = head;
        while (current.getNext().getNext() != tail) {
            current = current.getNext();
            if (current.compareWith(current.getNext()))
                return false;
        }
        return true;
    }
}
