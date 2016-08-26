import java.util.Queue;

/**
 * Created by scread on 26.08.16.
 */
public class ConcurrentMostRecentlyInsertedQueueTest extends QueuesTest {

    @Override
    Queue<Integer> initQueue(int capacity) {
        return new ConcurrentMostRecentlyInsertedQueue<>(capacity);
    }
}
