import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

import static org.junit.Assert.assertEquals;

public class ConcurrentMostRecentlyInsertedQueueTest extends QueuesTest {

    private final static int THREADS_NUMBERS = 20;

    private final static int NUMBER_ITEMS = 100;

    private final static int CAPACITY = THREADS_NUMBERS * NUMBER_ITEMS * 2;

    private final static int SLEEP_TIME = 50;

    private Queue<Integer> concurrentQueue;

    @Override
    Queue<Integer> initQueue(int capacity) {
        return new ConcurrentMostRecentlyInsertedQueue<>(capacity);
    }

    @Before
    public void createQueue() {
        concurrentQueue = new ConcurrentMostRecentlyInsertedQueue<>(CAPACITY);
    }

    @Test
    public void shouldCorrectlyAddedAllItems_whenThreadsOfferItems() throws InterruptedException {
        fillQueue();

        Thread.sleep(SLEEP_TIME);
        assertEquals(concurrentQueue.size(), THREADS_NUMBERS * NUMBER_ITEMS);
    }

    @Test
    public void shouldCorrectSize_whenPollItemsByThreads() throws InterruptedException {
        fillQueue();

        Integer additionalItem = new Random().nextInt();
        concurrentQueue.offer(additionalItem);
        int expectingSize = THREADS_NUMBERS * NUMBER_ITEMS + 1;

        assertEquals(concurrentQueue.size(), expectingSize);

        for (int i = 0; i < THREADS_NUMBERS; i++) {
            new Thread(() -> {
                for (int j = 0; j < NUMBER_ITEMS; j++) concurrentQueue.poll();
            }).start();
        }

        Thread.sleep(SLEEP_TIME);
        assertEquals(concurrentQueue.size(), 1);
    }

    @Test
    public void shouldBeAddedAddItems_whenAllItems() {
        List<Integer> collection = new ArrayList<>(10);
        for (int i = 0; i < NUMBER_ITEMS; i++) {
            collection.add(i);
        }

        concurrentQueue.addAll(collection);

        assertEquals(concurrentQueue.size(), NUMBER_ITEMS);
    }

    private void fillQueue() throws InterruptedException {
        for (int i = 0; i < THREADS_NUMBERS; i++) {
            new Thread(() -> {
                for (int j = 0; j < NUMBER_ITEMS; j++) concurrentQueue.offer(j);
            }).start();
        }
        Thread.sleep(SLEEP_TIME);
    }

}
