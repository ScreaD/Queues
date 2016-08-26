import java.util.Queue;

/**
 * Created by scread on 26.08.16.
 */
public class MostRecentlyInsertedBlockingQueueTest extends QueuesTest {

    @Override
    Queue<Integer> initQueue(int capacity) {
        return new MostRecentlyInsertedBlockingQueue<>(capacity);
    }
}
