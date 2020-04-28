import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class SemaphoreExam {

    public static void main(String[] args) {
        Semaphore semaphore = new Semaphore(3);
        ExecutorService service = Executors.newCachedThreadPool();
        for (int i = 0; i < 10; i++) {
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
