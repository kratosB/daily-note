package develop.java.concurrency.blockingqueue;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created on 2020/5/11.
 *
 * @author zhiqiang bao
 */
public class LinkedBlockingQueueDemo {

    public static void main(String[] args) {
        BlockingQueue<String> blockingQueue = new LinkedBlockingQueue<>(3);
        ExecutorService service = Executors.newCachedThreadPool();
        int count = 5;
        for (int i = 0; i < count; i++) {
            service.submit(new Producer("Producer" + i, blockingQueue));
        }
        for (int i = 0; i < count; i++) {
            service.submit(new Consumer("Consumer" + i, blockingQueue));
        }
        service.shutdown();
    }

    private static class Producer implements Runnable {

        private final String name;

        private final BlockingQueue<String> blockingQueue;

        private static Random rand = new Random(47);

        private static AtomicInteger productID = new AtomicInteger(0);

        Producer(String name, BlockingQueue<String> blockingQueue) {
            this.name = name;
            this.blockingQueue = blockingQueue;
        }

        @Override
        public void run() {
            try {
                int count = 10;
                for (int i = 0; i < count; i++) {
                    SECONDS.sleep(rand.nextInt(5));
                    String str = "Product" + productID.getAndIncrement();
                    blockingQueue.put(str);
                    // 注意，这里得到的size()有可能是错误的
                    System.out.println(name + " product " + str + ", queue size = " + blockingQueue.size());
                }
                System.out.println(name + " is over");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static class Consumer implements Runnable {

        private final String name;

        private final BlockingQueue<String> blockingQueue;

        private static Random rand = new Random(47);

        Consumer(String name, BlockingQueue<String> blockingQueue) {
            this.name = name;
            this.blockingQueue = blockingQueue;
        }

        @Override
        public void run() {
            try {
                int count = 10;
                for (int i = 0; i < count; i++) {
                    SECONDS.sleep(rand.nextInt(5));
                    String str = blockingQueue.take();
                    // 注意，这里得到的size()有可能是错误的
                    System.out.println(name + " consume " + str + ", queue size = " + blockingQueue.size());
                }
                System.out.println(name + " is over");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
