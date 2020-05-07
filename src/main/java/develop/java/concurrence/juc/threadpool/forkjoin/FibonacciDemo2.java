package develop.java.concurrence.juc.threadpool.forkjoin;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

/**
 * 用RecursiveAction求斐波那契数列的例子。 Created on 2020/5/7.
 *
 * @author zhiqiang bao
 */
public class FibonacciDemo2 {

    static class Fibonacci extends RecursiveAction {

        int n;

        int m;

        public Fibonacci(int n) {
            this.n = n;
        }

        // 主要的实现逻辑都在compute()里
        @Override
        protected void compute() {
            // 这里先假设 n >= 0
            if (n <= 1) {
                m = n;
            } else {
                Fibonacci f1 = new Fibonacci(n - 1);
                Fibonacci f2 = new Fibonacci(n - 2);
                f1.fork();
                f2.fork();
                f1.join();
                f2.join();
                m = f1.m + f2.m;
            }
        }
    }

    public static void main(String[] args) {
        ForkJoinPool forkJoinPool = new ForkJoinPool();
        System.out.println("CPU核数：" + Runtime.getRuntime().availableProcessors());
        long start = System.currentTimeMillis();
        Fibonacci fibonacci = new Fibonacci(10);
        forkJoinPool.invoke(fibonacci);
        System.out.println(fibonacci.m);
        long end = System.currentTimeMillis();
        System.out.println(String.format("耗时：%d millis", end - start));
    }

}
