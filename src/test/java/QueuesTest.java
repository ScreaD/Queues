import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Queue;

import static org.junit.Assert.*;

public abstract class QueuesTest {

    private Queue<Integer> queue;

    private final static int DEFAULT_CAPACITY = 5;

    abstract Queue<Integer> initQueue(int capacity);

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() {
        queue = initQueue(DEFAULT_CAPACITY);
    }

    @Test
    public void shouldThrownIllegalArgumentException_whenCapacityLowerZero() {
        exception.expect(IllegalArgumentException.class);
        queue = initQueue(-1);
    }

    @Test
    public void shouldSizeGreaterZero_whenOfferItem() {
        queue.offer(1);

        assertEquals(queue.size(), 1);

        queue.offer(2);

        assertEquals(queue.size(), 2);
    }

    @Test
    public void shouldZeroSize_whenInitQueue() {
        assertEquals(queue.size(), 0);
    }

    @Test
    public void shouldAddedItemEqualsPeekedItem() {
        Integer item = 1;

        queue.offer(item);

        assertEquals(queue.peek(), item);
    }

    @Test
    public void shouldThrowNPE_whenAddedNullValue() {
        exception.expect(NullPointerException.class);
        queue.offer(null);
    }

    @Test
    public void shouldBeAddedValue_whenSizeEqualsCapacity() {
        // fulfill the queue
        int startIndex = 0;
        for (int i = startIndex; i < DEFAULT_CAPACITY; i++) {
            queue.offer(i);
        }

        // when
        assertTrue(queue.offer(DEFAULT_CAPACITY + 1));

        // then
        Integer newHead = ++startIndex;
        assertEquals(queue.peek(), newHead);
    }

    @Test
    public void shouldNextHead_whenRemoveHead() {
        // given
        Integer item = 1;
        Integer prevItem = 2;
        queue.offer(item);
        queue.offer(prevItem);

        // when
        queue.poll();

        // then
        assertEquals(queue.peek(), prevItem);
    }

    @Test
    public void shouldSizeDecremented_whenPolledItem() {
        // given
        queue.offer(1);
        queue.offer(2);
        int givenSize = 2;
        int decrementedSize = 1;
        assertEquals(queue.size(), givenSize);

        // when
        queue.poll();

        // then
        assertEquals(queue.size(), decrementedSize);
    }

    @Test
    public void shouldNull_whenPeekEmptyQueue() {
        assertNull(queue.peek());
    }

    @Test
    public void shouldNull_whenPollEmptyQueue() {
        assertNull(queue.poll());
    }

    @Test
    public void shouldThrownNoSuchElementException_whenGettingNextOnEmptyQueue() {
        exception.expect(NoSuchElementException.class);
        queue.iterator().next();
    }

    @Test
    public void shouldReturnFalse_whenHasNextOnEmpty() {
        assertFalse(queue.iterator().hasNext());
    }

    @Test
    public void shouldReturnTrue_whenQueueIsNotEmpty() {
        queue.offer(1);

        assertTrue(queue.iterator().hasNext());
    }

    @Test
    public void shouldIteratorReturnItems_whenQueueIsNotEmpty() {
        // given
        Integer[] items = new Integer[] {1, 2, 3};
        for (Integer item : items) {
            queue.offer(item);
        }

        Iterator<Integer> iter = queue.iterator();
        for (Integer item: items) {
            assertEquals(iter.next(), item);
        }
    }
}
