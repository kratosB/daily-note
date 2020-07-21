package develop.java.concurrency.alternateprint;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created on 2020/6/12.
 *
 * @author zhiqiang bao
 */
public class AlternatePrint123UsingCondition {

    public static void main(String[] args) {
        final Lock lock = new ReentrantLock();
        final Condition condition1 = lock.newCondition();
        final Condition condition2 = lock.newCondition();
        final Condition condition3 = lock.newCondition();
        int count = 5;
        Runnable r1 = () -> {
            lock.lock();
            try {
                for (int i = 0; i < count; i++) {
                    condition1.await();
                    System.out.println("a");
                    condition2.signal();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        };
        Runnable r2 = () -> {
            lock.lock();
            try {
                for (int i = 0; i < count; i++) {
                    condition2.await();
                    System.out.println("b");
                    condition3.signal();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        };
        Runnable r3 = () -> {
            lock.lock();
            try {
                for (int i = 0; i < count; i++) {
                    condition3.await();
                    System.out.println("c");
                    condition1.signal();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        };
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        executorService.execute(r1);
        executorService.execute(r2);
        executorService.execute(r3);
        executorService.shutdown();
        try {
            lock.lock();
            Thread.sleep(1000);
            condition1.signal();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }
}

/**
 * 1个lock+1个condition的方法，参考AlternatePrint123UsingSynchronized3写的，用lock代替synchronized，signall代替notifyall。
 * 跟 https://www.cnblogs.com/jyx140521/p/6747750.html 中的1个condition方法的基本一致。
 */
class AlternatePrint123UsingCondition2 {

    static int index = 0;

    public static void main(String[] args) {
        Lock lock = new ReentrantLock();
        Condition condition = lock.newCondition();
        int count = 5;
        int value = 3;
        Runnable r1 = () -> {
            lock.lock();
            try {
                for (int i = 0; i < count; i++) {
                    while (index % value != 0) {
                        condition.await();
                    }
                    System.out.println("a, index = " + index);
                    index++;
                    condition.signalAll();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        };
        Runnable r2 = () -> {
            lock.lock();
            try {
                for (int i = 0; i < count; i++) {
                    while (index % value != 1) {
                        condition.await();
                    }
                    System.out.println("b, index = " + index);
                    index++;
                    condition.signalAll();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        };
        Runnable r3 = () -> {
            lock.lock();
            try {
                for (int i = 0; i < count; i++) {
                    while (index % value != 2) {
                        condition.await();
                    }
                    System.out.println("c, index = " + index);
                    index++;
                    condition.signalAll();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        };
        ExecutorService executorService = Executors.newFixedThreadPool(value);
        executorService.execute(r1);
        executorService.execute(r2);
        executorService.execute(r3);
        executorService.shutdown();
    }
}