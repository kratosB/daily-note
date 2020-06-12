package develop.java.concurrency.alternateprint;

/**
 * Created on 2020/6/12. 用自
 * https://blog.csdn.net/xiaokang123456kao/article/details/77331878，可以用，但是会有死锁问题，下面2中的方式更好。
 * 
 * @author zhiqiang bao
 */
class AlternatePrint123UsingSynchronized1 {

    public static class ThreadPrinter implements Runnable {

        private String name;

        private Object prev;

        private Object self;

        private ThreadPrinter(String name, Object prev, Object self) {
            this.name = name;
            this.prev = prev;
            this.self = self;
        }

        @Override
        public void run() {
            int count = 10;
            while (count > 0) {// 多线程并发，不能用if，必须使用whil循环
                synchronized (prev) { // 先获取 prev 锁
                    synchronized (self) {// 再获取 self 锁
                        System.out.print(name);// 打印
                        count--;

                        self.notifyAll();// 唤醒其他线程竞争self锁，注意此时self锁并未立即释放。
                    }
                    // 此时执行完self的同步块，这时self锁才释放。
                    try {
                        if (count == 0) {// 如果count==0,表示这是最后一次打印操作，通过notifyAll操作释放对象锁。
                            prev.notifyAll();
                        } else {
                            prev.wait(); // 立即释放 prev锁，当前线程休眠，等待唤醒
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        Object a = new Object();
        Object b = new Object();
        Object c = new Object();
        ThreadPrinter pa = new ThreadPrinter("A", c, a);
        ThreadPrinter pb = new ThreadPrinter("B", a, b);
        ThreadPrinter pc = new ThreadPrinter("C", b, c);

        new Thread(pa).start();
        // 保证初始ABC的启动顺序，如果三个线程一起启动，有死锁的可能（A获得c等a，B获得a等b，C获得b等c）。
        // 当然，由于cpu线程的调度，理论上死锁总是有可能会发生的。
        new Thread(pb).start();
        Thread.sleep(10);
        new Thread(pc).start();
        Thread.sleep(10);
    }
}

/**
 * 引用自
 * https://blog.csdn.net/weixin_42061805/article/details/92251917，这个例子，有blockingqueue的感觉。
 */
class AlternatePrint123UsingSynchronized2 {

    /**
     * 打印的状态
     */
    private int state = 0;

    private final Object objectA = new Object();

    private final Object objectB = new Object();

    private final Object objectC = new Object();

    public AlternatePrint123UsingSynchronized2() {
        super();
    }

    public void printA() {
        print("A", 0, objectA, objectB);
    }

    public void printB() {
        print("B", 1, objectB, objectC);
    }

    public void printC() {
        print("C", 2, objectC, objectA);
    }

    public void print(String name, int targetState, Object curr, Object next) {
        int times = 10;
        for (int i = 0; i < times;) {
            synchronized (curr) {
                while (state % 3 != targetState) {
                    try {
                        curr.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                i++;
                state++;
                System.out.print(name);
                synchronized (next) {
                    next.notify();
                }
            }
        }
    }

    public static void main(String[] args) {
        AlternatePrint123UsingSynchronized2 p = new AlternatePrint123UsingSynchronized2();
        new Thread(p::printA).start();
        new Thread(p::printB).start();
        new Thread(p::printC).start();
    }
}

/**
 * 引用自 https://www.cnblogs.com/jyx140521/p/6747750.html，notifyAll用得蛮巧。
 */
class AlternatePrint123UsingSynchronized3 {

    static int index = 1;

    public static void main(String[] args) {
        final Object lock = new Object();
        Runnable r1 = () -> {
            synchronized (lock) {
                for (int i = 0; i < 10; i++) {
                    while (index % 3 != 1) {
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    System.out.println("a");
                    index++;
                    lock.notifyAll();
                }
            }
        };
        Runnable r2 = () -> {
            synchronized (lock) {
                for (int i = 0; i < 10; i++) {
                    while (index % 3 != 2) {
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    System.out.println("b");
                    index++;
                    lock.notifyAll();
                }
            }
        };
        Runnable r3 = () -> {
            synchronized (lock) {
                for (int i = 0; i < 10; i++) {
                    while (index % 3 != 0) {
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    System.out.println("c");
                    index++;
                    lock.notifyAll();
                }
            }
        };
        new Thread(r1).start();
        new Thread(r2).start();
        new Thread(r3).start();
    }
}
