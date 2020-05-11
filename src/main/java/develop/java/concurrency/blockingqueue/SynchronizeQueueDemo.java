package develop.java.concurrency.blockingqueue;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created on 2020/5/11.
 *
 * @author zhiqiang bao
 */
public class SynchronizeQueueDemo {

    public static void main(String[] args) {
        SynchronousQueue<String> queue = new SynchronousQueue<>(false);
        ExecutorService service = Executors.newCachedThreadPool();
        int count = 5;
        for (int i = 0; i < count; i++) {
            service.submit(new Producer(queue, "Producer" + i));
        }
        for (int i = 0; i < count; i++) {
            service.submit(new Consumer(queue, "Consumer" + i));
        }
        service.shutdown();
    }

    static class Producer implements Runnable {

        private final SynchronousQueue<String> queue;

        private final String name;

        private static Random rand = new Random(47);

        private static AtomicInteger productID = new AtomicInteger(0);

        Producer(SynchronousQueue<String> queue, String name) {
            this.queue = queue;
            this.name = name;
        }

        @Override
        public void run() {
            try {
                int count = 5;
                for (int i = 0; i < count; i++) {
                    TimeUnit.SECONDS.sleep(rand.nextInt(5));
                    String str = "Product" + productID.incrementAndGet();
                    queue.put(str);
                    System.out.println(name + " put " + str);
                }
                System.out.println(name + " is over.");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    static class Consumer implements Runnable {

        private final SynchronousQueue<String> queue;

        private final String name;

        Consumer(SynchronousQueue<String> queue, String name) {
            this.queue = queue;
            this.name = name;
        }

        @Override
        public void run() {
            try {
                int count = 5;
                for (int i = 0; i < count; i++) {
                    String str = queue.take();
                    System.out.println(name + " take " + str);
                }
                System.out.println(name + " is over.");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
