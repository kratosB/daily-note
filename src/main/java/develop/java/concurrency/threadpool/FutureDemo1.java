package develop.java.concurrency.threadpool;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Created on 2020/5/8.
 *
 * @author zhiqiang bao
 */
public class FutureDemo1 {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        ExecutorService service = Executors.newCachedThreadPool();
        Future<Integer> future = service.submit(() -> {
            System.out.println("Callable is running");
            TimeUnit.SECONDS.sleep(2);
            return 47;
        });
        service.shutdown();
        System.out.println("future.get = " + future.get());
    }
}
