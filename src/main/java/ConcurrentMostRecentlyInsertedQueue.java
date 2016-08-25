import java.util.Collection;
import java.util.Iterator;

/**
 * Created by scread on 25.08.16.
 */
public class ConcurrentMostRecentlyInsertedQueue<E> extends MostRecentlyInsertedQueue<E> {

    private final MostRecentlyInsertedQueue<E> queue;

    public ConcurrentMostRecentlyInsertedQueue(int capacity) {
        super(capacity);
        this.queue = new MostRecentlyInsertedQueue<>(capacity);
    }

    @Override
    public boolean offer(E e) {
        if (e == null) throw new NullPointerException();
        synchronized (this) {
            queue.insertItem(e);
            return true;
        }
    }

    @Override
    public synchronized E poll() {
        return queue.poll();
    }

    @Override
    public synchronized E peek() {
        return queue.peek();
    }

    @Override
    public synchronized int size() {
        return queue.size();
    }

    @Override
    public synchronized Iterator<E> iterator() {
        return queue.iterator();
    }

    @Override
    public synchronized void clear() {
        while (queue.poll() != null)
            ;
    }

    @Override
    public synchronized boolean addAll(Collection<? extends E> c) {
        return queue.addAll(c);
    }

    // TODO: think about necessarily of this synchronization + remove
//    @Override
//    public synchronized boolean add(E e) {
//        return queue.add(e);
//    }
}
