# Java Concurrency代码实例之一执行者与线程池

## 1. 线程执行者

这个功能主要由三个接口和类提供，分别是：
1. Executor：执行者接口，所有执行者的父类。
2. ExecutorService：执行者服务接口，具体的执行者类都继承自此接口。
3. Executors：执行者工具类，大部分执行者的实例以及线程池都由它的工厂方法创建。

参考例子ExecutorDemo。

## 2. 得到异步执行的结果

通过Callable接口、Future接口和ExecutorService的submit方法来得到异步执行的结果
1. Callable：泛型接口，与Runnable接口类似，它的实例可以被另一个线程执行，内部有一个call方法，返回一个泛型变量V。
    >1. 执行的时候，先把callable转化成futureTask。futureTask当作runnableFuture。
    >2. runnableFuture实现了runnable，所以里面有run方法，线程池中调用start的时候，执行futureTask里面的run()方法。
    >3. 在run()方法中调用callable中的call()方法，最后设置结果。
2. Future：泛型接口，代表依次异步执行的结果值，调用其get方法可以得到一次异步执行的结果，如果运算未完成，则阻塞直到完成；调用其cancel方法可以取消一次异步执行。
    >1. 这边的future的实现，实际上就是futureTask。所以这边调用的是futureTask的get()方法。
    >2. get()方法会阻塞，并在最后返回outcome。
    >3. outcome是run()方法中的set()方法写入的。
3. CompletionService：一种执行者，可将submit的多个任务的结果按照完成的先后顺序存入一个内部队列，然后可以使用take方法从队列中依次取出结果并移除，如果调用take时计算未完成则会阻塞。
    >1. ExecutorCompletionService的submit()方法中，会把futureTask转化为queueingFuture。
    >2. queueingFuture重写了futureTask的done()方法，done()方法会在futureTask执行完设置结果的步骤被调用。
    >3. done()方法中，把计算完成的future结果放入阻塞队列中，后面就可以用take()方法取用了。

参考例子FutureDemo1，futureDemo2。

## 3. 重复执行和延期执行

1. ScheduledExecutorService：另一种执行者，可以将提交的任务延期执行，也可以将提交的任务反复执行。
2. ScheduledFuture：与Future接口类似，代表一个被调度执行的异步任务的返回值。

这个原理较上面那个复杂，这个贴子也没介绍，不过在《深入浅出Java多线程》里面有（之前看过，大概看懂了，没记下来）。

参考例子ScheduledExecutorServiceDemo1。

## 4. TimeUnit

主要用来替代`Thread.sleep()`。例如`TimeUnit.SECONDS.sleep(1);`。

## 5. 线程池

这一块儿也还算熟悉了，主要就介绍了线程池的几个参数：
1. corePoolSize：线程池中应该保持的线程数量，即使线程处于空闲状态。
2. maximumPoolSize：线程池中允许的最大线程数，线程池中的线程一旦到达这个数，后续任务就会等待池中的线程空闲，而不会去创建新的线程。
3. keepAliveTime：当池中线程数量大于corePoolSize时，多出的线程在空闲时能够生存的最大时间，若一个线程空闲超过这个时间，它就会被终止并从池中删除。
4. unit：第三个参数的时间单位。
5. workQueue：存储待执行任务的阻塞队列，这些任务必须是Runnable的对象。

另外还介绍了`newSingleThreadExecutor()`，`newFixedThreadPool(nThreads)`，`newCachedThreadPool()`，`newSingleThreadScheduledExecutor()`，`newScheduledThreadPool(n)`等方法。

## 6. Fork-Join

特点：
1. 分而治之，先将一个问题fork（分为）几个子问题，然后子问题又分为孙子问题，直至细分为一个容易计算的问题，然后再将结果依次join(结合)为最终的答案。类似Map-reduce。
2. 在运行线程时，它使用“work-steal”（任务偷取）算法。先完成的线程会从其他线程的任务队列的尾部偷取一个任务来执行，保证了线程的运行效率达到最高。

fork-join框架提供的几个工具类：
1. ForkJoinPool：支持fork-join框架的线程池，所有ForkJoinTask任务都必须在其中运行，线程池主要使用invoke()、invokeAll()等方法来执行任务，当然也可以使用原有的execute()和submit()方法。
    >invoke返回task.join，submit直接返回task，execute没返回。
2. ForkJoinTask：支持fork-join框架的任务抽象类，它是Future接口，它代表一个支持fork()和join()方法的任务。
    >其实就类似callable，future部分的futureTask。
3. RecursiveAction：没有返回值的ForkJoinTask任务。
4. RecursiveTask：有返回值的ForkJoinTask任务。

参考例子FibonacciDemo，RecursiveTaskDemo和RecursiveActionDemo。

# 参考资料

>1. [Java Concurrency代码实例之一执行者与线程池](https://zhuanlan.zhihu.com/p/26724352)
>
>2. [1中那个楼主写的JUC的教程，一共有1-8](https://www.zhihu.com/people/wang-du-du-43-1/posts?page=2)
>
>3. [【java并发核心八】Fork-Join分治编程，这里面有join和get的区别](https://www.cnblogs.com/klbc/p/9797969.html)
>
>4. [Java多线程系列--“JUC线程池”01之 线程池架构](https://www.cnblogs.com/skywang12345/p/3509903.html)