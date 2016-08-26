import java.util.Queue;

public class MostRecentlyInsertedBlockingQueueTest extends QueuesTest {

    @Override
    Queue<Integer> initQueue(int capacity) {
        return new MostRecentlyInsertedBlockingQueue<>(capacity);
    }
}