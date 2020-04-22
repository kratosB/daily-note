# [深入浅出Java多线程](https://github.com/RedSpider1/concurrent)

## 基础篇

### 1. 进程与线程的基本概念

意义不大，略

### 2. Java多线程入门类和接口

#### 2.1 Thread类和Runnable接口

1. Thread类构造方法。常用的就两个。
    1. Thread(Runnable target)
    2. Thread(Runnable target, String name)
2. Thread类的几个常用方法。
    1. currentThread()：静态方法，返回对当前正在执行的线程对象的引用。
    2. start()：开始执行线程的方法，java虚拟机会调用线程内的run()方法。
    3. yield()：yield在英语里有放弃的意思，同样，这里的yield()指的是当前线程愿意让出对当前处理器的占用。这里需要注意的是，就算当前线程调用了yield()方法，程序在调度的时候，也还有可能继续运行这个线程的。
    4. sleep()：静态方法，使当前线程睡眠一段时间。
    5. join()：使当前线程等待另一个线程执行完毕之后再继续执行，内部调用的是Object类的wait方法实现的。
3. Thread类与Runnable接口的比较。
    1. 由于Java“单继承，多实现”的特性，Runnable接口使用起来比Thread更灵活。
    2. Runnable接口出现更符合面向对象，将线程单独进行对象的封装。
    3. Runnable接口出现，降低了线程对象和线程任务的耦合性。
    4. 如果使用线程时不需要使用Thread类的诸多方法，显然使用Runnable接口更为轻量。

#### 2.2 Callable、Future与FutureTask

1. Callable一般是配合线程池工具ExecutorService来使用的。ExecutorService可以使用submit方法来让一个Callable接口执行。它会返回一个Future，我们后续的程序可以通过这个Future的get方法得到结果。
2. Future接口的`cancel`方法，是试图取消一个线程的执行。试图取消，并不一定能取消成功。
    >有时候，为了让任务有能够取消的功能，就使用Callable来代替Runnable。
3. Future接口有一个实现类叫FutureTask。FutureTask是实现的RunnableFuture接口的，而RunnableFuture接口同时继承了Runnable接口和Future接口。
    >callable的本质，其实就是runnable。执行的时候执行的是FutureTask的run()方法，FutureTask的run()方法里面调用了callable的call()方法。

### 3. 线程组和线程优先级

意义不大，略

### 4. Java线程的状态及主要转化方法

#### 4.1 操作系统中的线程状态转换

![系统进程状态转换图](http://concurrent.redspider.group/article/01/imgs/%E7%B3%BB%E7%BB%9F%E8%BF%9B%E7%A8%8B%E7%8A%B6%E6%80%81%E8%BD%AC%E6%8D%A2%E5%9B%BE.png)

1. 操作系统线程主要有以下三个状态。
    1. 就绪状态(ready)：线程正在等待使用CPU，经调度程序调用之后可进入running状态。
    2. 执行状态(running)：线程正在使用CPU。
    3. 等待状态(waiting): 线程经过等待事件的调用或者正在等待其他资源（如I/O）。

#### 4.2 Java线程的6个状态

1. NEW。
    1. 线程刚刚new出来，还没有start的状态。
    2. 一个线程不能start两次。调用一次之后，threadStatus会改变，第二次调会抛出异常。
2. RUNNABLE。
    1. 表示当前线程正在运行中。可能是操作系统中的ready和running两种状态。
    2. running：处于RUNNABLE状态的线程在Java虚拟机中运行。
    3. ready：也有可能在等待其他系统资源（比如I/O）。
3. BLOCKED。
    1. 阻塞状态。处于BLOCKED状态的线程正等待锁的释放以进入同步区。
4. WAITING。
    1. 等待状态。处于等待状态的线程变成RUNNABLE状态需要其他线程唤醒。
    2. 调用如下3个方法会使线程进入等待状态：
        1. Object.wait()：使当前线程处于等待状态直到另一个线程唤醒它。
        2. Thread.join()：等待线程执行完毕，底层调用的是Object实例的wait方法。
        3. LockSupport.park()：除非获得调用许可，否则禁用当前线程进行线程调度。
5. TIMED_WAITING。
    1. 超时等待状态。线程等待一个具体的时间，时间到后会被自动唤醒。
    2. 调用如下方法会使线程进入超时等待状态：
        1. Thread.sleep(long millis)：使当前线程睡眠指定时间。
        2. Object.wait(long timeout)：线程休眠指定时间，等待期间可以通过notify()/notifyAll()唤醒。
        3. 等待当前线程最多执行millis毫秒，如果millis为0，则会一直执行。
        4. LockSupport.parkNanos(long nanos)： 除非获得调用许可，否则禁用当前线程进行线程调度指定时间。
        5. LockSupport.parkUntil(long deadline)：同上，也是禁止线程进行调度指定时间。
6. TERMINATED。
    1. 终止状态。此时线程已执行完毕。

#### 4.3 线程状态的转换

![线程状态转换图](http://concurrent.redspider.group/article/01/imgs/%E7%BA%BF%E7%A8%8B%E7%8A%B6%E6%80%81%E8%BD%AC%E6%8D%A2%E5%9B%BE.png)

1. WAITING状态与RUNNABLE状态的转换。
    1. Object.wait()
        1. 调用wait()方法前线程必须持有对象的锁。
        2. 线程调用wait()方法时，会释**放当前的锁**，直到有其他线程调用notify()/notifyAll()方法唤醒等待锁的线程。
        3. 需要注意的是，其他线程调用notify()方法只会唤醒单个等待锁的线程，如有有多个线程都在等待这个锁的话不一定会唤醒到之前调用wait()方法的线程。
        4. 同样，调用notifyAll()方法唤醒所有等待锁的线程之后，也不一定会马上把时间片分给刚才放弃锁的那个线程，具体要看系统的调度。
    2. Thread.join()
        1. 调用join()方法不会释放锁，会一直等待当前线程执行完毕（转换为TERMINATED状态）。
2. TIMED_WAITING与RUNNABLE状态转换。
    1. Thread.sleep(long)
        1. 使当前线程睡眠指定时间。需要注意这里的“睡眠”只是暂时使线程停止执行，并不会释放锁。时间到后，线程会重新进入RUNNABLE状态。
    2. Object.wait(long)
        1. wait(long)方法使线程进入TIMED_WAITING状态。这里的wait(long)方法与无参方法wait()相同的地方是，都可以通过其他线程调用notify()或notifyAll()方法来唤醒。
        2. 不同的地方是，有参方法wait(long)就算其他线程不来唤醒它，经过指定时间long之后它会自动唤醒，拥有去争夺锁的资格。
    3. Thread.join(long)
        1. join(long)使当前线程执行指定时间，并且使线程进入TIMED_WAITING状态。
3. 线程中断。
    1. 目前在Java里还没有安全直接的方法来停止线程。
    2. Java提供了线程中断机制来处理需要中断线程的情况。通过中断操作并不能直接终止一个线程，而是通知需要被中断的线程自行处理。
    3. 关于线程中断的几个方法。
        1. Thread.interrupt()：中断线程。这里的中断线程并不会立即停止线程，而是设置线程的中断状态为true（默认是flase）。
        2. Thread.interrupted()：测试当前线程是否被中断。线程的中断状态受这个方法的影响，意思是调用一次使线程中断状态设置为true，连续调用两次会使得这个线程的中断状态重新转为false。
        3. Thread.isInterrupted()：测试当前线程是否被中断。与上面方法不同的是调用这个方法并不会影响线程的中断状态。
        4. 线程中断的状态被设置为true，但是具体被要求中断的线程要怎么处理，完全由被中断线程自己而定，可以在合适的实际处理中断请求，也可以完全不处理继续执行下去。

### 5. Java线程间的通信

#### 5.1 锁与同步

意义不大，略

#### 5.2 等待/通知机制

1. 
    1. 
    2. 
    3. 
2. 
    1. 
    2. 
    3. 
3. 
    1. 
    2. 
    3. 

#### 5.3 信号量

1. 
    1. 
    2. 
    3. 
2. 
    1. 
    2. 
    3. 
3. 
    1. 
    2. 
    3. 

#### 5.4 管道

1. 
    1. 
    2. 
    3. 
2. 
    1. 
    2. 
    3. 
3. 
    1. 
    2. 
    3. 
    
#### 5.5 其它通信相关

1. 
    1. 
    2. 
    3. 
2. 
    1. 
    2. 
    3. 
3. 
    1. 
    2. 
    3. 

## 原理篇

### 6. Java内存模型基础知识
### 7. 重排序与happens-before
### 8. volatile
### 9. synchronized与锁
### 10. 乐观锁和悲观锁
### 11. AQS

## JDK工具篇

### 12. 线程池原理
### 13. 阻塞队列
### 14. 锁接口和类
### 15. 并发容器集合
### 16. CopyOnWrite容器
### 17. 通信工具类
### 18. Fork/Join框架
### 19. Java 8 Stream并行计算原理
### 20. 计划任务




























# 参考资料

>1. [深入浅出Java多线程](http://concurrent.redspider.group/RedSpider.html)
>
>2. [深入浅出Java多线程](https://redspider.gitbook.io/concurrent/)