# [ThreadLocal原理分析与使用场景](https://mp.weixin.qq.com/s/BP9Dp9SIFoyUySYKZj4ojQ)

1. ThreadLocal内部维护了一个Map，这个Map不是直接使用的HashMap，而是ThreadLocal实现的一个叫做ThreadLocalMap的静态内部类。
2. ThreadLocal主要用set，get，remove方法，调用的就是ThreadLocalMap的set，get，remove方法。
3. 每个线程，通过当前线程（这个对象）作为key，去ThreadLocal中set或者get的时候，可以获取对应的ThreadLocalMap。
4. 从这个ThreadLocalMap中，可以获取一个entry（key-value），key就是当前这个ThreadLocal的引用，value是存入的值。
5. key=ThreadLocal是弱引用，不论强弱引用，都会内存泄漏。（因为这个ThreadLocalMap对应的key（当前线程）一直都还在，所以ThreadLocalMap不会被回收）
6. 防止内存泄露的方式就是每次调用remove的时候，移除所有key=null的entry。
7. 用弱引用，是为了把key变成null，方便回收。如果是强引用，key就不是null，就不能回收。
8. 使用场景，存session，存connection。

## 引用
>1. [ThreadLocal原理分析与使用场景](https://mp.weixin.qq.com/s/BP9Dp9SIFoyUySYKZj4ojQ)
>2. [ThreadLocal 内存泄露问题](https://blog.csdn.net/jh39456194/article/details/107304997)