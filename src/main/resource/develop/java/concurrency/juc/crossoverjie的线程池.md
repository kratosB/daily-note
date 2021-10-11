

# 参考资料

>1. [如何优雅的使用和理解线程池](https://crossoverjie.top/2018/07/29/java-senior/ThreadPool/)
>
>2. [一个线程罢工的诡异事件](https://crossoverjie.top/2019/03/12/troubleshoot/thread-gone/)
> 主要就是线程池里面只跑了一个线程，队列是空的。然后这个线程挂了，线程池起了一个新的线程，去队列里拿任务，但是队列是空的，所以就等着，然后报错？
>2. [线程池中你不容错过的一些细节](https://crossoverjie.top/2019/03/26/troubleshoot/thread-gone2/)
> 上面那个帖子的一点补充
>3. [线程池没你想的那么简单](https://crossoverjie.top/2019/05/20/concurrent/threadpool-01/)
>
>4. [线程池没你想的那么简单（续）](https://crossoverjie.top/2019/06/06/concurrent/threadpool-02/)
>
>5. [定时任务方案大百科](https://crossoverjie.top/2019/10/14/algorithm/timer-detail/)
