import java.util.AbstractQueue;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Created by scread on 25.08.16.
 */
public class MostRecentlyInsertedQueue<E> extends AbstractQueue<E> {

    private final int capacity;
    private int currentSize;
    private final E[] items;
    private int takeIndex;
    private int putIndex;

    public MostRecentlyInsertedQueue(int capacity) {
        if (capacity <= 0) throw new IllegalArgumentException("Queue cant be lower than zero");
        this.items = (E[]) new Object[capacity];
        this.capacity = capacity;
    }

    /**
     * Returns an iterator over the elements contained in this collection.
     *
     * @return an iterator over the elements contained in this collection
     */
    @Override
    public Iterator<E> iterator() {
        return new Iterator<E>() {
            private int nextIndex = takeIndex;
            private int lastReturnedIndex = -1;

            @Override
            public boolean hasNext() {
                return nextIndex >= 0;
            }

            @Override
            public E next() {
                if (!hasNext()) throw new NoSuchElementException();
                lastReturnedIndex = nextIndex;
                nextIndex = increment(nextIndex);
                return items[lastReturnedIndex];
            }
        };
    }

    @Override
    public int size() {
        return currentSize;
    }

    /**
     * Inserts the specified element into this queue if it is possible to do
     * so immediately without violating capacity restrictions.
     * When using a capacity-restricted queue, this method is generally
     * preferable to {@link #add}, which can fail to insert an element only
     * by throwing an exception.
     *
     * @param e the element to add
     * @return {@code true} if the element was added to this queue, else
     * {@code false}
     * @throws ClassCastException       if the class of the specified element
     *                                  prevents it from being added to this queue
     * @throws NullPointerException     if the specified element is null and
     *                                  this queue does not permit null elements
     * @throws IllegalArgumentException if some property of this element
     *                                  prevents it from being added to this queue
     */
    @Override
    public boolean offer(E e) {
        if (e == null) throw new NullPointerException();
        insertItem(e);
        return true;
    }

    protected void insertItem(E e) {
        if (currentSize >= capacity) poll();
        items[putIndex] = e;
        putIndex = increment(putIndex);
        ++currentSize;
    }

    /**
     * Retrieves and removes the head of this queue,
     * or returns {@code null} if this queue is empty.
     *
     * @return the head of this queue, or {@code null} if this queue is empty
     */
    @Override
    public E poll() {
        if (currentSize == 0)
            return null;
        final E[] items = this.items;
        E x = items[takeIndex];
        items[takeIndex] = null;
        takeIndex = increment(takeIndex);
        --currentSize;
        return x;
    }

    /**
     * Retrieves, but does not remove, the head of this queue,
     * or returns {@code null} if this queue is empty.
     *
     * @return the head of this queue, or {@code null} if this queue is empty
     */
    @Override
    public E peek() {
        if (currentSize == 0)
            return null;
        return items[takeIndex];
    }

    private int increment(int i) {
        return (++i == items.length) ? 0 : i;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < items.length; i++) {
            sb.append(items[i]).append(" ");
        }
        return sb.toString();
    }
}
