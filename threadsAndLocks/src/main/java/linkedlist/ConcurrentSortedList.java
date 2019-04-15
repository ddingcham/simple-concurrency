package linkedlist;

public class ConcurrentSortedList<T extends Comparable<T>> {
    private final Node<T> head;
    private final Node<T> tail;

    public ConcurrentSortedList() {
        head = new Node<>();
        tail = new Node<>();
        head.setNext(tail);
        tail.setPrev(head);
    }

    public void insert(T value) {
        Node current = head;
        current.lock();
        Node next = current.getNext();
        try {
            while (true) {
                next.lock();
                try {
                    if (next == tail || next.compareWith(value)) {
                        Node node = new Node(value, current, next);
                        next.setPrev(node);
                        current.setNext(node);
                        return;
                    }
                } finally {
                    current.unlock();
                }
                current = next;
                next = current.getNext();
            }
        } finally {
            next.unlock();
        }
    }

    public int size() {
        Node current = tail;
        int count = 0;
        while (current.isNotAfter(head)) {
            Node lockedNode = current;
            lockedNode.lock();
            try {
                ++count;
                current = current.getPrev();
            } finally {
                lockedNode.unlock();
            }
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
