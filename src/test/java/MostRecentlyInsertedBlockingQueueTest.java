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

    private MostRecentlyInsertedBlockingQueue<Integer> blockingQueue;

    @Override
    Queue<Integer> initQueue(int capacity) {
        return new MostRecentlyInsertedBlockingQueue<Integer>(capacity);
    }

    @Before
    public void createQueue() {
        blockingQueue = new MostRecentlyInsertedBlockingQueue<Integer>(CAPACITY);
    }

    @Test
    public void shouldThrowNPE_whenDrainNull() {
        exception.expect(NullPointerException.class);
        blockingQueue.drainTo(null);
    }

    @Test
    public void shouldThrowNPE_whenDrainMaxItemsWithNull() {
        exception.expect(NullPointerException.class);
        blockingQueue.drainTo(null, 1);
    }


    @Test
    public void shouldCorrectlyAddedAllItems_whenThreadsOfferItems() throws InterruptedException {
        fillQueue();

        Thread.sleep(SLEEP_TIME);
        assertEquals(blockingQueue.size(), THREADS_NUMBERS * NUMBER_ITEMS);
    }

    @Test
    public void shouldCorrectSize_whenPollItemsByThreads() throws InterruptedException {
        fillQueue();

        Integer additionalItem = new Random().nextInt();
        blockingQueue.offer(additionalItem);
        int initialSize = THREADS_NUMBERS * NUMBER_ITEMS + 1;
        int expectedSize = 1;

        assertEquals(blockingQueue.size(), initialSize);

        for (int i = 0; i < THREADS_NUMBERS; i++) {
            new Thread(() -> {
                for (int j = 0; j < NUMBER_ITEMS; j++) blockingQueue.poll();
            }).start();
        }

        Thread.sleep(SLEEP_TIME);
        assertEquals(blockingQueue.size(), expectedSize);
    }

    private void fillQueue() throws InterruptedException {
        for (int i = 0; i < THREADS_NUMBERS; i++) {
            new Thread(() -> {
                for (int j = 0; j < NUMBER_ITEMS; j++) blockingQueue.offer(j);
            }).start();
        }
        Thread.sleep(SLEEP_TIME);
    }

    @Test
    public void shouldQueueBlocks_whenTakeOnEmptyQueue() throws InterruptedException {

        Thread thread = new Thread(() -> {
            try {
                blockingQueue.take();
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
                blockingQueue.poll(SLEEP_TIME * 2, TimeUnit.MILLISECONDS);
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
                assertEquals(expectedItem, blockingQueue.take());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        thread.start();

        Thread.sleep(SLEEP_TIME);
        blockingQueue.put(expectedItem);
        assertEquals(1, blockingQueue.size());

        Thread.sleep(SLEEP_TIME);
        assertEquals(0, blockingQueue.size());
    }

    @Test
    public void shouldThrowIllegalArgumentException_whenDrainSameCollection() {
        blockingQueue.put(new Random().nextInt());

        exception.expect(IllegalArgumentException.class);
        blockingQueue.drainTo(blockingQueue);
    }

    @Test
    public void shouldThrowIllegalArgumentException_whenDrainWithMaxItemsOnSameCollection() {
        blockingQueue.put(new Random().nextInt());

        exception.expect(IllegalArgumentException.class);
        blockingQueue.drainTo(blockingQueue, 1);
    }

    @Test
    public void shouldFalse_whenContainsOnNull() {
        assertFalse(blockingQueue.contains(null));
    }

    @Test
    public void shouldTrue_whenQueueContainsItem() {
        Random random = new Random();
        Integer expected = random.nextInt();

        blockingQueue.put(random.nextInt());
        blockingQueue.put(random.nextInt());
        blockingQueue.put(expected);

        assertTrue(blockingQueue.contains(expected));
    }

}