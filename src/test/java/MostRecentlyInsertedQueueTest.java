import java.util.Queue;

public class MostRecentlyInsertedQueueTest extends QueuesTest {

    @Override
    Queue<Integer> initQueue(int capacity) {
        return new MostRecentlyInsertedQueue<Integer>(capacity);
    }

}
