import static org.junit.Assert.*;

import java.util.NoSuchElementException;
import org.junit.Test;

public class LinkedQueueTest {
    @Test
    public void servesInArrivalOrderAndCanBeReused() {
        LinkedQueue<String> queue = new LinkedQueue<>();
        assertTrue(queue.isEmpty());
        queue.enqueue("first");
        queue.enqueue("second");
        assertFalse(queue.isEmpty());
        assertEquals("first", queue.peek());
        assertEquals("first", queue.dequeue());
        assertEquals("second", queue.dequeue());
        assertTrue(queue.isEmpty());
        queue.enqueue("third");
        assertEquals("third", queue.dequeue());
        assertTrue(queue.isEmpty());
    }

    @Test
    public void rejectsEmptyAccessAndNullEntries() {
        LinkedQueue<String> queue = new LinkedQueue<>();
        assertThrows(NoSuchElementException.class, queue::peek);
        assertThrows(NoSuchElementException.class, queue::dequeue);
        queue.enqueue("first");
        assertThrows(IllegalArgumentException.class, () -> queue.enqueue(null));
        assertEquals("first", queue.dequeue());
        assertTrue(queue.isEmpty());
    }
}
