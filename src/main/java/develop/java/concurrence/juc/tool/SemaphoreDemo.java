package develop.java.concurrence.juc.tool;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * @author admin
 */
public class SemaphoreDemo {

    public static void main(String[] args) {
        Semaphore semaphore = new Semaphore(3);
        ExecutorService service = Executors.newCachedThreadPool();
        int count = 10;
        for (int i = 0; i < count; i++) {
            service.submit(new Car("Car" + i, semaphore));
        }
        service.shutdown();
    }

    private static class Car implements Runnable {

        private String name;

        private Semaphore semaphore;

        private static Random random = new Random(System.currentTimeMillis());

        Car(String name, Semaphore semaphore) {
            this.name = name;
            this.semaphore = semaphore;
        }

        @Override
        public void run() {
            System.out.println(name + " is waiting for a permit");
            try {
                semaphore.acquire();
                System.out.println(name + "get a permit to access, available permits = " + semaphore.availablePermits());
                TimeUnit.SECONDS.sleep(random.nextInt(5));
                System.out.println(name + " release a permit, available permits:" + semaphore.availablePermits());
                semaphore.release();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
