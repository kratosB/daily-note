package develop.java.concurrence.juc.threadpool.forkjoin;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

/**
 * Created on 2020/5/7.
 *
 * @author zhiqiang bao
 */
public class RecursiveActionDemo {

    private final static int NUMBER = 10000000;

    static class ComputeTask extends RecursiveAction {

        final double[] array;

        final int lo, hi;

        ComputeTask(double[] array, int lo, int hi) {
            this.array = array;
            this.lo = lo;
            this.hi = hi;
        }

        @Override
        protected void compute() {
            int count = 2;
            if (hi - lo < count) {
                for (int i = lo; i < hi; ++i) {
                    array[i] = Math.sin(array[i]) + Math.cos(array[i]) + Math.tan(array[i]);
                }
            } else {
                int mid = (lo + hi) >>> 1;
                invokeAll(new ComputeTask(array, lo, mid), new ComputeTask(array, mid, hi));
            }
        }
    }

    public static void main(String[] args) {
        double[] array = new double[NUMBER];
        for (int i = 0; i < NUMBER; i++) {
            array[i] = i;
        }
        long startTime = System.currentTimeMillis();
        System.out.println(Runtime.getRuntime().availableProcessors());
        ForkJoinPool forkJoinPool = new ForkJoinPool();
        forkJoinPool.invoke(new ComputeTask(array, 0, array.length));
        long endTime = System.currentTimeMillis();
        System.out.println("Time span = " + (endTime - startTime));
    }
}
