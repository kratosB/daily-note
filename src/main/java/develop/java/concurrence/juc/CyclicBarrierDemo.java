package develop.java.concurrence.juc;

import java.util.Random;
import java.util.concurrent.*;

/**
 * @author admin
 */
public class CyclicBarrierDemo {

    public static void main(String[] args) {
        CyclicBarrier barrier = new CyclicBarrier(3,
                () -> System.out.println("======== all threads have arrived at the checkpoint ==========="));
        ExecutorService service = Executors.newFixedThreadPool(3);
        service.submit(new Traveler("Traveler1", barrier));
        service.submit(new Traveler("Traveler2", barrier));
        service.submit(new Traveler("Traveler3", barrier));
        service.shutdown();
    }

    private static class Traveler implements Runnable {

        private final String name;

        private final CyclicBarrier barrier;

        private static Random rand = new Random(47);

        Traveler(String name, CyclicBarrier barrier) {
            this.name = name;
            this.barrier = barrier;
        }

        @Override
        public void run() {
            try {
                TimeUnit.SECONDS.sleep(rand.nextInt(5));
                System.out.println(name + " arrived at Beijing.");
                barrier.await();
                TimeUnit.SECONDS.sleep(rand.nextInt(5));
                System.out.println(name + " arrived at Shanghai.");
                barrier.await();
                TimeUnit.SECONDS.sleep(rand.nextInt(5));
                System.out.println(name + " arrived at Guangzhou.");
                barrier.await();
            } catch (InterruptedException | BrokenBarrierException e) {
                e.printStackTrace();
            }

        }
    }
}
