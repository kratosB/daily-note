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
3. Future接口有一个实现类叫FutureTask。FutureTask是实现的RunnableFuture接口的，而RunnableFuture接口同时继承了Runnable接口和Future接口。
    >1. callable的本质，其实就是runnable。执行的时候执行的是FutureTask的run()方法，FutureTask的run()方法里面调用了callable的call()方法。
    >2. FutureTask的run()方法中，有一个set(result);，会把返回值设置到outcome中，future中获取的返回值就是outcome。

### 3. 线程组和线程优先级

意义不大，略

### 4. Java线程的状态及主要转化方法

#### 4.1 操作系统中的线程状态转换

![系统进程状态转换图](http://concurrent.redspider.group/article/01/imgs/%E7%B3%BB%E7%BB%9F%E8%BF%9B%E7%A8%8B%E7%8A%B6%E6%80%81%E8%BD%AC%E6%8D%A2%E5%9B%BE.png)

1. 操作系统线程主要有以下三个状态。
    1. 就绪状态(ready)：线程正在等待使用CPU，经调度程序调用之后可进入running状态。
    2. 执行状态(running)：线程正在使用CPU。
    3. 等待状态(waiting)：线程经过等待事件的调用或者正在等待其他资源（如I/O）。

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

1. BLOCKED与RUNNABLE状态的转换。
2. WAITING状态与RUNNABLE状态的转换。
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
    2. 跟wait的差别：
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
        >2和3两个说法存疑，我理解，JMM种的主内存就是堆之类的，JMM种的本地内存，是堆中数据在高速缓存中的拷贝。
 
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

#### 9.1 Synchronized关键字

1. synchronized用法。
    1. 锁代码块（对象）。
        1. 锁括号里面的对象。
            >根据对象锁代码块。静态对象和非静态对象的处理其实跟下面一样。
    2. 锁实例方法。
        1. 锁当前实例。
            >同一时间，一个对象只有一把锁，一个线程获取了该对象的锁之后，其他线程无法获取该对象的锁，就不能访问该对象的其他synchronized实例方法，但是可以访问非synchronized修饰的方法。
    3. 锁静态方法。
        1. 锁当前class对象。
            >当前类的class对象被锁了，一个线程获取了class对象的锁之后，其他线程无法获得class对象的锁，所以就不能放问这个类中其他static synchronized的方法，但是可以访问非static的synchronized方法，因为static的方法是在class对象中的，非static的不是。

#### 9.2 几种锁

1. 在Java6及其以后，一个对象其实有四种锁状态，它们级别由低到高依次是：
    1. 无锁状态。
    2. 偏向锁状态。
    3. 轻量级锁状态。
    4. 重量级锁状态。
2. Java的锁都是基于对象的。每个Java对象都有对象头。非数组类型用2个字宽来存储对象头，数组用3个字宽来存储对象头。32位处理器中，一个字宽是32位，64位虚拟机中，一个字宽是64位。
    1. 对象头内容：
        
        |长度|内容|说明|
        |---|---|---|
        |32/64bit|Mark Word|存储对象的hashCode或锁信息等|
        |32/64bit|Class Metadata Address|存储到对象类型数据的指针|
        |32/64bit|Array length|数组的长度（如果是数组）|
    2. Mark Word的格式：
    
        |锁状态|29 bit 或 61 bit|1 bit 是否是偏向锁？|2 bit 锁标志位|
        |---|---|---|---|
        |无锁|无|0|01|
        |偏向锁|线程ID|1|01|
        |轻量级锁|指向栈中锁记录（Lock Record）的指针|此时这一位不用于标识偏向锁|00|
        |重量级锁|指向互斥量（重量级锁）（monitor对象）的指针|此时这一位不用于标识偏向锁|10|
        |GC标记|无|此时这一位不用于标识偏向锁|11|
3. 偏向锁。
    1. 原理：如果在接下来的运行过程中，该锁没有被其他的线程访问，则持有偏向锁的线程将永远不需要触发同步。
        
        偏向锁在资源无竞争情况下消除了同步语句，连CAS操作都不做了，提高了程序的运行性能。
    2. 实现：
        1. 一个线程在第一次进入同步块时，会在对象头和栈帧中的锁记录里存储锁的偏向的线程ID。
        2. 下次该线程进入这个同步块时，会去检查锁的Mark Word里面是不是放的自己的线程ID。
        3. 如果是，表明该线程已经获得了锁，以后该线程在进入和退出同步块时不需要花费CAS操作来加锁和解锁。
        4. 如果不是，就代表有另一个线程来竞争这个偏向锁。
        5. 这个时候会尝试使用CAS来替换Mark Word里面的线程ID为自己线程的ID。如果成功，表示之前的线程不存在了， Mark Word里面的线程ID为新线程的ID，锁不会升级，仍然为偏向锁。
    3. 升级：
        1. 如果替换Mark Word里面的线程ID失败，表示之前的线程仍然存在，那么暂停之前的线程，设置偏向锁标识为0，并设置锁标志位为00，升级为轻量级锁，会按照轻量级锁的方式进行竞争锁。
    4. 撤销：
        1. 拥有轻量级锁的线程执行完毕。
        2. 偏向锁升级成轻量级锁时，会暂停拥有偏向锁的线程，重置偏向锁标识。
            1. 在一个安全点（在这个时间点上没有字节码正在执行）停止拥有锁的线程。
            2. 遍历线程栈，如果存在锁记录的话，需要修复锁记录和Mark Word，使其变成无锁状态。
            3. 唤醒被停止的线程，将当前锁升级成轻量级锁。
    5. 竞争太多的话，偏向锁会变成累赘，使用`-XX:UseBiasedLocking=false`可以关闭功能。
4. 轻量级锁。（介绍的比较繁琐，总的来说就是自旋）
    1. 原理：阻塞成本高，自旋成本低，如果很快就能获得锁，可以考虑自旋，所以有了轻量级锁。
    2. 实现：
        1. 在当前线程的栈帧中创建用于存储锁记录的空间（Displaced Mark Word）。
        2. 把锁的Mark Word复制到自己的Displaced Mark Word里面。
        3. 尝试用CAS将锁的Mark Word替换为指向锁记录的指针。
        4. 如果成功，当前线程获得锁，如果失败，表示Mark Word已经被替换成了其他线程的锁记录，说明在与其它线程竞争锁，当前线程就尝试使用自旋来获取锁。
            1. 自旋消耗cpu。
            2. 自适应自旋。
    3. 升级：
        1. 自旋到一定程度（和JVM、操作系统相关），依然没有获取到锁，称为自旋失败，那么这个线程会阻塞。同时这个锁就会升级成重量级锁。
    4. 释放：
        1. 当前线程会使用CAS操作将Displaced Mark Word的内容复制回锁的Mark Word里面
        2. 如果没有发生竞争，那么这个复制的操作会成功。
        3. 如果有其他线程因为自旋多次导致轻量级锁升级成了重量级锁，那么CAS操作会失败，此时会释放锁并唤醒被阻塞的线程。
5. 重量级锁。
    1. 重量级锁依赖于操作系统的互斥量（mutex）实现。操作系统中线程间状态的转换较慢，所以重量级锁效率很低，但被阻塞的线程不会消耗CPU。
    2. 实现：
        1. 当一个线程尝试获得锁时，如果该锁已经被占用，则会将该线程封装成一个ObjectWaiter对象插入到Contention List的队列的队首，然后调用park函数挂起当前线程。
        2. 当线程释放锁时，会从Contention List或Entry List中挑选一个线程唤醒，被选中的线程被唤醒后会尝试获得锁，但synchronized是非公平的，所以不一定能获得锁。
        3. 如果线程获得锁后调用Object.wait方法，则会将线程加入到WaitSet中。
        4. 当被Object.notify唤醒后，会将线程从WaitSet移动到Contention List或EntryList中去。
        >* 自旋线程可能会抢占非公平锁。
        >* Contention List：所有请求锁的线程将被首先放置到该竞争队列。
        >* Entry List：Contention List中那些有资格成为候选人的线程被移到Entry List。
        >* Wait Set：那些调用wait方法被阻塞的线程被放置到Wait Set。
6. 总结锁的升级流程。
    1. 一个线程在准备获取共享资源时，检查MarkWord里面是不是自己的ThreadId，如果是，表示当前线程是处于偏向锁。
    2. 如果MarkWord不是自己的ThreadId，锁升级。这时候，用CAS来执行切换，新的线程根据MarkWord里面现有的ThreadId，通知之前线程暂停，之前线程将Markword的内容置为空。
    3. 两个线程都把锁对象的HashCode复制到自己新建的用于存储锁的记录空间，接着开始通过CAS操作，把锁对象的MarKword的内容修改为自己新建的记录空间的地址的方式竞争MarkWord。
    4. 第三步中成功执行CAS的获得资源，失败的则进入自旋。
    5. 自旋的线程在自旋过程中，成功获得资源(即之前获的资源的线程执行完成并释放了共享资源)，则整个状态依然处于 轻量级锁的状态。
    6. 如果自旋失败，进入重量级锁的状态，这个时候，自旋的线程进行阻塞，等待之前线程执行完成并唤醒自己。
7. 各种锁的优缺点对比。

    |锁|优点|缺点|使用场景|
    |---|---|---|---|
    |偏向锁|加锁和解锁不需要额外的消耗，和执行非同步方法比仅存在纳秒级的差距。|如果线程间存在锁竞争，会带来额外的锁撤销的消耗。|适用于只有一个线程访问同步块场景。|
    |轻量级锁|竞争的线程不会阻塞，提高了程序的响应速度。|如果始终得不到锁竞争的线程使用自旋会消耗CPU。|追求响应时间。同步块执行速度非常快。|
    |重量级锁|线程竞争不使用自旋，不会消耗CPU。|线程阻塞，响应时间缓慢。|追求吞吐量。同步块执行速度较长。|

### 10. 乐观锁和悲观锁

#### 10.1 乐观锁与悲观锁的概念

1. 悲观锁。
    1. 悲观锁就是我们常说的锁。
    2. 对于悲观锁来说，它认为每次访问共享资源时都会发生冲突，所以必须对每次数据操作加上锁。
    3. 多用于”写多读少“的环境，避免频繁失败和重试影响性能。
2. 乐观锁。
    1. 乐观锁总是假设对共享资源的访问没有冲突，线程可以不停地执行，无需加锁也无需等待。
    2. 一旦多个线程发生冲突，乐观锁通常是使用一种称为CAS的技术来保证线程执行的安全性。
    3. 多用于“读多写少“的环境，避免频繁加锁影响性能。

#### 10.2 CAS的概念

1. CAS的全称是：比较并交换（Compare And Swap）
2. CAS中，有这样三个值：
    1. V：要更新的变量(var)。
    2. E：预期值(expected)。
    3. N：新值(new)。
3. 比较并交换的过程如下：
    1. 判断V是否等于E，如果等于，将V的值设置为N。
    2. 如果不等，说明已经有其它线程更新了V，则当前线程放弃更新，什么都不做。
4. 当多个线程同时使用CAS操作一个变量时，只有一个会胜出，并成功更新，其余均会失败，但失败的线程并不会被挂起，仅是被告知失败，并且允许再次尝试，当然也允许失败的线程放弃操作。

#### 10.3 Java实现CAS的原理 - Unsafe类

1. Java中的cas是原子操作，由Unsafe类的native方法实现。

#### 10.4 原子操作-AtomicInteger类源码简析

1. JDK提供了一些用于原子操作的类，在java.util.concurrent.atomic包下面。
2. 其实就是Unsafe类的CAS的应用，比较简单。 

#### 10.5 CAS实现原子操作的三大问题

1. ABA问题。
    1. 一个值原来是A，变成了B，又变回了A。这个时候使用CAS是检查不出变化的，但实际上却被更新了两次。
    2. ABA问题的解决思路是在变量前面追加上版本号或者时间戳。
    3. 从JDK1.5开始，JDK的atomic包里提供了一个类AtomicStampedReference类来解决ABA问题。
2. 循环时间长开销大。
    1. CAS多与自旋结合。如果自旋CAS长时间不成功，会占用大量的CPU资源。
    2. 解决思路是让JVM支持处理器提供的pause指令。
        1. pause指令能让自旋失败时cpu睡眠一小段时间再继续自旋，从而使得读操作的频率低很多，为解决内存顺序冲突而导致的CPU流水线重排的代价也会小很多。
3. 只能保证一个共享变量的原子操作。
    1. 使用JDK1.5开始就提供的AtomicReference类保证对象之间的原子性，把多个变量放到一个对象里面进行CAS操作。
    2. 使用锁。锁内的临界区代码可以保证只有当前线程能操作。

### 11. AQS

#### 11.1 AQS简介

1. 抽象队列同步器，一个用来构建锁和同步器的框架，很多同步工具的底层。

#### 11.2 AQS的数据结构

1. 内部有一个volatile的state。
2. 内部有几个protected方法操作state。
    1. getState()
    2. setState()
    3. compareAndSetState()
3. AQS类实现的是一些排队和阻塞的机制。
    1. 比如具体线程等待队列的维护。
    2. 内部使用了一个先进先出（FIFO）的双端队列，并使用了两个指针head和tail用于标识队列的头部和尾部。
    3. 但它并不是直接储存线程，而是储存拥有线程的Node节点。

#### 11.3 资源共享模式

1. 资源有两种共享模式，或者说两种同步方式：
    1. 独占模式（Exclusive）：资源是独占的，一次只能一个线程获取。如ReentrantLock。
    2. 共享模式（Share）：同时可以被多个线程获取，具体的资源个数可以通过参数指定。如Semaphore/CountDownLatch。
2. AQS有一个内部类Node，通过Node我们可以实现两个队列：
    1. 通过prev和next实现CLH队列(线程同步队列，双向队列)。
    2. 二是nextWaiter实现Condition条件上的等待线程队列(单向队列)，这个Condition主要用在ReentrantLock类中。

#### 11.4 AQS的主要方法源码解析

源码复杂，看了个大概，这里略过了。
1. 获取资源。
    1. 获取资源的入口是acquire(int arg)方法。arg是要获取的资源的个数，在独占模式下始终为1。
        1. 获取资源的方法除了acquire外，还有以下三个：
            1. acquireInterruptibly：申请可中断的资源（独占模式）
            2. acquireShared：申请共享模式的资源
            3. acquireSharedInterruptibly：申请可中断的资源（共享模式）
    2. 首先调用tryAcquire(arg)尝试去获取资源。
    3. 如果获取资源失败，就通过addWaiter(Node.EXCLUSIVE)方法把这个线程插入到等待队列中。
        >在队列的尾部插入新的Node节点，通过CAS自旋的方式保证了操作的线程安全性。
    4. 处于等待队列的结点是从头结点一个一个去获取资源的。在acquireQueued方法中。
    5. 这里parkAndCheckInterrupt方法内部使用到了LockSupport.park(this)。实际上就是Unsafe里的park和unpark。
2. 释放资源。
    1. release方法。
    2. 里面调用了unparkSuccessor方法。

## JDK工具篇

### 12. 线程池原理

#### 12.1 为什么要使用线程池

1. 创建/销毁线程需要消耗系统资源，线程池可以复用已创建的线程。
2. 控制并发的数量。并发数量过多，可能会导致资源消耗过多，从而造成服务器崩溃。（主要原因）
3. 可以对线程做统一管理。

#### 12.2 线程池的原理

1. Java中的线程池顶层接口是Executor接口，ThreadPoolExecutor是这个接口的实现类。
2. ThreadPoolExecutor一共有四个构造方法，必要的参数有5个，非必要的2个。
    1. （必要）int corePoolSize：该线程池中核心线程数最大值。
    2. （必要）int maximumPoolSize：该线程池中线程总数最大值。
        1. maximumPoolSize = corePoolSize + 临时线程（任务少了会销毁）size。
    3. （必要）long keepAliveTime：非核心（临时）线程闲置超时时长。
    4. （必要）TimeUnit unit：keepAliveTime的单位。
    5. （必要）BlockingQueue workQueue：阻塞队列，维护着等待执行的Runnable任务对象。
        1. LinkedBlockingQueue：链式阻塞队列，底层数据结构是链表，默认大小是Integer.MAX_VALUE，也可以指定大小。
        2. ArrayBlockingQueue：数组阻塞队列，底层数据结构是数组，需要指定队列的大小。
        3. SynchronousQueue：同步队列，内部容量为0，每个put操作必须等待一个take操作，反之亦然。
        4. DelayQueue：延迟队列，该队列中的元素只有当其指定的延迟时间到了，才能够从队列中获取到该元素 。
    6. （非必要）ThreadFactory threadFactory：创建线程的工厂 ，用于批量创建线程，统一在创建线程时设置一些参数，如是否守护线程、线程的优先级等。如果不指定，会新建一个默认的线程工厂。
    7. （非必要）RejectedExecutionHandler handler：拒绝处理策略，线程数量大于最大线程数就会采用拒绝处理策略。
        1. ThreadPoolExecutor.AbortPolicy：默认拒绝处理策略，丢弃任务并抛出RejectedExecutionException异常。
        2. ThreadPoolExecutor.DiscardPolicy：丢弃新来的任务，但是不抛出异常。
        3. ThreadPoolExecutor.DiscardOldestPolicy：丢弃队列头部（最旧的）的任务，然后重新尝试执行程序（如果再次失败，重复此过程）。
        4. ThreadPoolExecutor.CallerRunsPolicy：由调用线程处理该任务。
3. 线程池主要的任务处理流程。
    1. 线程总数量 < corePoolSize，无论线程是否空闲，都会新建一个核心线程执行任务（让核心线程数量快速达到corePoolSize，在核心线程数量 < corePoolSize时）。注意，这一步需要获得全局锁。
    2. 线程总数量 >= corePoolSize时，新来的线程任务会进入任务队列中等待，然后空闲的核心线程会依次去缓存队列中取任务来执行（体现了线程复用）。
    3. 当缓存队列满了，说明这个时候任务已经多到爆棚，需要一些“临时工”来执行这些任务了。于是会创建非核心线程去执行这个任务。注意，这一步需要获得全局锁。
    4. 缓存队列满了， 且总线程数达到了maximumPoolSize，则会采取上面提到的拒绝策略进行处理。
4. ThreadPoolExecutor如何做到线程复用的。
    1. ThreadPoolExecutor在创建线程时，会将线程封装成工作线程worker，并放入工作线程组中，然后这个worker反复从阻塞队列中拿任务去执行。
    2. addWorker()方法。
        1. 先判断线程数量是否超出阈值，超过了就返回false。
        2. 创建worker对象，并初始化一个Thread对象，然后启动这个线程对象。
        3. Thread start之后，触发了worker的run方法。
        4. worker的run方法，调用了runWorker方法。
        5. runWorker方法线执行创建时的任务，然后循环去队列中获取任务，达到复用线程。
        6. 如果队列中没有任务，核心线程会卡在workQueue.take方法，阻塞。非核心线程超时之后会被回收。

#### 12.3 四种常见的线程池

1. newCachedThreadPool。
    1. 只会创建非核心线程。
    2. 当需要执行很多短时间的任务时，CacheThreadPool的线程复用率比较高，会显著的提高性能。
    3. 线程60s后会回收，意味着即使没有任务进来，CacheThreadPool并不会占用很多资源。
2. newFixedThreadPool。
    1. 只会创建核心线程。
    2. 如果队列里没有任务可取，线程会一直阻塞在LinkedBlockingQueue.take()，线程不会被回收。没有任务的情况下，FixedThreadPool占用资源更多。
3. newSingleThreadExecutor。
    1. 单线程复用。
4. newScheduledThreadPool。
    1. 延迟队列。
5. 阿里规范推荐不用这几种方法，因为corePoolSize无限大或者队列容量无限大，容易溢出。

### 13. 阻塞队列

#### 13.1 阻塞队列的由来

1. 自己开发生产者-消费者模式，需要处理很多锁，并发，阻塞，唤醒。BlockingQueue提供了线程安全的队列访问方式。

#### 13.2 BlockingQueue的操作方法

|方法/处理方式|抛出异常|返回特殊值|阻塞|超时退出|
|---|---|---|---|---|
|插入方法|add(e)|offer(e)|put(e)|offer(e,time,unit)|
|移除方法	|remove()|poll()|take()|poll(time,unit)|
|检查方法	|element()|peek()|

1. 不能往阻塞队列中插入null，会抛出空指针异常。
2. 可以访问阻塞队列中的任意元素，调用remove(o)可以将队列之中的特定对象移除，但并不高效，尽量避免使用。

#### 13.3 BlockingQueue的实现类

1. ArrayBlockingQueue。
    1. 由数组结构组成的有界阻塞队列。内部结构是数组，故具有数组的特性。
    2. 可以初始化队列大小，且一旦初始化不能改变。构造方法中的fair表示控制对象的内部锁是否采用公平锁，默认是非公平锁。
    3. 内部只有一把锁，所以不能同时put和take。
    4. 原生数组性能好。
2. LinkedBlockingQueue。
    1. 由链表结构组成的有界阻塞队列。内部结构是链表，具有链表的特性。
    2. 默认队列的大小是Integer.MAX_VALUE，也可以指定大小。此队列按照先进先出的原则对元素进行排序。
    3. 内部有两把锁，可以同时put和take。
    4. 链表性能稍差点，需要new一个Node，也需要更多的gc。
3. DelayQueue。
    1. 该队列中的元素只有当其指定的延迟时间到了，才能够从队列中获取到该元素。
    2. 注入其中的元素必须实现java.util.concurrent.Delayed接口。
    3. DelayQueue是一个没有大小限制的队列，因此往队列中插入数据的操作（生产者）**永远不会被阻塞**，而只有获取数据的操作（消费者）才会被阻塞。
4. PriorityBlockingQueue。
    1. 基于优先级的无界阻塞队列（优先级的判断通过构造函数传入的Compator对象来决定），内部控制线程同步的锁采用的是公平锁。
    2. 不会阻塞数据生产者（因为队列是无界的），而只会在没有可消费的数据时，阻塞数据的消费者。
5. SynchronousQueue。
    1. 这个队列比较特殊，没有任何内部容量，甚至连一个队列的容量都没有。并且每个 put 必须等待一个 take，反之亦然。
    2. 即使是容量为1的ArrayBlockingQueue、LinkedBlockingQueue，跟这个还是有区别。
    3. 特殊之处：
        1. iterator() 永远返回空，因为里面没有东西。
        2. peek() 永远返回null。
        3. put() 往queue放进去一个element以后就一直wait直到有其他thread进来把这个element取走。
        4. offer() 往queue里放一个element后立即返回，如果碰巧这个element被另一个thread取走了，offer方法返回true，认为offer成功；否则返回false。
        5. take() 取出并且remove掉queue里的element，取不到东西他会一直等。
        6. poll() 取出并且remove掉queue里的element，只有到碰巧另外一个线程正在往queue里offer数据或者put数据的时候，该方法才会取到东西。否则立即返回null。
        7. isEmpty() 永远返回true。
        8. remove()&removeAll() 永远返回false。
>对于所有无界的阻塞队列，使用的时候要特别注意，生产者生产数据的速度绝对不能快于消费者消费数据的速度，否则时间一长，会最终耗尽所有的可用堆内存空间。

#### 13.4 阻塞队列的原理

1. 首先是构造器。（这里的例子是ArrayBlockingQueue，内部使用一把锁。作为对比，LinkedBlockingQueue内部使用两把锁，所以可以同时put和take。）
    1. 除了初始化队列的大小和是否是公平锁之外，还对同一个锁（lock）初始化了两个条件（condition），notFull和notEmpty，共用一把锁。
2. put操作的流程。
    1. 跟所有线程竞争lock锁（take和put一起竞争），拿到了lock锁的线程进入下一步，没有拿到lock锁的线程自旋竞争锁。
    2. 判断阻塞队列是否满了，如果满了，则调用await方法阻塞这个线程，并标记为notFull condition，同时释放lock锁，等待被消费者线程唤醒。
    3. 如果没有满，则调用enqueue方法将元素put进阻塞队列。注意这一步的线程，还有一种情况，是第二步中阻塞的线程中，被唤醒且又拿到了lock锁的线程。
        >被唤醒的线程之所以被唤醒，是因为消费者消费了队列中的内容，所以这里的“没满”说法也没有问题。被唤醒之后会再次执行while判断是否满了，如果没满，执行这个步骤。
    4. enqueue方法结束的时候，唤醒一个notEmpty condition的线程。
3. take操作的流程。
    1. 跟所有线程竞争lock锁（take和put一起竞争），拿到了lock锁的线程进入下一步，没有拿到lock锁的线程自旋竞争锁。
    2. 判断阻塞队列是否为空，如果是空，则调用await方法阻塞这个线程，并标记为notEmpty condition，同时释放lock锁,等待被生产者线程唤醒。
    3. 如果不为空，则调用dequeue方法。注意这一步的线程，还有一种情况，是第二步中阻塞的线程中，被唤醒且又拿到了lock锁的线程。
        >被唤醒的线程之所以被唤醒，是因为生产者生产了新的内容到队列中，所以这里的“不为空”说法也没有问题。被唤醒之后会再次执行while判断是否满了，没满也会执行这个步骤。
    4. dequeue方法结束的时候，唤醒一个标记为notFull（生产者）的线程。
4. 注意
    1. 因为只有一把锁，所以put和take是一起竞争的，同一时间只能put或者take。
    2. 被阻塞的线程被唤醒之后，依然需要竞争拿到锁才能继续往下执行。

#### 13.5 示例和使用场景

1. 生产者-消费者模型
2. 线程池中使用阻塞队列

### 14. 锁接口和类

#### 14.1 synchronized的不足之处

1. 如果都是读操作，synchronized也只能一个线程执行。
2. synchronized无法知道线程有没有成功获取到锁。（这一点其实我不太懂）
3. 如果临界区因为IO或者sleep方法等原因阻塞了，而当前线程又没有释放锁，就会导致所有线程等待。

#### 14.2 锁的几种分类

1. 可重入锁和非可重入锁。
    1. 重入锁。
        1. 支持重新进入的锁，也就是说这个锁支持一个线程对资源重复加锁。
        2. synchronized关键字就是使用的重入锁。
            >你在一个synchronized实例方法里面调用另一个本实例的synchronized实例方法，它可以重新进入这个锁，不会出现任何异常。
        3. ReentrantLock的中文意思就是可重入锁。
2. 公平锁与非公平锁。
    1. 这里的“公平”，其实通俗意义来说就是“先来后到”，也就是FIFO。对一个锁来说，先对锁获取请求的线程一定会先被满足，后对锁获取请求的线程后被满足，那这个锁就是公平的。反之，那就是不公平的。
    2. 一般情况下，非公平锁能提升一定的效率。但是非公平锁可能会发生线程饥饿（有一些线程长时间得不到锁）的情况。
    3. ReentrantLock支持非公平锁和公平锁两种。
3. 读写锁和排它锁。
    1. 排它锁：同一时刻只允许一个线程进行访问。
    2. 读写锁：可以在同一时刻允许多个读线程访问。
    3. synchronized用的锁和ReentrantLock，其实都是“排它锁”。
    4. Java提供了ReentrantReadWriteLock类作为读写锁的默认实现，内部维护了两个锁：一个读锁，一个写锁。通过分离读锁和写锁，使得在“读多写少”的环境下，大大地提高了性能。

#### 14.3 JDK中有关锁的一些接口和类

1. 接口Condition。
    1. condition和object的对比。
         
        |对比项|Object监视器|Condition|
        |---|---|---|
        |前置条件|获取对象的锁|调用Lock.lock获取锁，调用Lock.newCondition获取Condition对象|
        |调用方式|直接调用，比如object.notify()|直接调用，比如condition.await()|
        |等待队列的个数|一个|多个|
        |当前线程释放锁进入等待状态|支持|支持|
        |当前线程释放锁进入等待状态，在等待状态中不中断|不支持|支持|
        |当前线程释放锁并进入超时等待状态|支持|支持|
        |当前线程释放锁并进入等待状态直到将来的某个时间|不支持|支持|
        |唤醒等待队列中的一个线程|支持|支持|
        |唤醒等待队列中的全部线程|支持|支持|
    2. await()：当前线程进入等待状态直到被通知（signal）或者中断。
        1. 当前线程进入运行状态并从await()方法返回的场景包括：
            1. 其他线程调用相同Condition对象的signal/signalAll方法，并且当前线程被唤醒。
            2. 其他线程调用interrupt方法中断当前线程。
    3. awaitUninterruptibly()：当前线程进入等待状态直到被通知，在此过程中对中断信号不敏感，不支持中断当前线程。
    4. awaitNanos(long)：当前线程进入等待状态，直到被通知、中断或者超时。如果返回值小于等于0，可以认定就是超时了。
    5. awaitUntil(Date)：当前线程进入等待状态，直到被通知、中断或者超时。如果没到指定时间被通知，则返回true，否则返回false。
    6. signal()：唤醒一个等待在Condition上的线程，被唤醒的线程在方法返回前必须获得与Condition对象关联的锁。
    7. signalAll()：唤醒所有等待在Condition上的线程，能够从await()等方法返回的线程必须先获得与Condition对象关联的锁。
2. ReentrantLock。
    1. 可重入。
    2. 排他锁。
    3. 公平/非公平，默认非公平。
3. ReentrantReadWriteLock。
    1. 可重入。
    2. 支持读写锁。
    3. 公平/非公平。
    4. 内部维护了两个同步器。
    5. 维护了两个Lock的实现类ReadLock和WriteLock。
    6. 有一个小弊端，就是在“写”操作的时候，其它线程不能写也不能读。我们称这种现象为“写饥饿”。
4. StampedLock。
    1. 性能最好。
    2. 如果读的时候写，则使用重试获取新的值。类似cas。在读多写少的时候性能很好。
    3. 复杂。
    4. 略

>p.s. 这里有一个写饥饿的概念没懂。

### 15. 并发容器集合

#### 15.1 同步容器与并发容器

1. Vector和HashTable。
    1. 是线程安全的容器类。
    2. 但是这些容器实现同步的方式是通过对方法加锁(sychronized)方式实现的，这样读写均需要锁操作，导致性能低下。
    3. 在面对多线程下的复合操作的时候也是需要通过客户端加锁的方式保证原子性。
        >如果不加锁的话，比如compare+set操作，虽然两个操作各自都是线程安全的，但是中间有间隔时间，无法保证原子性。

#### 15.2 并发容器类介绍

1. ConcurrentHashMap类。
    1. 1.7
        1. 分段锁：将数据分段，对每一段数据分配一把锁。
        2. Segments->segment->HashEntry。Segment是一种可重入锁ReentrantLock，HashEntry则用于存储键值对数据。
        3. 优点是：在并发环境下将实现更高的吞吐量，而在单线程环境下只损失非常小的性能。
        4. 有些方法需要跨段，需要锁定整个表而不仅仅是某个段，这需要按顺序锁定所有段，操作完毕后，又按顺序释放所有段的锁。比如size()、isEmpty()、containsValue()。
        5. 当对HashEntry数组的数据进行修改时，必须首先获得它对应的Segment锁。
    2. 1.8
        1. 同HashMap一样，链表也会在长度达到8的时候转化为红黑树，这样可以提升大量冲突时候的查询效率。
        2. 以某个位置的头结点（链表的头结点或红黑树的root结点）为锁，配合自旋+CAS避免不必要的锁开销，进一步提升并发性能。
    3. 在1.8中ConcurrentHashMap的get操作全程不需要加锁，这是它安全高效的原因之一。[链接](https://mp.weixin.qq.com/s/O1xcRn3PK2sl37XTs1Z-ZA)
        1. get操作全程不需要加锁是因为Node的成员val是用volatile修饰的和数组用volatile修饰没有关系。
        2. 数组用volatile修饰主要是保证在数组扩容的时候保证可见性。
2. ConcurrentNavigableMap接口与ConcurrentSkipListMap类。
    1. ConcurrentNavigableMap接口继承了NavigableMap接口，这个接口提供了针对给定搜索目标返回最接近匹配项的导航方法。
    2. ConcurrentNavigableMap接口的主要实现类是ConcurrentSkipListMap类。底层用了跳表。
    3. 
    4. 
3. 并发Queue。
    1. JDK并没有提供线程安全的List类，因为很难去开发一个通用并且没有并发瓶颈的线程安全的List。
    2. JDK提供了对队列和双端队列的线程安全的类：ConcurrentLinkedDeque和ConcurrentLinkedQueue。
    3. 这两个类是使用CAS来实现线程安全的。
4. 并发Set。
    1. JDK提供了ConcurrentSkipListSet，是线程安全的有序的集合。底层是使用ConcurrentSkipListMap实现。
    2. 谷歌的guava框架实现了一个线程安全的ConcurrentHashSet。

### 16. CopyOnWrite容器

#### 16.1 什么是CopyOnWrite容器

1. CopyOnWrite思想：就是当有多个调用者同时去请求一个资源数据的时候，有一个调用者出于某些原因需要对当前的数据源进行修改，这个时候系统将会复制一个当前数据源的副本给调用者修改。
    >当我们往一个容器中添加元素的时候，不直接往容器中添加，而是将当前容器进行copy，复制出来一个新的容器，然后向新容器中添加我们需要的元素，最后将原容器的引用指向新容器。
2. 优点：
    1. 并发场景对读不用加锁。
3. 缺点：
    1. 耗内存，因为要copy一遍。
    2. 只能读到老数据，新数据读不到。
4. 实现：
    1. CopyOnWriteArrayList。
    2. CopyOnWriteArraySet 。
    
#### 16.2 CopyOnWriteArrayList

1. 优点。
    1. CopyOnWriteArrayList经常被用于“读多写少”的并发场景，是因为CopyOnWriteArrayList无需任何同步措施，大大增强了读的性能。
2. 缺点。
    1. 耗内存。CopyOnWriteArrayList每次执行写操作都会将原容器进行拷贝了一份。
    2. 在写操作执行过程中，读不会阻塞，但读取到的却是老容器的数据。
3. add流程：copy->写操作->切换引用。
4. remove流程：copy被remove元素之外的其他元素->切换引用。

#### 16.3 CopyOnWrite的业务中实现

1. 最适用于“读多写少”的并发场景。
2. 时效性不敏感的增量更新。
    1. 网站需要屏蔽一些“关键字”，“黑名单”。每晚定时更新，每当用户搜索的时候，“黑名单”中的关键字不会出现在搜索结果当中，并且提示用户敏感字。
3. 如果我们希望写入的数据马上能准确地读取，请不要使用CopyOnWrite容器。

### 17. 通信工具类（这个部分都有演示代码）

#### 17.1 Semaphore

1. 可以理解为一种公共资源，可以申请和释放，如果申请完了，那么其他申请的线程要阻塞。
    1. 可以在构造函数中传入初始资源总数，以及是否使用“公平”的同步器。
    2. 默认情况下，是非公平的。
2. acquire方法：申请资源，每次acquire，permits就会减少一个或者多个。
3. release方法：释放资源，每次release，permits就会增加一个或者多个。
4. 原理。
    1. Semaphore内部有一个继承了AQS的同步器Sync，重写了tryAcquireShared方法。
    2. 在这个方法里，会去尝试获取资源。如果获取失败（想要的资源数量小于目前已有的资源数量），就会返回一个负数（代表尝试获取资源失败）。然后当前线程就会进入AQS的等待队列。

#### 17.2 Exchanger

1. Exchanger类用于两个线程交换数据。它支持泛型，也就是说你可以在两个线程之间传送任何数据。
2. 当一个线程调用exchange方法后，它是处于阻塞状态的，只有当另一个线程也调用了exchange方法，它才会继续向下执行。
    1. 它是使用park/unpark来实现等待状态的切换的。
    2. 在使用park/unpark方法之前，使用了CAS检查，估计是为了提高性能。
3. Exchanger类还有一个有超时参数的方法，如果在指定时间内没有另一个线程调用exchange，就会抛出一个超时异常。
4. 如果三个线程调用同一个实例的exchange方法，只有前两个线程会交换数据，第三个线程会进入阻塞状态。
5. exchange是可以重复使用的。也就是说。两个线程可以使用Exchanger在内存中不断地再交换数据。

#### 17.3 CountDownLatch

1. 简单说就是计数器+门闩。某个线程在执行任务之前，需要等待其它线程完成一些前置任务，必须等所有的前置任务都完成（计数器清零），才能开始执行本线程的任务（门闩打开）。
    1. 等待的线程（可有有多个）调用await()方法。
    2. 前置任务线程完成后调用countDown()方法。
2. 原理。
    1. CountDownLatch类的原理挺简单的，内部同样是一个基层了AQS的实现类Sync，且实现起来还很简单，可能是JDK里面AQS的子类中最简单的实现了。
    2. 构造器中的计数值（count）实际上就是闭锁需要等待的线程数量。这个值只能被设置一次，而且CountDownLatch没有提供任何机制去重新设置这个计数值。

#### 17.4 CyclicBarrier

1. 作用和用法跟CountDownLatch差不多，不过CountDownLatch是一次性的，CyclicBarrier可以用很多次。
    1. CyclicBarrier没有分为await()和countDown()，而是只有单独的一个await()方法。
    2. 一旦调用await()方法的线程数量等于构造方法中传入的任务总量，就代表达到屏障了，就可以继续往后执行。
    >只调用一次await()方法，就相当于实现了CountDownLatch的功能。
2. CyclicBarrier原理。
    1. CyclicBarrier虽说功能与CountDownLatch类似，但是实现原理却完全不同，CyclicBarrier内部使用的是Lock + Condition实现的等待/通知模式。
    2. 详情可以查看`dowait(boolean timed, long nanos)`的源码

#### 17.5 Phaser

1. Phaser跟CyclicBarrier基本差不多，就是在前者的基础上，增加了修改“任务总量”`parties`的方法。
    1. CyclicBarrier初始化之后不能修改`parties`。
    2. Phaser初始化之后可以修改`parties`。
    3. `register`：注册一个party，每一阶段必须所有注册的party都到达才能进入下一阶段。
    4. `deRegister`：减少一个party。
2. Phaser终止的两种途径，Phaser维护的线程执行完毕或者onAdvance()返回true。
3. `arriveAndDeregister()`指令减少一个party在下一轮生效，本轮仍照旧。
4. 另外Phaser类用来控制某个阶段的线程数量很有用，但它并在意这个阶段具体有哪些线程arrive，只要达到它当前阶段的parties值，就触发屏障。
5. Phaser类的原理相比起来要复杂得多。它内部使用了两个基于Fork-Join框架的原子类辅助。

### 18. Fork/Join框架

#### 18.1 什么是Fork/Join

1. Fork/Join就是拆分任务，然后聚合结果。
    1. fork，拆分任务。
    2. join，聚合结果。
    3. Fork/Join框架会将任务分配给线程池中的线程。Fork/Join框架在执行任务时使用了工作窃取算法，哪个线程先完成，就去其他（没完成的）线程偷任务。

#### 18.2 工作窃取算法

1. 多线程执行不同任务队列的过程中，某个线程执行完自己队列的任务后从其他线程的任务队列里窃取任务来执行。
2. 当一个线程窃取另一个线程的时候，为了减少两个任务线程之间的竞争，我们通常使用双端队列来存储任务。被窃取的任务线程都从双端队列的头部拿任务执行，而窃取其他任务的线程从双端队列的尾部执行任务。
3. 当一个线程在窃取任务时要是没有其他可用的任务了，这个线程会进入阻塞状态以等待再次“工作”。

#### 18.3 Fork/Join的具体实现

1. Fork/Join框架中，任务的载体是ForkJoinTask。
    1. 一般用RecursiveAction和RecursiveTask，两个都是ForkJoinTask的子类。RecursiveAction无返回值，RecursiveTask有返回值。
    2. fork()方法：把任务推入当前工作线程的工作队列里。
    3. join()方法：等待处理任务的线程处理完毕，获得返回值。
2. ForkJoinPool是用于执行ForkJoinTask任务的执行（线程）池。
    1. WorkQueue：
        1. 内部有一个WorkQueue[]数组，数组里的每一个元素，都是一个双端队列。工作线程在处理自己的工作队列时，会从队列首取任务来执行（FIFO）；如果是窃取其他队列的任务时，窃取的任务位于所属任务队列的队尾（LIFO）。
        2. ForkJoinPool与传统线程池最显著的区别就是它维护了一个工作队列数组。传统线程池一般只有一个阻塞队列。
    2. runState：
        1. ForkJoinPool的运行状态。SHUTDOWN状态用负数表示，其他用2的幂次表示。

#### 18.4 Fork/Join的使用

有演示代码（FibonacciDemo）。

### 19. Java 8 Stream并行计算原理

#### 19.1 Java 8 Stream简介

无用信息，略。

#### 19.2 Stream单线程串行计算

就是普通的stream，略。

#### 19.3 Stream多线程并行计算

Stream中的parallel方法，其实就跟parallelStream一样，略。

#### 19.4 从源码看Stream并行计算原理

1. parallelStream底层实际上就是fork/join。
    1. parallel方法会设置`sourceStage.parallel = true`。
    2. 之后reduce方法会先判断`isParallel()`，然后用并行/非并行模式运算。
    3. reduce方法 -> terminalOp.evaluateParallel -> ReduceOp.evaluateParallel - > ReduceTask ->  AbstractTask -> CountedCompleter -> ForkJoinTask。
    4. 这里的ReduceTask的invoke方法，其实是调用的ForkJoinTask的invoke方法，中间三层继承并没有覆盖这个方法的实现。

#### 19.5 Stream并行计算的性能提升

1. 线程的创建、销毁以及维护线程上下文的切换等等都有一定的开销。
2. 如果你的服务器并不是多核服务器，那也没必要用Stream的并行计算。因为在单核的情况下，往往Stream的串行计算比并行计算更快，因为它不需要线程切换的开销。

### 20. 计划任务

1. Timer的缺点：
    1. Timer是单线程模式。
    2. 如果某个TimerTask耗时较久，那么就会影响其它任务的调度。
    3. Timer的任务调度是基于绝对时间的，对系统时间敏感。
    4. Timer不会捕获执行TimerTask时所抛出的异常。一旦出现异常，则线程就会终止，其他任务也得不到执行。

#### 20.1 使用案例

有演示代码（ScheduledThreadPoolDemo）。

#### 20.2 类结构

1. ScheduledThreadPoolExecutor继承了ThreadPoolExecutor，实现了ScheduledExecutorService。
2. ScheduledExecutorService实现了ExecutorService ,并增加若干定时相关的接口。
    1. scheduleAtFixedRate：特点是，period是从任务**开始执行**算起的。
    2. scheduleWithFixDelay：特点是，period是从任务**执行完成后**算起的。

#### 20.3 主要方法介绍

较复杂，略。

#### 20.4 DelayedWorkQueue

较复杂，略。

# 参考资料

>1. [深入浅出Java多线程](http://concurrent.redspider.group/RedSpider.html)
>
>2. [深入浅出Java多线程](https://redspider.gitbook.io/concurrent/)
>
>3. [为什么ConcurrentHashMap的读操作不需要加锁](https://www.cnblogs.com/keeya/p/9632958.html)