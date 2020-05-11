package develop.java.concurrency.threadpool;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created on 2020/5/9.
 *
 * @author zhiqiang bao
 */
public class ScheduledExecutorServiceDemo1 {

    public static void main(String[] args) {
        ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(2);
        ScheduledFuture<?> scheduledFuture = scheduler.scheduleAtFixedRate(() -> System.out.println("beep"), 1, 1,
                TimeUnit.SECONDS);
        scheduler.schedule(() -> {
            System.out.println("cancel beep");
            scheduledFuture.cancel(true);
        }, 4, TimeUnit.SECONDS);
        scheduler.schedule(scheduler::shutdown, 6, TimeUnit.SECONDS);
    }
}
