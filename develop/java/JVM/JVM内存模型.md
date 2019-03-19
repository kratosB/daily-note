##　JVM运行时内存结构

JVM内存结构布局图  
![JVM内存结构布局图](https://mmbiz.qpic.cn/mmbiz_png/PgqYrEEtEnoUSbbnzEiafyyQWUibOfnE3GicpdRQOuxWBrhB3Fic7MRf4z5ywT2RmCicibGibHNQEgUbsibLR1eLVRfo3A/640 "JVM内存结构布局")

Java运行时的内存划分图  
![Java运行时的内存划分图](https://ws1.sinaimg.cn/large/006tNc79ly1fmk5v19cmvj30g20anq3y.jpg  "Java运行时的内存划分图")

Java运行时的内存划分图及设置参数  
![Java运行时的内存划分图及设置参数](https://ws1.sinaimg.cn/large/006tNbRwly1fxjcmnkuqyj30p009vjsn.jpg  "Java运行时的内存划分图及设置参数")

通过上图可以直观的查看各个区域的参数设置。
常见的如下：

* -Xms64m 最小堆内存 64m.
* -Xmx128m 最大堆内存 128m.
* -XX:NewSize=30m 新生代初始化大小为30m.
* -XX:MaxNewSize=40m 新生代最大大小为40m.
* -Xss=256k 线程栈大小。
* -XX:+PrintHeapAtGC 当发生 GC 时打印内存布局。
* -XX:+HeapDumpOnOutOfMemoryError 发送内存溢出时 dump 内存。

新生代和老年代的默认比例为 1:2，也就是说新生代占用 1/3的堆内存，而老年代占用 2/3 的堆内存。

可以通过参数 -XX:NewRatio=2 来设置老年代/新生代的比例。

---
### Java堆（Heap）

1. 所有线程共享
2. 存放类的实例（对象）和数组（数组本身，不是引用），不存放基本类型和对象引用
3. 堆分为年轻代和年老代
4. 年轻代内存又被分成三部分，Eden空间、From Survivor空间、To Survivor空间,默认情况下年轻代按照8:1:1的比例来分配
---
### 方法区（Method Area）

1. 所有线程共享
2. 存储已被虚拟机加载的类信息、常量、静态变量、即时编译器编译后的代码等数据
3. 方法区中包含的都是在整个程序中永远唯一的元素，如class，static变量

---
### 运行时常量池

1. 方法区的一部分，所以应该也是线程共享的
2. 存放了一些符号引用，当 new 一个对象时，会检查这个区域是否有这个符号的引用

---
### 程序计数器（Program Counter Register）

1. 线程隔离
2. 存放当前线程所执行的字节码的行号

---
### JVM栈（JVM Stacks）

1. 线程隔离
2. 存放栈帧

#### 栈帧

1. 每个方法被执行的时候都会同时创建一个栈帧（Stack Frame）用于存储局部变量表、操作栈、动态链接、方法出口等信息
    1. 局部变量表存放了编译期可知的各种基本数据类型（boolean、byte、char、short、int、float、long、double）
    2. 对象引用（reference类型，它不等同于对象本身，根据不同的虚拟机实现，它可能是一个指向对象起始地址的引用指针，也可能指向一个代表对象的句柄或者其他与此对象相关的位置）
    3.  returnAddress类型（指向了一条字节码指令的地址）
2. 每一个方法被调用直至执行完成的过程，就对应着一个栈帧在虚拟机栈中从入栈到出栈的过程

---
### 本地方法栈（Native Method Stacks）

1. 线程隔离
2. 虚拟机使用到的Native方法的内容





## 参考资料

>[Inside the Java Virtual Machine](https://www.artima.com/insidejvm/ed2/index.html "推荐")
>
> [java运行时内存分配详解](https://www.cnblogs.com/hewenwu/p/3662529.html)
> 
> [Java 运行时的内存划分](https://crossoverjie.top/JCSprout/#/jvm/MemoryAllocation)
> 
> [jvm系列(二):JVM内存结构](https://mp.weixin.qq.com/s?__biz=MzI4NDY5Mjc1Mg==&mid=2247483949&idx=1&sn=8b69d833bbc805e63d5b2fa7c73655f5&chksm=ebf6da52dc815344add64af6fb78fee439c8c27b539b3c0e87d8f6861c8422144d516ae0a837&scene=21#wechat_redirect)