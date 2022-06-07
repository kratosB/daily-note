# AQS

## 引用
1. [由浅入深逐步讲解Java并发的半壁江山AQS](https://mp.weixin.qq.com/s/bxWgo9IuggDpE1l37JqEhQ)
2. [Java Concurrency代码实例之四-锁](https://zhuanlan.zhihu.com/p/27546231)
3. [AQS-为什么队列头节点的Thread是null](https://blog.csdn.net/weixin_38106322/article/details/107141976)
4. [一行一行源码分析清楚AbstractQueuedSynchronizer](https://www.javadoop.com/post/AbstractQueuedSynchronizer)
5. [AQS唤醒线程的时候为什么从后向前遍历，我懂了](https://blog.csdn.net/qq_37699336/article/details/124294697)
   1. 主要就是说，同步队列设置队尾，然后`t.next=node`的时候，cas是有保障的，但是if里面的内容（`t.next=node`）不是线程安全的，可能在这里上下文切换了，那么t.next=null。从前往后就会出错，但是从后往前不会，因为之前设置过`node.prev=t`。
   2. 这个问题同时解释了，为什么要先`node.prev=t`，然后再`compareAndSetTail`。在极端情况下可能会发生。