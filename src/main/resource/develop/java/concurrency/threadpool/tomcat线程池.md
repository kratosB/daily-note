# tomcat线程池

1. 简单来说，tomcat线程池，会先用完最大线程数，然后再入队列。（基于IO密集型的原因）
   1. 如果**当前线程数<核心线程数**，创建新线程。
   2. 如果**当前线程数=最大线程数**，新任务入队。
   3. 如果**当前任务数<当前线程数**，新任务入队。
   4. 如果**当前线程数<最大线程数**，创建新线程。
2. 修改这个逻辑，主要是修改了队列的offer方法，让offer方法适当在适当的时候返回false，进入新增线程的逻辑。
3. 队列用的是taskQueue（专门为线程池设计的队列，继承LinkedBlockingQueue），默认配置无限长度。
4. startInternal方法构建线程池的时候，`taskqueue.setParent(executor);`这句很关键。
5. TaskQueue的offer方法：
   1. 如果没有parent，就用linkedBlockingQueue的offer。（所以上面那个setParent很关键）
   2. 如果**当前线程数=最大线程数**，就用linkedBlockingQueue的offer（新任务入队）。
   3. 如果**当前任务数<当前线程数**，就用linkedBlockingQueue的offer（新任务入队）。
   4. 如果**当前线程数<最大线程数**，返回false。（触发下面else里面的addWorker）
   5. 其他情况，就用linkedBlockingQueue的offer（新任务入队）。
   这样设计有一个好处，队列满了或者其他什么情况，都复用linkedBlockingQueue的代码。
6. 拒绝策略。
   1. 如果队列满了，则会等待指定时间后再次放入队列。
   2. 如果再次放入队列的时候还是满的，则抛出拒绝异常。

## 引用
[每天都在用，但你知道 Tomcat 的线程池有多努力吗？](https://mp.weixin.qq.com/s/iF209Rgtpw1-3Pdf2lujjg)

