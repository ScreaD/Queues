import java.util.AbstractQueue;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by scread on 26.08.16.
 */
public class MostRecentlyInsertedBlockingQueue<E> extends AbstractQueue<E> {

    private final int capacity;
    private int currentSize;
    private final E[] items;
    private int takeIndex;
    private int putIndex;
    private final ReentrantLock lock;
    private final Condition notEmpty;

    public MostRecentlyInsertedBlockingQueue(int capacity) {
        if (capacity <= 0) throw new IllegalArgumentException("Queue cant be lower than zero");
        this.items = (E[]) new Object[capacity];
        this.capacity = capacity;
        lock = new ReentrantLock();
        notEmpty = lock.newCondition();
    }

    /**
     * Returns an iterator over the elements contained in this collection.
     *
     * @return an iterator over the elements contained in this collection
     */
    @Override
    public Iterator<E> iterator() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return new Iterator<E>() {
                private int lastReturnedIndex = -1;
                private int nextIndex = takeIndex;

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
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int size() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return currentSize;
        } finally {
            lock.unlock();
        }
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
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            insertItem(e);
        } finally {
            lock.unlock();
        }
        return true;
    }

    private void insertItem(E e) {
        if (currentSize >= capacity) poll();
        items[putIndex] = e;
        putIndex = increment(putIndex);
        ++currentSize;
        notEmpty.signal();
    }

    /**
     * Retrieves and removes the head of this queue,
     * or returns {@code null} if this queue is empty.
     *
     * @return the head of this queue, or {@code null} if this queue is empty
     */
    @Override
    public E poll() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            if (currentSize == 0)
                return null;
            final E[] items = this.items;
            E x = items[takeIndex];
            items[takeIndex] = null;
            --currentSize;
            takeIndex = increment(takeIndex);
            return x;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Retrieves, but does not remove, the head of this queue,
     * or returns {@code null} if this queue is empty.
     *
     * @return the head of this queue, or {@code null} if this queue is empty
     */
    @Override
    public E peek() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            if (currentSize == 0)
                return null;
            return items[takeIndex];
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void clear() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            while (poll() != null);
        } finally {
            lock.unlock();
        }
    }

    public void put(E e) {
        offer(e);
    }

    public void take(E e) throws InterruptedException {
        final ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        try {
            while (currentSize == 0)
                notEmpty.await();
        } catch (InterruptedException exception) {
            notEmpty.signal();
            throw exception;
        } finally {
            lock.unlock();
        }
    }

    private int increment(int i) {
        return (++i == items.length) ? 0 : i;
    }
}
