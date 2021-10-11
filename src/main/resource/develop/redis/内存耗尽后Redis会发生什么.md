# [内存耗尽后Redis会发生什么](https://cnblogs.com/lonely-wolf/p/14403264.html)

## 1. 设置过期时间的方式

1. expire key ttl：将key设置成ttl秒过期。
2. pexpire key ttl：将key设置成ttl毫秒过期。
3. expireat key timestamp：将key设置成指定的timestamp时刻过期。
4. pexpireat key timestamp：将key设置成指定的timestamp（毫秒）时刻过期。

这四个指令，底层都是pexpireat。

也可以set的时候同时设置过期时间，保证原子性。

## 2. 过期策略

1. 定时删除：给这个key搞一个定时器（类似job），一旦时间到了，就删除。
   1. 优点：不会浪费内存。
   2. 缺点：定时器占用cpu资源。
2. 惰性删除：每次用到这个key的时候，才判断是否过期，过期则删除。
   1. 优点：简单，不用额外的cpu。
   2. 缺点：浪费内存。
3. 定期删除：每隔一段时间，扫一部分key，如果扫到的key过期，则删除。
   1. 优点：cpu和内存都比较友好。
   2. 缺点：间隔时间不好搞，每次扫多少key也不好搞。

redis采用惰性+定期。定期只会扫设置过过期时间的数据。设置过过期时间的数据会单独存储，不会和没设置过期时间的数据放在一起。

## 3. 8 种淘汰策略

通过参数maxmemory-policy进行配置

1. volatile-lru: 针对设置超时时间的key，用lru算法删除key，直到有可用空间。
2. volatile-lfu: 针对设置超时时间的key，用lfu算法删除key，直到有可用空间。
3. volatile-random: 针对设置超时时间的key，用随机算法删除key，直到有可用空间。
4. volatile-ttl: 针对设置超时时间的key，根据过期时间，删除最近要过期的key，直到有可用空间。
5. allkeys-lru: 针对所有key，用lru算法删除key，直到有可用空间。
6. allkeys-lfu: 针对所有key，用lfu算法删除key，直到有可用空间。
7. allkeys-random: 针对所有key，用随机算法删除key，直到有可用空间。
8. noeviction: 不做操作，直接报错。

1-7种，如果没有可以删除的对象，内存还是不够用，那么就报错。

## 4. LRU（Least Recently Used，最长时间没有使用）

复杂，再议

## 5. LFU（Least Frequently Used，最少使用频率）

复杂，再议

## 引用
>1. [内存耗尽后Redis会发生什么](https://cnblogs.com/lonely-wolf/p/14403264.html)
>1. [内存耗尽后Redis会发生什么](https://mp.weixin.qq.com/s/XI2GZ_wwAAKi_9Rq7LMelA)