import org.junit.Before;
import org.junit.Test;

import java.util.Queue;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class MostRecentlyInsertedBlockingQueueTest extends QueuesTest {

    private final static int THREADS_NUMBERS = 20;

    private final static int NUMBER_ITEMS = 100;

    private final static int CAPACITY = THREADS_NUMBERS * NUMBER_ITEMS * 2;

    private final static int SLEEP_TIME = 50;

    MostRecentlyInsertedBlockingQueue<Integer> intQueue;

    @Override
    Queue<Integer> initQueue(int capacity) {
        return new ConcurrentMostRecentlyInsertedQueue<>(capacity);
    }

    @Before
    public void createQueue() {
        intQueue = new MostRecentlyInsertedBlockingQueue<>(CAPACITY);
    }

    @Test
    public void shouldNPE_whenDrainNull() {
        exception.expect(NullPointerException.class);
        intQueue.drainTo(null);
    }

    @Test
    public void shouldNPE_whenDrainMaxItemsWithNull() {
        exception.expect(NullPointerException.class);
        intQueue.drainTo(null, 1);
    }


    @Test
    public void shouldCorrectlyAddedAllItems_whenThreadsOfferItems() throws InterruptedException {
        fillQueue();

        Thread.sleep(SLEEP_TIME);
        assertEquals(intQueue.size(), THREADS_NUMBERS * NUMBER_ITEMS);
    }

    @Test
    public void shouldCorrectSize_whenPollItemsByThreads() throws InterruptedException {
        fillQueue();

        Integer additionalItem = new Random().nextInt();
        intQueue.offer(additionalItem);
        int initialSize = THREADS_NUMBERS * NUMBER_ITEMS + 1;
        int expectedSize = 1;

        assertEquals(intQueue.size(), initialSize);

        for (int i = 0; i < THREADS_NUMBERS; i++) {
            new Thread(() -> {
                for (int j = 0; j < NUMBER_ITEMS; j++) intQueue.poll();
            }).start();
        }

        Thread.sleep(SLEEP_TIME);
        assertEquals(intQueue.size(), expectedSize);
    }

    private void fillQueue() throws InterruptedException {
        for (int i = 0; i < THREADS_NUMBERS; i++) {
            new Thread(() -> {
                for (int j = 0; j < NUMBER_ITEMS; j++) intQueue.offer(j);
            }).start();
        }
        Thread.sleep(SLEEP_TIME);
    }

    //     public boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException {
    @Test
    public void shouldQueueBlocks_whenTakeOnEmptyQueue() throws InterruptedException {

        Thread thread = new Thread(() -> {
            try {
                intQueue.take();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        thread.start();
        Thread.sleep(SLEEP_TIME);
        assertEquals(Thread.State.valueOf("WAITING"), thread.getState());
    }

    @Test
    public void shouldQueueBlocks_whenPollWithTimeoutOnEmpty() throws InterruptedException {
        Thread thread = new Thread(() -> {
            try {
                intQueue.poll(SLEEP_TIME * 2, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        thread.start();
        Thread.sleep(SLEEP_TIME);
        assertEquals(Thread.State.valueOf("TIMED_WAITING"), thread.getState());
    }

    @Test
    public void shouldTakeItem_whenQueueBecomesNotEmpty() throws InterruptedException {
        Integer expectedItem = new Random().nextInt();

        Thread thread = new Thread(() -> {
            try {
                assertEquals(expectedItem, intQueue.take());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        thread.start();

        Thread.sleep(SLEEP_TIME);
        intQueue.put(expectedItem);
        assertEquals(1, intQueue.size());

        Thread.sleep(SLEEP_TIME);
        assertEquals(0, intQueue.size());
    }

    @Test
    public void shouldIllegalArgumentException_whenDrainSameCollection() {
        intQueue.put(new Random().nextInt());

        exception.expect(IllegalArgumentException.class);
        intQueue.drainTo(intQueue);
    }

    @Test
    public void shouldIllegalArgumentException_whenDrainWithMaxItemsOnSameCollection() {
        intQueue.put(new Random().nextInt());

        exception.expect(IllegalArgumentException.class);
        intQueue.drainTo(intQueue, 1);
    }

    @Test
    public void shouldFalse_whenContainsOnNull() {
        assertFalse(intQueue.contains(null));
    }

    @Test
    public void shouldTrue_whenQueueContainsItem() {
        Random random = new Random();
        Integer expected = random.nextInt();

        intQueue.put(random.nextInt());
        intQueue.put(random.nextInt());
        intQueue.put(expected);

        assertTrue(intQueue.contains(expected));
    }

}