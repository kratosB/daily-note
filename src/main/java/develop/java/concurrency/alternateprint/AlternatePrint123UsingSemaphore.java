package develop.java.concurrency.alternateprint;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Created on 2020/6/11.
 *
 * @author zhiqiang bao
 */
public class AlternatePrint123UsingSemaphore {

    public static void main(String[] args) {
        Semaphore semaphoreA = new Semaphore(1);
        Semaphore semaphoreB = new Semaphore(0);
        Semaphore semaphoreC = new Semaphore(0);
        ExecutorService exe = Executors.newCachedThreadPool();
        int count = 5;
        Runnable runnableA = () -> {
            try {
                for (int i = 0; i < count; i++) {
                    semaphoreA.acquire();
                    TimeUnit.SECONDS.sleep(1);
                    System.out.print("1");
                    semaphoreB.release();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };
        Runnable runnableB = () -> {
            try {
                for (int i = 0; i < count; i++) {
                    semaphoreB.acquire();
                    TimeUnit.SECONDS.sleep(1);
                    System.out.print("2");
                    semaphoreC.release();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };
        Runnable runnableC = () -> {
            try {
                for (int i = 0; i < count; i++) {
                    semaphoreC.acquire();
                    TimeUnit.SECONDS.sleep(1);
                    System.out.print("3");
                    semaphoreA.release();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };
        exe.execute(runnableA);
        exe.execute(runnableB);
        exe.execute(runnableC);
        exe.shutdown();
    }
}
