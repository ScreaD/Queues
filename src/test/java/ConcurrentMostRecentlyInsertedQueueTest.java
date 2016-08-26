import org.junit.Before;
import org.junit.Test;

import java.util.Queue;
import java.util.Random;

import static org.junit.Assert.assertEquals;

public class ConcurrentMostRecentlyInsertedQueueTest extends QueuesTest {

    private final static int THREADS_NUMBERS = 20;

    private final static int NUMBER_ITEMS = 100;

    private final static int CAPACITY = THREADS_NUMBERS * NUMBER_ITEMS * 2;

    private final static int SLEEP_TIME = 50;

    Queue<Integer> q;

    @Override
    Queue<Integer> initQueue(int capacity) {
        return new ConcurrentMostRecentlyInsertedQueue<>(capacity);
    }

    @Before
    public void createQueue() {
        q = new ConcurrentMostRecentlyInsertedQueue<>(CAPACITY);
    }

    @Test
    public void shouldCorrectlyAddedAllItems_whenThreadsSimultaneouslyOfferItems() throws InterruptedException {
        fillQueue();

        Thread.sleep(SLEEP_TIME);
        assertEquals(q.size(), THREADS_NUMBERS * NUMBER_ITEMS);
    }

    @Test
    public void shouldCorrectSize_whenPollItemsByThreads() throws InterruptedException {
        fillQueue();

        Integer additionalItem = new Random().nextInt();
        q.offer(additionalItem);
        int expectingSize = THREADS_NUMBERS * NUMBER_ITEMS + 1;

        assertEquals(q.size(), expectingSize);

        for (int i = 0; i < THREADS_NUMBERS; i++) {
            new Thread(() -> {
                for (int j = 0; j < NUMBER_ITEMS; j++) q.poll();
            }).start();
        }

        Thread.sleep(SLEEP_TIME);
        assertEquals(q.size(), 1);
    }

    private void fillQueue() throws InterruptedException {
        for (int i = 0; i < THREADS_NUMBERS; i++) {
            new Thread(() -> {
                for (int j = 0; j < NUMBER_ITEMS; j++) q.offer(j);
            }).start();
        }
        Thread.sleep(SLEEP_TIME);
    }

    @Test
    public void shouldSameHead_whenPeekByThreads() throws InterruptedException {
        Integer expectedItem = new Random().nextInt();
        q.offer(expectedItem);

        for (int i = 0; i < THREADS_NUMBERS; i++) {
            new Thread(() -> {
                assertEquals(q.peek(), expectedItem);
            }).start();
        }
        Thread.sleep(SLEEP_TIME);
    }

    @Test
    public void shouldEmptyQueue_whenTwoThreadsCleanSimultaneously() throws InterruptedException {
        fillQueue();

        int numberOfThreads = 2;
        int i = 0;

        while(i++ != numberOfThreads)
            new Thread(() -> {
                q.clear();
            }).start();

        assertEquals(q.size(), 0);
    }

    @Test
    public void shouldReturnSameSize_whenAllThreadsGettingSize() throws InterruptedException {
        fillQueue();
        int expectedSize = q.size();

        for (int i = 0; i < THREADS_NUMBERS; i++) {
            new Thread(() -> {
                assertEquals(q.size(), expectedSize);
            }).start();
        }
    }

}
