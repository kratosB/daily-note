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

1. 介绍了synchronized (object)，最基础的应用。

#### 5.2 等待/通知机制

1. object.wait()，object.notify()和object.notifyAll()。

#### 5.3 信号量

1. 简单介绍了volatile的变量模拟semaphore。（除取余数的方法模拟）
2. 信号量的应用场景：限流。

#### 5.4 管道

1. PipedWriter，PipedReader，PipedOutputStream，PipedInputStream等。
2. 当我们一个线程需要先另一个线程发送一个信息（比如字符串）或者文件等等时，就需要使用管道通信了。
    
#### 5.5 其它通信相关

1. join()方法：是Thread类的一个实例方法。它的作用是让当前线程陷入“等待”状态，等join的这个线程执行完成后，再继续执行当前线程。
    1. 注意join()方法有两个重载方法，一个是join(long)， 一个是join(long, int)。
    2. 通过源码你会发现，join()方法及其重载方法底层都是利用了wait(long)这个方法。
    3. join(long, int)，通过源码(JDK 1.8)发现，底层并没有精确到纳秒，而是对第二个参数做了简单的判断和处理。
2. sleep方法：是Thread类的一个静态方法。它的作用是让当前线程睡眠一段时间。
    1. Thread.sleep(long)和Thread.sleep(long, int)。
    2. 查看源码(JDK 1.8)发现，第二个方法貌似只对第二个参数做了简单的处理，没有精确到纳秒。实际上还是调用的第一个方法。
    3. 跟wait的差别：
        1. wait可以指定时间，也可以不指定；而sleep必须指定时间。
        2. wait释放cpu资源，同时**释放锁**；sleep释放cpu资源，但是**不释放锁**，所以易死锁。
        3. wait必须放在同步块或同步方法中，而sleep可以在任意位置。
3. ThreadLocal是一个本地线程副本变量工具类。
    1. 内部是一个弱引用的Map来维护。
    2. 如果开发者希望将类的某个静态变量（user ID或者transaction ID）与线程状态关联，则可以考虑使用ThreadLocal。
    3. 最常见的ThreadLocal使用场景为用来解决数据库连接、Session管理等。

## 原理篇

### 6. Java内存模型基础知识

#### 6.1 并发编程模型的两个关键问题

1. 两个关键问题：
    1. 线程间如何通信？即：线程之间以何种机制来交换信息。
    2. 线程间如何同步？即：线程以何种机制来控制不同线程间操作发生的相对顺序。
2. 有两种并发模型可以解决这两个问题：
    1. 消息传递并发模型。
    2. 共享内存并发模型。
3. Java中，使用的是共享内存并发模型。

![两种并发模型的比较](http://concurrent.redspider.group/article/02/imgs/%E4%B8%A4%E7%A7%8D%E5%B9%B6%E5%8F%91%E6%A8%A1%E5%9E%8B%E7%9A%84%E6%AF%94%E8%BE%83.png)

#### 6.2 Java内存模型的抽象结构

1. 运行时内存的划分。
    1. 栈中的变量（局部变量、方法定义参数、异常处理器参数）不会在线程之间共享。堆内存共享。
    2. 堆中的变量是共享的。
2. 堆是共享的，为什么在堆中会有内存不可见问题？
    1. 现代计算机为了高效，往往会在高速缓存区中缓存共享变量。所有的共享变量都存在主内存中。每个线程都保存了一份该线程使用到的共享变量的副本。
    2. 如果线程A与线程B之间要通信的话，必须经历下面2个步骤：
        1. 线程A将本地内存A中更新过的共享变量刷新到主内存中去。
        2. 线程B到主内存中去读取线程A之前已经更新过的共享变量。
        >所以，线程A无法直接访问线程B的工作内存，线程间通信必须经过主内存。
    3. JMM通过控制主内存与每个线程的本地内存之间的交互，来提供内存可见性保证。
        1. volatile。
        2. synchronized。
3. JMM和Java运行时内存区域的划分
    1. 区别：
        1. JMM是抽象的。他是用来描述一组规则的。
        2. Java运行时内存的划分是具体的。是JVM运行Java程序时，必要的内存划分。
    2. 联系：
        1. 都存在私有数据区域和共享数据区域。
        2. 一般来说，JMM中的主内存属于共享数据区域，他是包含了堆和方法区。
        3. 一般来说，JMM中的本地内存属于私有数据区域，包含了程序计数器、本地方法栈、虚拟机栈。
 
### 7. 重排序与happens-before

#### 7.1 什么是重排序？

1. 计算机在执行程序时，为了提高性能，编译器和处理器常常会对指令做重排。
    1. 指令重排序可以提高性能。（有个流水线技术，把能连续执行的先执行了，效率高）
2. 指令重排一般分为以下三种：
    1. 编译器优化重排。
        
        编译器在不改变单线程程序语义的前提下，可以重新安排语句的执行顺序。
    2. 指令并行重排。
        
        现代处理器采用了指令级并行技术来将多条指令重叠执行。如果不存在数据依赖性(即后一个执行的语句无需依赖前面执行的语句的结果)，处理器可以改变语句对应的机器指令的执行顺序。
    3. 内存系统重排。
    
        由于处理器使用缓存和读写缓存冲区，这使得加载(load)和存储(store)操作看上去可能是在乱序执行，因为三级缓存的存在，导致内存与缓存的数据同步存在时间差。
3. 指令重排可以保证串行语义一致，但是没有义务保证多线程间的语义也一致。所以在多线程下，指令重排序可能会导致一些问题。

#### 7.2 顺序一致性模型与JMM的保证

纯理论，很繁琐，简单说，就是 
1. 顺序一致性模型中所有操作完全按照程序的顺序串行执行。
2. JMM中，临界区内的代码可以发生重排序。（但是有前提）
其他略过。

#### 7.3 happens-before

1. 如果一个操作happens-before另一个操作，那么第一个操作的执行结果将对第二个操作可见，而且第一个操作的执行顺序排在第二个操作之前。
2. 两个操作之间存在happens-before关系，并不意味着Java平台的具体实现必须要按照happens-before关系指定的顺序来执行。如果重排序之后的执行结果，与按happens-before关系来执行的结果一致，那么JMM也允许这样的重排序。
3. 如果操作A happens-before操作B，那么操作A在内存上所做的操作对操作B都是可见的，不管它们在不在一个线程。
4. 天然的happens-before关系：
    1. 程序顺序规则：一个线程中的每一个操作，happens-before于该线程中的任意后续操作。
    2. 监视器锁规则：对一个锁的解锁，happens-before于随后对这个锁的加锁。
    3. volatile变量规则：对一个volatile域的写，happens-before于任意后续对这个volatile域的读。
    4. 传递性：如果A happens-before B，且B happens-before C，那么A happens-before C。
    5. start规则：如果线程A执行操作ThreadB.start()启动线程B，那么A线程的ThreadB.start（）操作happens-before于线程B中的任意操作。
    6. join规则：如果线程A执行操作ThreadB.join（）并成功返回，那么线程B中的任意操作happens-before于线程A从ThreadB.join()操作成功返回。
5. 重排序有两类，JMM对这两类重排序有不同的策略：
    1. 会改变程序执行结果的重排序，JMM要求编译器和处理器都禁止这种重排序。
    2. 不会改变程序执行结果的重排序，JMM对编译器和处理器不做要求，允许这种重排序。

### 8. volatile

#### 8.1 几个基本概念

1. 内存可见性。
2. 指令重排序。
3. happens-before规则。

#### 8.2 volatile的内存语义

1. volatile主要有以下两个功能：
    1. 保证变量的内存可见性。
    2. 禁止volatile变量与普通变量重排序。
2. JVM是通过内存屏障来实现“限制处理器的重排序”的。
3. 内存屏障：
    1. 阻止屏障两侧的指令重排序。
    2. 强制把写缓冲区/高速缓存中的脏数据等写回主内存，或者让缓存中相应的数据失效。

#### 8.3 volatile的用途

1. volatile的用途
    1. 在保证内存可见性这一点上，volatile有着与锁相同的内存语义，所以可以作为一个“轻量级”的锁来使用。
    2. 在功能上，锁比volatile更强大；在性能上，volatile更有优势。
    3. **双重锁检查单例**，很早以前以为网上例子错了，后来读懂了，这里就详细分析了。

### 9. synchronized与锁

1.
    1. 
    2. 
    3. 
    4. 
2. 
    1. 
    2. 
    3. 
    4. 
3. 
    1. 
    2. 
    3. 
    4. 
4. 
    1. 
    2. 
    3. 
    4. 
5. 
    1. 
    2. 
    3. 
    4. 

### 10. 乐观锁和悲观锁

1.
    1. 
    2. 
    3. 
    4. 
2. 
    1. 
    2. 
    3. 
    4. 
3. 
    1. 
    2. 
    3. 
    4. 
4. 
    1. 
    2. 
    3. 
    4. 
5. 
    1. 
    2. 
    3. 
    4. 

### 11. AQS

1.
    1. 
    2. 
    3. 
    4. 
2. 
    1. 
    2. 
    3. 
    4. 
3. 
    1. 
    2. 
    3. 
    4. 
4. 
    1. 
    2. 
    3. 
    4. 
5. 
    1. 
    2. 
    3. 
    4. 


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