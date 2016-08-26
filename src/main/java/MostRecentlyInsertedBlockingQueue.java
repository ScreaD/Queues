import java.util.AbstractQueue;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class MostRecentlyInsertedBlockingQueue<E> extends AbstractQueue<E> implements BlockingQueue<E> {

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

    private int increment(int i) {
        return (++i == items.length) ? 0 : i;
    }

    public int drainTo(Collection<? super E> c) {
        if (c == null) throw new NullPointerException();
        if (c == this) throw new IllegalArgumentException();
        final E[] items = this.items;
        final ReentrantLock lock = this.lock;
        try {
            int takeIndex = this.takeIndex;
            int transferred = 0;
            int size = currentSize;
            transferred = transfer(c, items, takeIndex, transferred, size);
            if (transferred > 0) {
                this.currentSize = 0;
                this.takeIndex = 0;
                this.putIndex = 0;
            }
            return transferred;
        } finally {
            lock.unlock();
        }
    }

    public int drainTo(Collection<? super E> c, int maxElements) {
        if (c == null) throw new NullPointerException();
        if (c == this) throw new IllegalArgumentException();
        if (maxElements <= 0) return 0;
        final E[] items = this.items;
        final ReentrantLock lock = this.lock;
        try {
            int takeIndex = this.takeIndex;
            int transferred = 0;
            int max = (maxElements < currentSize) ? maxElements : currentSize;
            transferred = transfer(c, items, takeIndex, transferred, max);
            if (transferred > 0) {
                this.currentSize -= transferred;
                this.takeIndex = takeIndex;
            }
            return transferred;
        } finally {
            lock.unlock();
        }
    }

    private int transfer(Collection<? super E> c, E[] items, int takeIndex, int transferred, int size) {
        if (transferred < size) {
            c.add(items[takeIndex]);
            items[takeIndex] = null;
            takeIndex = increment(takeIndex);
            ++transferred;
        }
        return transferred;
    }

    private void insertItem(E e) {
        if (currentSize >= capacity) poll();
        items[putIndex] = e;
        putIndex = increment(putIndex);
        ++currentSize;
        notEmpty.signal();
    }

    public boolean offer(E e) {
        if (e == null) throw new NullPointerException();
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            insertItem(e);
            return true;
        } finally {
            lock.unlock();
        }
    }

    public boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException { // TODO
        if (e == null) throw new NullPointerException();
        long nanos = unit.toNanos(timeout);
        final ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        try {
            for (; ; ) {
                if (currentSize != items.length) {
                    insertItem(e);
                    return true;
                }
                if (nanos <= 0)
                    return false;
            }
        } finally {
            lock.unlock();
        }
    }

    private E extract() {
        final E[] items = this.items;
        E result = items[takeIndex];
        items[takeIndex] = null;
        --currentSize;
        takeIndex = increment(takeIndex);
        return result;
    }

    public E poll() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            if (currentSize == 0)
                return null;
            return extract();
        } finally {
            lock.unlock();
        }
    }

    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        long nanos = unit.toNanos(timeout);
        final ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        try {
            while (true) {
                if (currentSize != 0) {
                    return extract();
                }
                if (nanos <= 0) return null;
                try {
                    nanos = notEmpty.awaitNanos(nanos);
                } catch (InterruptedException ie) {
                    notEmpty.signal();
                    throw ie;
                }
            }
        } finally {
            lock.unlock();
        }
    }

    public int remainingCapacity() { // TODO: think about it
        return capacity;
    }

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

    public void put(E e) {
        offer(e);
    }

    public E take() throws InterruptedException {
        final ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        try {
            try {
                while (currentSize == 0)
                    notEmpty.await();
            } catch (InterruptedException exception) {
                notEmpty.signal();
                throw exception;
            }
            return extract();
        } finally {
            lock.unlock();
        }
    }

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
    public boolean contains(Object o) {
        if (o == null) return false;
        final E[] items = this.items;
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            int takeIndex = this.takeIndex;
            int index = 0;
            while (index++ < currentSize) {
                if (o.equals(items[takeIndex]))
                    return true;
                index = increment(takeIndex);
            }
            return false;
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

    @Override
    public void clear() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            while (poll() != null) ;
        } finally {
            lock.unlock();
        }
    }
}
