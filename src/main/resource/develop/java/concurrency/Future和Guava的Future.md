# [笑了，面试官问我知不知道异步编程的Future。](https://mp.weixin.qq.com/s?__biz=MzAxNjk4ODE4OQ==&mid=2247495717&idx=2&sn=8b256ec1a70d0d0c473437b6407e1e2e&chksm=9beed157ac995841ce0951e5aed992818573ddf7f874f5d3f8fbfd5cc3b90b516cdbdbb0cf4e&xtrack=1&scene=90&subscene=93&sessionid=1606208128&clicktime=1606210999&enterid=1606210999&ascene=56&devicetype=android-29&version=270014e5&nettype=WIFI&abtest_cookie=AAACAA%3D%3D&lang=zh_CN&exportkey=AobtH4SWUZyp7y5AcMBc1BA%3D&pass_ticket=8TRsta7hMCGVhU8DpfBd2ZcWqdmLOX4uD9CyCMbA6Et%2FH3NkVKd39xzIXtWbPkfC&wx_header=1)

1. 先介绍了future
2. 介绍了guava的future
3. 介绍了1.8的CompletableFuture

主要去别就是，future要在主线程中get返回结果，其他两个可以在另一个线程（非主线程）中处理返回结果。

但是个人觉得其实本质没啥区别，future也可以再放进其他线程中吧。