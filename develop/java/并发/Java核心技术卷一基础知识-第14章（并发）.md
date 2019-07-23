# 并发

## 14.1 什么是线程
## 14.2 中断线程
## 14.3 线程状态

1. 线程有下面6种状态：
    1. New（新创建）
    2. Runnable（可运行）
    3. Blocked（被阻塞）
    4. Waiting（等待）
    5. Timed waiting（计时等待）
    6. Terminated（被终止）
    要确定一个线程的当前状态，可以调用getState方法

### 14.3.1 新创建线程

1. 当用new操作符创建一个新的线程时（如new Thread(r)），该线程还没开始运行，意味着它的状态是new。
2. 当一个线程处于新创建状态时，程序还没开始运行线程种的代码。
3. 运行前有一些准备工作。

### 14.3.2 可运行线程

1. 一旦调用start方法，线程处于runnable状态。
2. runnable的线程可能正在运行，也可能没有运行，取决于操作系统给线程提供运行的时间。

### 14.3.3 被阻塞线程和等待线程

1. 当线程处于被阻塞，或者等待状态时，它暂时不活动。它不运行任何代码，且消好最少的资源，直到线程调度器重新激活它。
2. 当一个线程试图获取一个内部的对象锁（不是JUC库中的锁），而该锁被其他线程持有，则该线程进入阻塞状态。当所有其他线程释放这个锁，并且线程调度器允许本线程持有它的时候，该线程讲变成非阻塞状态。
3. 当线程等待另一个线程通知调度器一个条件时，它自己进入等待状态。在调用Object.wait方法或者Thread.join方法，或者是等待JUC库中的Lock或Condition时，就会出现这种情况
>被阻塞状态与等待状态，是有很大不同的。
4. 有几个方法有一个超时参数。调用它们导致线程进入计时等待（timed waiting）状态。这一状态将一直保持到超时期满，或者接收到适当的通知。带有超时参数的方法有Thread.sleep和Object.wait，Thread.join，Lock.tryLock以及Condition.await的计时版本。

### 14.3.4 被终止的线程

1. 被终止的原因如下：
    1. 因为run方法正常退出而自然死亡。
    2. 因为一个没有被捕获的异常终止了run方法而意外死亡。

## 14.4 线程属性

### 14.4.1 线程优先级

1. 每个线程都有一个优先级。默认情况下，继承父线程的优先级。
2. 优先级高度依赖与系统，可变，所以不可靠（基本没啥用）。

### 14.4.2 守护线程

1. 可以通过调用`t.setDaemon(true);`将线程转换成守护线程。

### 14.4.1 未捕获异常处理器


## 14.5 同步

1. 如果两个线程存取相同的对象，并且每一个线程都调用了一个修改该对象状态的方法，将会产生讹误的对象。这种情况被称为竞争条件（race condition）。

### 14.5.1 竞争条件的一个例子

1. 模拟一个有若干帐户的银行，随机发生在这些帐户之间转移钱款的交易。
2. 创建多个线程，同时随机的在这些帐户之间转移资金。
3. 一定时间之后，停止转移，发现银行中所有帐户的总金额与开始的时候不一致。

### 14.5.2 竞争条件详解

1. 假定两个线程同时执行`accounts = accounts + amount;`这句指令，就会出错。因为这不是一个原子操作。
2. `accounts = accounts + amount;`这个操作实际上被分为三个步骤：
    1. 将accounts加载到寄存器
    2. 给寄存器里的值增加amount
    3. 把增加之后的值写回accounts
3. 假设同时有两个线程执行了2.1，那么他们获得的值是一样的，然后两个线程再各自给accounts增加一个amount，再回写。其中一个线程的操作就会被覆盖，于是总金额就出错了。

### 14.5.3 锁对象

1. 有两种方法防止代码块受并发访问的干扰。
    1. synchronized关键字。
    2. ReentrantLock类。
        1. lock()方法。
        2. unlock()方法。

### 14.5.4 条件对象

大概看了，不太好总结，而且感觉不太重要。

### 14.5.5 synchronized关键字

1. 如果一个方法用synchronized关键字声明，那么对象的锁将保护整个方法。

    ```
    public synchronized void method() {
        method body
    }
    ```
    等价于
    ```
    public void method() {
        this.intrinsicLock.lock();
        try {
            method body
        }
        finally {
            tiis.intrinsicLock.unlock();
        }
    }
    ```
    
    synchronized中的wait和notifyAll等价于条件对象中的`intrinsicCondition.await()`和`intrinsicCondition.signalAll()`。
2. 将静态方法声明为synchronized，该方法获得相关的类对象的内部锁。

    例如，Bank类有一个静态同步的方法，那么当该方法被调用时，Bank.class对象的锁被锁住。因此，没有其他线程可以调用同一个类的这个或任何其他的同步静态方法。

### 14.5.6 同步阻塞

1. 除了调用同步方法获得锁，通过进入同步阻塞，也能获得锁。
    ```
    synchronized(obj) {
        body
    }
    ```
    在这里，获得obj对象的锁的线程，才能继续执行，其他线程进入阻塞。

### 14.5.7 监视器概念

感觉不重要，没看

### 14.5.8 Volatile域

1. 多处理器环境下，有于寄存器和本地缓存的存在，不同线程的同一个内存位置的值可能不一样。volatile可以解决这个问题，称为可见性。
2. 编译器可以改变指令的顺序使吞吐量最大化，被称为“指令的重排序优化”。多线程环境下可能会出错。volatile可以解决这个问题，称为有序性。
3. volatile不能保证原子性。

### 14.5.9 final变量

1. 使用final修饰的对象，其他线程会在构造函数完成构造之后才看到。

### 14.5.10 原子性

1. 加锁可以保证原子性。
2. JUC包中的类使用了高效的机器指令，可以保证原子性，例如incrementAndGet和compareAndSet。
3. 比较+修改的操作，就是乐观锁思想。
4. 如果有大量线程要访问相同的原子值，性能会大幅下降，因为重试次数太多。Java8提供了LongAdder和LongAccumulator来解决这个问题。
    1. 原理是，按照线程数量，把值分为好几个值，每个线程更新自己的一个值，需要总值的时候再获取总值。

### 14.5.11 死锁

1. 线程被阻塞，却又获取不到锁（锁被被阻塞的线程拥有），就会发生死锁。
2. 或者线程在等待条件，条件永远没满足，也会发生死锁。
3. 遗憾的是，Java 编程语言中没有任何东西可以避免或打破这种死锁现象。必须仔细设计程序， 以确保不会出现死锁。

### 14.5.12 线程局部变量

1. 可以使用ThreadLocal。

### 14.5.13 锁测试与超时

1. 在申请锁的时候，可能会发生阻塞，所以要慎重。可以使用`tryLock()`方法尝试申请一个锁，成功后会返回true。
    ```
    if (myLock.tryLock()) {
        try
        finally
    } else {
        body
    }
    ```
2. `tryLock()`方法还可以带一个超时参数
    >条件等待的时候也可以带超时参数

### 14.5.14 读/写锁

1. java.util.concurrent.locks包定义了两个锁类：
    1. ReentrantLock：前面介绍过了
    2. ReentrantReadWriteLock：如果很多线程从一个数据结构读取数据而很少线程修改其中数据的话，是十分有用的。
        1. 允许对读线程共享访问。
        2. 写线程依然必须是互斥访问的
        ```
        private ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
        private Lock readLock = rwl.readLock();
        private Lock writeLock = rwl.writeLock();
        // 加读锁
        public double getTotalBalance() {
            readLock.lock();
            try {}
            finally {readLock.unlock();}
        }
        // 加写锁
        public double transfer() {
            writeLock.lock();
            try {}
            finally {writeLock.unlock();}
        }
        ```

### 14.5.15 为什么放弃stop和suspend方法

1. stop会导致对象状态不一样。比方说打破原子性？
2. 如果用`suspend`挂起一个持有一个锁的线程，那么，该锁在恢复之前是不可用的。所以可能导致死锁或者阻塞。
3. 其他没怎么看。

## 14.6 阻塞队列

1. 对于许多线程问题，可以通过使用一个或多个队列实现。

    生产者线程向队列插入元素，消费者取出并使用。使用队列，可以安全地从一个线程向另一个线程传递数据。
2. 当试图向队列添加元素而队列已满，或是想从队列移出元素而队列为空的时候，阻塞队列导致线程阻塞。
3. 这里的阻塞队列是JUC包下面的BlockingQueue。
    1. ArrayBlockingQueue
    2. LinkedBlockingQueue
    3. DelayQueue
    4. PriorityBlockingQueue
    5. SynchronousQueue
    6. ArrayBlockingQueue
>**未完待续**

## 14.7 线程安全的集合

1. 如果多线程要并发修改一个数据结构，例如ArrayList和HashMap，很容易出现错误。可以通过加锁来避免这些错误，但是线程安全的集合也是一种好的选择。

### 14.7 高效的映射、集和队列

1. java.util.concurrent包提供了映射，有续集和队列的高效实现：
    1. ConcurrentHashMap
    2. ConcurrentSkipListMap
    3. ConcurrentSkipListSet
    4. ConcurrentLinkedQueue
2. 

## 14.8 Callable与Future
## 执行器
## 同步器
## 线程与Swing





















