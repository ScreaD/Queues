import java.util.Collection;

public class ConcurrentMostRecentlyInsertedQueue<E> extends MostRecentlyInsertedQueue<E> {

    public ConcurrentMostRecentlyInsertedQueue(int capacity) {
        super(capacity);
    }

    @Override
    public boolean offer(E e) {
        if (e == null) {
            throw new NullPointerException();
        }

        synchronized (this) {
            insertItem(e);
            return true;
        }
    }

    @Override
    public synchronized E poll() {
        return super.poll();
    }

    @Override
    public synchronized E peek() {
        return super.peek();
    }

    @Override
    public synchronized boolean addAll(Collection<? extends E> c) {
        return super.addAll(c);
    }

    @Override
    public synchronized void clear() {
        while (poll() != null) ;
    }

    @Override
    public synchronized int size() {
        return super.size();
    }

}
