import java.util.AbstractQueue;
import java.util.Iterator;
import java.util.NoSuchElementException;

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

    private int increment(int i) {
        return (++i == items.length) ? 0 : i;
    }

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

    @Override
    public E peek() {
        if (currentSize == 0)
            return null;
        return items[takeIndex];
    }

    @Override
    public Iterator<E> iterator() {
        return new Iterator<E>() {
            private int nextIndex = (currentSize == 0) ? -1 : takeIndex;
            private int lastReturnedIndex = -1;
            private E nextItem;

            @Override
            public boolean hasNext() {
                return nextIndex >= 0;
            }

            @Override
            public E next() {
                if (!hasNext()) throw new NoSuchElementException();
                lastReturnedIndex = nextIndex;
                E result = items[lastReturnedIndex];
                nextIndex = increment(nextIndex);
                checkNext();
                return result;
            }

            private void checkNext() {
                if (nextIndex == putIndex) {
                    nextIndex = -1;
                    nextItem = null;
                } else {
                    nextItem = items[nextIndex];
                    if (nextItem == null)
                        nextIndex = -1;
                }
            }
        };
    }

    @Override
    public int size() {
        return currentSize;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < items.length; i++) {
            sb.append(items[i]).append(" ");
        }
        return sb.toString();
    }
}
