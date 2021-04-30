# [都说知道 HashMap 线程不安全，它为啥不安全？](https://mp.weixin.qq.com/s/K-BvEsMN1qTRhmvK5KJ3qQ)

1. 在jdk1.7中，多线程环境下，resize的transfer方法，扩容时会造成环形链或数据丢失。
2. 在jdk1.8中，多线程环境下，会发生数据覆盖的情况。

具体参考文章，有详解。（基本上就是node.next设置成null或者第一个node之类的情况）

## 引用
>[都说知道 HashMap 线程不安全，它为啥不安全？](https://mp.weixin.qq.com/s/K-BvEsMN1qTRhmvK5KJ3qQ)