# binlog, redo log, undo log

看了引用2，3两个例子，还是没有太看懂redo log和binlog的区别，看了引用1，有点懂了，大致备注一下，以免忘记。

1. 引用1里面，先介绍了binlog，大致就是用来恢复数据的数据库的备份。
2. 之后介绍了undo log，就是innodb用来实现事务的工具，大概就是事务开始前，先备份要修改的数据，commit之前，如果crash或者rollback，那么就把之前的备份还原，这样就可以实现事务回滚了。
3. 最后介绍了redo log存在的缘由，原文描述是
    ```text
   假设有A、B两个数据要更新，值分别为1,2。
   A.事务开始.
   B.记录A=1到undo log.
   C.修改A=3.
   D.记录B=2到undo log.
   E.修改B=4.
   F.将undo log写到磁盘。
   G.将数据写到磁盘。
   H.事务提交
   
   这里有一个隐含的前提条件：‘数据都是先读到内存中，然后修改内存中的数据，最后将数据写回磁盘’。
   
   * 为了保证持久性，必须在事务提交前，将数据写到磁盘。
     只要事务成功提交，数据必然已经持久化。
   * 为了保证原子性，**将数据写到磁盘**前，必须先将
     undo log持久化到磁盘，不然如果在G,H之间系统崩溃，没法回滚。
   * 如果在A-F之间系统崩溃,因为数据没有持久化到磁盘。
     所以磁盘上的数据还是保持在事务开始前的状态。
   
   缺陷：如果每个事务提交前都将数据和Undo log写入磁盘，这样会导致大量的磁盘IO，
   因此性能很低。如果能够将数据缓存一段时间，就能减少IO提高性能。
   但是这样就会丧失事务的**持久性**。因此引入了另外一种机制来实现持久化，即Redo log。
   
   redo log记录的是新数据的备份。在事务提交前，只要将Redo log持久化即可，
   不需要将数据持久化。当系统崩溃时，虽然数据没有持久化，但是redo log已经持久化。
   系统可以根据Redolog的内容，将所有数据恢复到最新的状态。
   
   例子如下
   -Undo+Redo事务的简化过程
   假设有A、B两个数据，值分别为1,2.
   A.事务开始.
   B.记录A=1到undo log.
   C.修改A=3.
   D.记录A=3到redo log.
   E.记录B=2到undo log.
   F.修改B=4.
   G.记录B=4到redo log.
   H.将redo log写入磁盘。
   I.事务提交
   
   有一个隐含的特点，数据必须要晚于redo log写入持久存
   
   * 为了保证持久性，必须在事务提交前将redo log持久化。
   * 数据不需要在事务提交前写入磁盘，而是缓存在内存中（如果崩溃，直接用redo log还原）。
   * 如果A到H之间奔溃，因为数据没有持久化到磁盘。所以磁盘上的数据还是保持在事务开始前的状态。
   * 如果H和I之间崩溃，redo log已经写入磁盘，可以保证事务的持久性。
   * 如果直到H都没有奔溃，那么undo log还在内存中，不需要写入磁盘也可以保证事务的原子性。
   
   * 注意，这里就只有一次磁盘写入，上面单一undo log的模式有两次磁盘写入。
    ```
   所以，我个人把redo log理解为写入数据的缓存，只是为了配合undo log的存在而存在的（也不知道这么理解对不对，但是其他帖子总觉得没说清楚binlog和redo log的区别）。
---
引用2，3则主要讲了（还讲了更新语句的流程）为什么要两段式提交（避免binlog和redo log冲突）。

---
引用4主要讲了redo log和undo log的结构，没怎么仔细看，但是貌似挺详细的。

## 参考资料
>1. [binlog，redo log，undo log区别](https://blog.csdn.net/mydriverc2/article/details/50629599)
>2. [一条SQL语句在MySQL中如何执行的](https://mp.weixin.qq.com/s?__biz=Mzg2OTA0Njk0OA==&mid=2247485097&idx=1&sn=84c89da477b1338bdf3e9fcd65514ac1&chksm=cea24962f9d5c074d8d3ff1ab04ee8f0d6486e3d015cfd783503685986485c11738ccb542ba7&token=79317275&lang=zh_CN#rd)
>3. 《MySQL实战45讲-02 | 日志系统：一条SQL更新语句是如何执行的？》
>4. [详细分析MySQL事务日志(redo log和undo log)](https://www.cnblogs.com/f-ck-need-u/archive/2018/05/08/9010872.html#auto_id_11)