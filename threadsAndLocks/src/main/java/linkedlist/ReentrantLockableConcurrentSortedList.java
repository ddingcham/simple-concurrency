package linkedlist;

public class ReentrantLockableConcurrentSortedList<T extends Comparable<T>> implements Insertable<T> {
    private final ReentrantLockableNode<T> head;
    private final ReentrantLockableNode<T> tail;

    public ReentrantLockableConcurrentSortedList() {
        head = new ReentrantLockableNode<>();
        tail = new ReentrantLockableNode<>();
        head.setNext(tail);
        tail.setPrev(head);
    }

    @Override
    public void insert(T value) {
        ReentrantLockableNode current = head;
        current.lock();
        ReentrantLockableNode next = current.getNext();
        try {
            while (true) {
                next.lock();
                try {
                    if (next.equals(tail) || next.compareWith(value)) {
                        ReentrantLockableNode node = new ReentrantLockableNode(value, current, next);
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
        ReentrantLockableNode current = tail;
        int count = 0;
        while (current.isNotAfter(head)) {
            ReentrantLockableNode lockedNode = current;
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
        ReentrantLockableNode current = head;
        while (!current.getNext().getNext().equals(tail)) {
            current = current.getNext();
            if (current.compareWith(current.getNext()))
                return false;
        }
        return true;
    }
}
