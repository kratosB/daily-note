# ThreadLocal，FastThreadLocal

1. 为什么需要ThreadLocal。
   1. 完全线程隔离。
2. ThreadLocal的结构。
   1. Thread里面放ThreadLocalMap。
   2. ThreadLocalMap里key=ThreadLocal实例，value=值。 
   3. 多线程环境下，每隔线程都有自己的ThreadLocalMap，用同一个key（ThreadLocal实例）。
   4. hash冲突跟hashMap不一样，直接往后找空位置，满了就扩容。
3. 为什么要用弱引用。
   1. 线程池线程不常变更，会导致ThreadLocalMap和Entry不会收。所以Entry就会永远引用key（ThreadLocal实例）。
   2. 做成弱引用，当栈上对ThreadLocal实例的引用断了，，key（ThreadLocal实例）就可以回收了。
4. 内存泄漏解决办法。
   1. 每次set，get的时候，找到key=null的value就会被回收。（没找到就不会回收）
   2. 每次使用完手动回收。
5. FastThreadLocal要搭配InternalThreadLocalMap，FastThreadLocalThread使用。
6. FastThreadLocal结构。
   1. 内置一个index，全局共享，逐步累加。个人理解是，jvm启动之后，  
   第一个FastThreadLocal，index=1，那么第二个就=2，依次累加，即使前面的已经gc掉了。
   2. 用AtomicInteger nextIndex来实现。 
7. InternalThreadLocalMap的结构
   1. 直接用Objectp[]，[]中的值，就是FastThreadLocal的index。
   2. 这里会出现一个**空间浪费**的问题。比方说之前有99个FastThreadLocal实例。这时候new了  
      一个新的FastThreadLocal实例，它的index=100。然后一个新的FastThreadLocalThread  
      调用了FastThreadLocal.set()，那么它内部的InternalThreadLocalMap，  
      前面99个格子（不考虑set）都是空的，第100个才放了这个新东西。
   3. 主要是为了空间换时间

## 引用
1. [我把 ThreadLocal 能问的，都写了](https://mp.weixin.qq.com/s?__biz=MzkxNTE3NjQ3MA==&mid=2247491733&idx=1&sn=2a4efe9f12a6d3009d89d703e7dadaa5&chksm=c1618decf61604fa0eb46bb65e31248db2bd555527dc558b19c5a60a2be634a24e5e1bc79e0c&scene=21#wechat_redirect)
2. [原来这就是比 ThreadLocal 更快的玩意](https://mp.weixin.qq.com/s?__biz=MzkxNTE3NjQ3MA==&mid=2247491864&idx=1&sn=a3854250a148526e9136427b738dc95a&chksm=c1618c61f6160577400cf0d33a14545eb9aee2b15d4ff767f9e93ac2d75559f84374d361008b&scene=21#wechat_redirect `fastThreadLocal`)
3. [ThreadLocal的短板，我TTL来补！](https://mp.weixin.qq.com/s?__biz=MzkxNTE3NjQ3MA==&mid=2247492913&idx=1&sn=3a5efee12ca65f3c3113930b1f0e35d0&chksm=c1618848f616015e20a316cc7cbd81806f2d853c314385af36ce1ff4a6adc797b1b822009050&scene=178&cur_album_id=2058599863742758913#rd)
4. [我把之前写的有关 ThreadLocal 的所有，在这篇做个汇总](https://mp.weixin.qq.com/s/bECVeuxE-WIYmvXbF2V3QA)