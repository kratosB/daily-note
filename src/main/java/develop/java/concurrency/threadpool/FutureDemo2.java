package develop.java.concurrency.threadpool;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Created on 2020/5/9.
 *
 * @author zhiqiang bao
 */
public class FutureDemo2 {

    private static class TaskInteger implements Callable<Integer> {

        private final int sum;

        TaskInteger(int sum) {
            this.sum = sum;
        }

        @Override
        public Integer call() throws Exception {
            TimeUnit.SECONDS.sleep(sum);
            return sum * sum;
        }
    }

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        ExecutorService service = Executors.newCachedThreadPool();
        CompletionService<Integer> completionService = new ExecutorCompletionService<>(service);
        int count = 5;
        for (int i = 0; i < count; i++) {
            completionService.submit(new TaskInteger(i));
        }
        service.shutdown();
        // will block
        for (int i = 0; i < count; i++) {
            Future<Integer> future = completionService.take();
            System.out.println(future.get());
        }

    }
}
