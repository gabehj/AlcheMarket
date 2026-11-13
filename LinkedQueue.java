import java.util.NoSuchElementException;

public class LinkedQueue<T> {
    private static class Node<T> {
        private final T value;
        private Node<T> next;

        Node(T value) {
            this.value = value;
        }
    }

    private Node<T> head;
    private Node<T> tail;

    public void enqueue(T value) {
        if (value == null) {
            throw new IllegalArgumentException("A queue entry cannot be null.");
        }
        Node<T> node = new Node<>(value);
        if (isEmpty()) {
            head = node;
        }
        else {
            tail.next = node;
        }
        tail = node;
    }

    public T dequeue() {
        T value = peek();
        head = head.next;
        if (isEmpty()) {
            tail = null;
        }
        return value;
    }

    public T peek() {
        if (isEmpty()) {
            throw new NoSuchElementException("The queue is empty.");
        }
        return head.value;
    }

    public boolean isEmpty() {
        return head == null;
    }
}
