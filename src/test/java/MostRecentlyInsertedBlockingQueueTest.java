import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

public class MostRecentlyInsertedBlockingQueueTest extends QueuesTest {

    private final static int THREADS_NUMBERS = 20;

    private final static int NUMBER_ITEMS = 100;

    private final static int CAPACITY = THREADS_NUMBERS * NUMBER_ITEMS * 2;

    private final static int SLEEP_TIME = 50;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    MostRecentlyInsertedBlockingQueue<Integer> q;

    @Override
    Queue<Integer> initQueue(int capacity) {
        return new ConcurrentMostRecentlyInsertedQueue<>(capacity);
    }

    @Before
    public void createQueue() {
        q = new MostRecentlyInsertedBlockingQueue<>(CAPACITY);
    }

    @Test
    public void shouldNPE_whenDrainNull() {
        exception.expect(NullPointerException.class);
        q.drainTo(null);

        exception.expect(NullPointerException.class);
        q.drainTo(null, 0);
    }

    @Test
    public void shouldCorrectlyAddedAllItems_whenThreadsOfferItems() throws InterruptedException {
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

    //     public boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException {
    @Test
    public void shouldQueueBlocks_whenTakeOnEmptyQueue() throws InterruptedException {

        Thread thread = new Thread(() -> {
            try {
                q.take();
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
                q.poll(200, TimeUnit.MILLISECONDS);
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
                assertEquals(expectedItem, q.take());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        thread.start();

        Thread.sleep(SLEEP_TIME);
        q.put(expectedItem);
        assertEquals(1, q.size());

        Thread.sleep(SLEEP_TIME);
        assertEquals(0, q.size());
    }

//    @Test // TODO: test DrainTo
//    public void shouldIllegalArgumentException_whenDrainSameCollection() {
//        List<Integer> collection = new ArrayList<>();
//        q.drainTo(collection);
//
//        exception.expect(IllegalArgumentException.class);
//        q.drainTo(collection);
//
//        exception.expect(IllegalArgumentException.class);
//        q.drainTo(collection, 1);
//    }

}