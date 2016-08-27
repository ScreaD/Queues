import java.util.AbstractQueue;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class MostRecentlyInsertedQueue<E> extends AbstractQueue<E> {

    private static final int DEFAULT_CAPACITY = 10;

    private final int capacity;
    private int currentSize;
    private final E[] items;
    private int takeIndex;
    private int putIndex;

    public MostRecentlyInsertedQueue(int capacity) {
        if (capacity <= 0) throw new IllegalArgumentException("Size of queue cant be lower than zero");

        this.items = (E[]) new Object[capacity];
        this.capacity = capacity;
    }

    public MostRecentlyInsertedQueue() {
        this.items = (E[]) new Object[DEFAULT_CAPACITY];
        this.capacity = DEFAULT_CAPACITY;
    }

    private int getRealIndex(int i) {
        return (++i == items.length) ? 0 : i;
    }

    @Override
    public boolean offer(E e) {
        if (e == null) throw new NullPointerException();

        insertItem(e);

        return true;
    }

    protected void insertItem(E e) {
        if (currentSize >= capacity) {
            poll();
        }

        items[putIndex] = e;
        putIndex = getRealIndex(putIndex);
        ++currentSize;
    }

    @Override
    public E poll() {
        if (currentSize == 0) {
            return null;
        }

        E x = items[takeIndex];
        items[takeIndex] = null;
        takeIndex = getRealIndex(takeIndex);
        --currentSize;

        return x;
    }

    @Override
    public E peek() {
        if (currentSize == 0) {
            return null;
        }
        
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
                nextIndex = getRealIndex(nextIndex);
                checkNext();
                return result;
            }

            /**
             * Removes from the underlying collection the last element returned
             * by this iterator (optional operation).  This method can be called
             * only once per call to {@link #next}.  The behavior of an iterator
             * is unspecified if the underlying collection is modified while the
             * iteration is in progress in any way other than by calling this
             * method.
             *
             * @throws UnsupportedOperationException if the {@code remove}
             *                                       operation is not supported by this iterator
             * @throws IllegalStateException         if the {@code next} method has not
             *                                       yet been called, or the {@code remove} method has already
             *                                       been called after the last call to the {@code next}
             *                                       method
             * @implSpec The default implementation throws an instance of
             * {@link UnsupportedOperationException} and performs no other action.
             */
            @Override
            public void remove() { // TODO: init this

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
