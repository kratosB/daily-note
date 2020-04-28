# HashMap的数据结构

HashMap也是我们使用非常多的Collection，它是基于哈希表的 Map 接口的实现，以key-value的形式存在。在HashMap中，key-value总是会当做一个整体来处理，系统会根据hash算法来来计算key-value的存储位置，我们总是可以通过key快速地存、取value。

对于HashMap，我们最常使用的是两个方法：Get 和 Put。

## 1. Put 方法的原理

调用 Put 方法的时候发生了什么呢？比如调用 hashMap.put(“apple”, 0) ，插入一个Key为“apple”的元素。这时候我们需要利用一个哈希函数来确定Entry的插入位置（index）：

    index = Hash（"apple"）

假定最后计算出的index是2，那么结果如下：

![图1](https://upload-images.jianshu.io/upload_images/10601062-a6156ca7e0fdab93.png)

但是，因为 HashMap 的长度是有限的，当插入的 Entry 越来越多时，再完美的 Hash 函数也难免会出现 index 冲突的情况。

比如下面这样：

![图2](https://upload-images.jianshu.io/upload_images/10601062-1fa5023db1576e3a.png)

这时候该怎么办呢？我们可以利用链表来解决。

HashMap 数组的每一个元素不止是一个 Entry 对象，也是一个链表的头节点。

每一个 Entry 对象通过 Next 指针指向它的下一个 Entry 节点。当新来的Entry映射到冲突的数组位置时，只需要插入到对应的链表即可：

![图3](https://upload-images.jianshu.io/upload_images/10601062-d46ffddf7d2f0135.png)

需要注意的是，新来的Entry节点插入链表时，使用的是“头插法”。至于为什么不插入链表尾部，后面会有解释。

## 2. Get方法的原理

使用 Get 方法根据 Key 来查找 Value 的时候，发生了什么呢？首先会把输入的 Key 做一次 Hash 映射，得到对应的 index：

    index = Hash（“apple”）

由于刚才所说的 Hash 冲突，同一个位置有可能匹配到多个Entry，这时候就需要顺着对应链表的头节点，一个一个向下来查找。假设我们要查找的Key是 “apple”：

![图4](https://upload-images.jianshu.io/upload_images/10601062-9a9e0aa9f86e5a32.png)

第一步，我们查看的是头节点 Entry6，Entry6 的 Key是banana，显然不是我们要找的结果。

第二步，我们查看的是 Next 节点 Entry1，Entry1 的 Key 是 apple，正是我们要找的结果。

之所以把 Entry6 放在头节点，是因为 HashMap 的发明者认为，后插入的 Entry 被查找的可能性更大。

## 3. HashMap的默认长度是16 ，自动扩展或初始化时，长度必须是2的幂

目的：服务于从Key映射到index的Hash算法

之前说过，从Key映射到HashMap数组的对应位置，会用到一个Hash函数

    index = Hash（“apple”）

如何实现一个尽量均匀分布的Hash函数呢？我们通过利用Key的HashCode值来做某种运算。

### Hash算法的实现采用了位运算的方式

如何进行位运算呢？有如下的公式（Length是HashMap的长度）：

    index = HashCode（Key） & （Length - 1）

下面我们以值为“book”的 Key 来演示整个过程：

1. 计算 book 的 hashcode，结果为十进制的 3029737，二进制的101110001110101110 1001。
2. 假定 HashMap 长度是默认的16，计算Length-1的结果为十进制的15，二进制的1111。
3. 把以上两个结果做与运算，101110001110101110 1001 & 1111 = 1001，十进制是9，所以 index=9。(与运算：和，同位上都为1则为1，否则为0)

可以说，Hash 算法最终得到的 index 结果，完全取决于 Key 的 Hashcode 值的最后几位。

## 备注
>1. [面试官：HashMap 源码你都不知道还来面试？](https://mp.weixin.qq.com/s/kXpRgz4KxKEcrJR9zMeybQ)
>
>2. [死磕 java集合之HashMap源码分析](https://mp.weixin.qq.com/s/0yN_qQhlSxvr3BMNUEg5mQ)
>
>3. [HashMap的数据结构](https://www.jianshu.com/p/518edb2d2d18)




















