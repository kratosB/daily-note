# HashMap面试题

1. HashMap的数据结构。
   1. 底层是数组+链表的结构。数组里装着链表（的头节点）。
2. HashMap的存取原里。（主要是put和get的流程）
   1. put：
      1. 根据key计算hash值。
      2. 跟数组容量做计算，找到对应的数组下标。
      3. 看看数组是否需要扩容。
      4. 添加/更新节点。
   2. get：
      1. 根据key计算hash值。
      2. 跟数组容量做计算，找到对应的数组下标。
      3. 遍历链表找到对应的节点。
3. hashCode，equals为什么要重写，相同/不同会怎么样。
   1. hashCode用于定位数组下标，equal用于判断这个链表上的节点和目标节点是否相等。
   2. 不重写hashCode，同样的两个Object（name=a，age=1）可能会出现两个不同的hashCode，然后被存在数组中不同的位置。所以要重写hashCode。
   3. 重写hashCode，不重写equals，同样的两个Object（name=a，age=1）会被定位到数组的同一个位置。但是在判断链表中的数据是否相等时，判断为不相等，所以两组相同的数据就会共存。
4. hash的实现及原因。
   1. 先计算hashCode。
   2. hashCode的高16位和低16位做异或。
   3. 好处是：
      1. 速度快
      2. 区分度高，减少碰撞，任何一位变动都会改变整个hash值。
      >因为最后算index的时候需要用hash和length-1做与，length-1很小，不做扰动的话，其实只有hash的最后几位会被用到。一些特殊情况（例如等差数列）就会分布不均匀。
5. 扩容，以及，容量为什么是2的倍数。
   1. 有一个因子，默认0.75，当size达到数组length*0.75的时候，就会扩容两倍。
   2. 2倍的原因：
      1. 计算index可以用除，但是与比除效率更高。2的倍数-1可以直接与。
6. 1.7和1.8的区别。
   1. 红黑树。长度超过8，且总数超过64。变回来的话是长度小于6。
   2. 头尾插。
   3. 扰动次数。
7. 头插和尾插。
   1. 多线程头插会成环。
   2. 尾插可能数据丢失/put和get值不同。
8. ConcurrentHashMap。
   1. 1.7：
      1. 分段锁。
      2. ReentrantLock
   2. 1.8：
      1. 锁头节点。
      2. cas+synchronized

## 引用
>[HashMap 面试题](https://mp.weixin.qq.com/s/Fhks3dHxAdsQL3RrLr8xlQ)
>[《进大厂系列》系列-HashMap](https://www.cnblogs.com/aobing/p/12014271.html)
>[都说知道 HashMap 线程不安全，它为啥不安全](https://mp.weixin.qq.com/s/K-BvEsMN1qTRhmvK5KJ3qQ)
>[HashMap的数据结构](https://www.jianshu.com/p/518edb2d2d18)
>[扰动函数](https://www.zhihu.com/question/20733617/answer/111577937)