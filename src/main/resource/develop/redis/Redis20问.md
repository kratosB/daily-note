# [redis20问](https://mp.weixin.qq.com/s/7eat4HplDfMetaJdjUr9zg)

## 1. 什么是redis。

1. 基于内存的字典服务，多用于缓存。

## 2. redis基本数据结构。

1. 整个redis就是一个全局的hash表。
2. String
   1. 数字用int。
   2. 非数字小于39字节的用embstr。
   3. 非数字大于39字节的用raw。
   4. 底层用sds
      1. len：记录buf中已经使用的长度
      2. free：记录buf中没用的元素的长度
      3. buf[]：存放元素的数组
3. Hash
   1. key是hash的名字。（类似于HashMap xxx = new HashMap中的xxx）
   2. value是hash的内容（一个map），map才是java中hashMap类似的结构。
4. List
   1. 有序的字符串。
   2. ziplist
   3. linkedlist
   4. 应用场景：消息队列，文章列表
   5. 3.0之后用quicklist
5. Set
6. Zset
   1. 有序的set，应用场景：排行榜？用户点赞？
   2. zskiplist
7. Geospatial
   1. 附近的人
8. Hyperloglog
9. Bitmap

## 3. [redis为什么这么快。](https://mp.weixin.qq.com/s?__biz=MzkzMDI1NjcyOQ==&mid=2247487752&idx=1&sn=72a1725e1c86bb5e883dd8444e5bd6c4&source=41#wechat_redirect `Redis 核心篇：唯快不破的秘密`)

1. 基于内存实现
2. 高效的数据结构
   1. SDS动态字符串（各种空间换时间）
      1. len记录字符串长度，需要的话可以直接返回，不用遍历，O(1)复杂度。
      2. 空间预分配：SDS分配了额外空间，不用反复分配。
      3. 惰性释放：SDS缩短的时候，不回收内存，只用free记录。后续要变长，直接使用free记录的空间，减少分配。
      4. 二进制安全：c中用'\0'判断是否结束，SDS用len判断。
   2. hash表（整个redis就是一个全局的hash表，这个hash是在说redis的整体结构）
      1. value可以是任意这五种数据（String，hash，set，list，sorted set）
      2. 冲突了会使用链表。
      3. 也会rehash，用了两个全局hash表，渐进式rehash（不是一次性的），保证redis服务不阻塞（而且保证redis更快）。
         1. 给hash表2分配更大空间。
         2. 把hash表1的数据映射到hash表2。
         3. 释放hash表1。
   3. 跳跃表
      1. 链表的基础上，增加多级索引，提升查找效率。
   4. 压缩列表
      1. 数据量少，用压缩列表（省空间+查头尾很快）。
   5. 双端链表
      1. 前后指针
      2. 首尾指针
      3. 长度计数
   6. 后续版本用了quicklist，就是ziplist和linkedlist的合体。多个小ziplist，每个作为一个node，组成linkedlist。
3. 合理的数据编码
   1. String：如果存储数字的话，是用int类型的编码;如果存储非数字，小于等于39字节的字符串，是embstr；大于39个字节，则是raw编码。
   2. List：如果列表的元素个数小于512个，列表每个元素的值都小于64字节（默认），使用ziplist编码，否则使用linkedlist编码。
   3. Hash：哈希类型元素个数小于512个，所有值小于64字节的话，使用ziplist编码，否则使用hashtable编码。
   4. Set：如果集合中的元素都是整数且元素个数小于512个，使用intset编码，否则使用hashtable编码。
   5. Zset：当有序集合的元素个数小于128个，每个元素的值小于64字节时，使用ziplist编码，否则使用skiplist（跳跃表）编码。
4. 合理的线程模型
   1. IO多路复用（select，poll，epoll，主要是epoll，是最新的）
      1. redis多路复用是包装操作系统的IO多路复用函数库来实现的。
      2. linux上，多路复用是，多个IO注册到同一个管道，这个管道统一跟内核交互。当管道中的某一个请求需要的数据准备好之后，进程再把对应的数据拷贝到用户空间中。
      3. redis上，应该也是类似的。
         1. 多个socket每有一个操作，就会有一个文件事件，多个事件一起提交。
         2. 内核不监视连接，而监视文件事件。
         3. 一旦有请求（文件事件），旧交给redis线程处理。
         4. redis找到结果之后回调。redis线程不会阻塞灯带某一个连接完成。
      4. 6.0把多路复用变成多线程，是因为有些公司的并发量实在太大了，多路复用虽然能一定程度上解决IO阻塞问题，但是本质上还是同步阻塞型IO模型。
      [Redis不是一直号称单线程效率也很高吗，为什么又采用多线程了？](https://mp.weixin.qq.com/s/mscKInWNAuhCbg183Um9_g)
      [redis的多路复用是什么鬼](https://www.cnblogs.com/hujingnb/p/12439661.html)
   2. 单线程模型
      1. 因为redis的性能瓶颈是内存不是cpu，单线程避免cpu在上下文切换，以及锁的竞争。（单线程简单，便于开发维护）
      2. 缺点：执行时间过长的大命令会造成阻塞。
      3. 网络IO和键值对读写是由一个线程完成的，其他的如持久化存储模块、集群支撑模块等是多线程的。
      4. 6.0引入多线程（在IO部分），但是执行命令操作内存的仍然是单线程。
5. 虚拟内存机制
   1. redis自建vm。
   2. vm会把冷数据交换到磁盘，热数据放在内存。

## 4. 缓存击穿，缓存穿透，缓存雪崩。

1. 穿透
   1. 原因
      1. 非法请求。
      2. 数据误删。
      3. 业务设计不合理。
   2. 解决方案
      1. 在api入口进行校验，过滤非法请求。
      2. 设置空值缓存。
      3. 布隆过滤器。
2. 雪崩
   1. 原因
      1. 数据集中过期，所有的请求都打到数据库上，压力过大。
      2. redis故障。
   2. 解决方案
      1. 设置不同的过期时间。
      2. redis集群。
3. 击穿
   1. 热点key失效的时候，大量相同的请求直接打到数据库。
   2. 解决方案
      1. 热点key不过期。
      2. 互斥锁，让这些相同的请求只有一个打到数据库。单机用java锁，分布式用分布式锁。

## 5. 什么是热key问题，怎么解决。

1. 原因：
   1. 请求太多。
   2. 请求过于集中。
2. 解决方案
   1. redis集群扩容。
   2. 热key分散。
   3. 使用jvm本地二级缓存。

## 6. redis过期策略和内存淘汰策略。

1. 过期策略。
   1. 定时过期，给设置过期时间的key创建一个定时器，过期直接删除。缺点太耗cpu。
   2. 惰性过期，查到这个key的时候，判断是不是过期，如果过期，就删除。缺点太占内存。
   3. 定期过期，每隔一段时间，扫描一部分key，如果是过期的，则删除。缺点，时间段和删除量不好设置。
2. 内存淘汰策略。
   1. volatile-lfu：已设置过期时间的当中，最不频繁使用的删掉。
   2. volatile-lru：已设置过期时间的当中，最长时间没有使用的删掉。
   3. volatile-random：已设置过期时间的当中，随机闪。
   4. volatile-ttl：已设置过期时间的当中，最早过期的删。
   5. allkeys-lfu：所有key中，最不频繁使用的删掉。
   6. allkeys-lru：所有key中，最长时间没有使用的删掉。
   7. allkeys-random：所有key中，随机删。
   8. 直接报错。

## 7. redis常用场景。

1. 缓存。
2. 分布式锁。
    1. 数据量不大可以用数据库。数据量大了，影响数据库性能。
    2. redis setnx和lua，可以实现分布式锁。
3. 排行榜。zset，可以add，increase，排序。
4. 计数器。视频播放量，文章/网站阅读量，每次都要+1。频繁访问数据库不太行，redis天然支持+1，性能好。
5. 共享session。
6. 社交网络。
7. 消息队列。
8. 位操作。

## 8. [redis持久化机制。](https://mp.weixin.qq.com/s?__biz=MzkzMDI1NjcyOQ==&mid=2247487758&idx=1&sn=beb5918bb61948b2920907f54510311f&source=41#wechat_redirect `Redis 日志篇：无畏宕机快速恢复的杀手锏`)

1. RDB（redis database）
   1. redis默认的持久化方式。
   2. 内存数据以快照的形式存在磁盘上。
   3. 在指定的时间间隔内，执行指定次数的写操作，就把内存中的数据快照写入磁盘。
       1. save：主线程执行，会阻塞。
       2. bgsave：调用glibc函数fork一个子线程写rdb，不阻塞。
       3. 不阻塞，只代表可以读，不代表也可以写。所以要用COW技术，来让主线程也可以写。
       4. 使用COW（copy on write）技术，主线程写原本，子线程用副本。可以实现主线程写数据，子线程写RDB，同步进行。
       5. 已经过期的键不会被保存到新的RDB中。
   4. 优点：
       1. 适合大规模数据恢复，如备份，全量复制。
       2. 恢复速度快。
   5. 缺点：
       1. 生成频率不好把握。
       2. 没法实时持久化。（有可能会丢失数据）
       3. fork的过程，主进程需要拷贝自己的内存页表给子进程，过程可能会比较耗时。在完成fork之前，整个Redis实例会被阻塞住，无法处理任何客户端请求。
2. AOF
   1. 默认不开启。
   2. 用日志追加写文件的形式，记录每一个写操作。先写redis缓存，然后写日志。（所以被称为写后日志）
   3. 为了提高效率，会先写缓冲区，然后写磁盘。
       1. appendfsync = always：同步写回，性能低，可靠性好。
       2. appendfsync = everysec：每秒写回，性能可靠性折中。
       3. appendfsync = no：系统自己决定时间写磁盘，性能高，可靠性低。
   4. 优点：
       1. 缓存执行成功才写，避免语法检查开销。
       2. 数据一致性完整性高。
   5. 缺点：
       1. 文件可能会很大，恢复会很慢。
   6. AOF重写机制。
       1. 开辟一个子进程对内存进行遍历，转换成一系列 Redis 的操作指令。
       2. 序列化到一个新的 AOF 日志文件中。
       3. 序列化完毕后再将操作期间发生的增量 AOF 日志追加到这个新的 AOF 日志文件中。
       4. 完毕后就立即替代旧的 AOF 日志文件。
       5. 可以精简指令。（例如对key=1做了多次操作，精简之后只要最后一个就行了。）
3. Redis 4.0 混合日志模型
   1. 将 rdb 文件的内容和增量的 AOF 日志文件存在一起。
   2. RDB 内存快照以稍微慢一点的频率执行。
   3. 在两次 RDB 快照期间使用 AOF 日志记录期间发生的所有「写」操作。
   4. 恢复的时候，先用RDB恢复，然后用AOF恢复。
4. 使用建议：
   1. 允许分钟级别数据丢失，直接用RDB。
   2. 不允许数据丢失，使用混合。
   3. 只用AOF，推荐使用everysec。

## 9. redis高可用

1. [主从模式](https://mp.weixin.qq.com/s?__biz=MzkzMDI1NjcyOQ==&mid=2247487769&idx=1&sn=3c975ea118d4e59f72df5beed58f4768&chksm=c27c532ff50bda39055fc4e6dabf5bb0b6cc2945a4cad87782c8e46fdb32bb67beaa38438c65&scene=178&cur_album_id=1918295695426404359#rd `Redis 高可用篇：你管这叫主从架构数据同步原理？`)
   1. 作用
      1. 故障恢复。
      2. 负载均衡。
      3. 高可用基础。
   2. 主节点负责读写，从节点负责读，主从之间使用主从复制。
      1. 不用AOF，因为RDB传输和写磁盘效率高，而且RDB恢复速度也更快。
   3. 主从之间的三种情况。
      1. 第一次主从库全量复制。
         1. slave发送同步请求。
         2. master执行bgsave命令生成RDB文件，并发给slave。
         3. master同时为**每一个slave**开辟一块**replication buffer**，记录从bgsave开始的所有写命令。
         4. slave加载完RDB之后，master把replication buffer的数据发给slave，继续同步。
      2. 主从库间网络断开重连。
         1. 2.8之前，网络断开就会重新全量复制。2.8之后，主从库用增量复制继续同步。
         2. 增量复制：网络中断等情况后的复制，只发送中断期间主节点执行的写命令到从节点，比全量复制更高效。
         3. master维护一个**repl_backlog_buffer**缓冲区，类似redo log的环形数据区，使用覆盖写。
         4. master记录自己写的**master_repl_offset**，每个slave记录读的**slave_repl_offset**。
         5. 断开重连之后，slave发送自己的offset，master把offset差部分发给slave就即可。
         6. slave_repl_offset被slave_repl_offset套圈，就要重新全量复制。所以buffer要大一点。
      3. 正常运行的同步。（基于长连接的命令传播） 
         1. 除了发送写命令，主从节点还维持着心跳机制：master发送PING，slave发送REPLCONF ACK。
         2. slave发送REPLCONF ACK，会带上slave_replication_offset，master会返回对应数据。
   4. 缺点：master挂了，得手动把slave升级成master。而且应用方也得升级（比如写数据，要写新的master）
2. [哨兵模式](https://mp.weixin.qq.com/s?__biz=MzkzMDI1NjcyOQ==&mid=2247487780&idx=1&sn=9a0ea0971e661556c4c5e438ab1b081b&chksm=c27c5312f50bda04231254e78736d151f789ef056f43d36f7cd861c70f0cb54b7e26ea03d5d4&scene=178&cur_album_id=1918295695426404359#rd `Redis 高可用篇：你管这叫 Sentinel 哨兵集群原理`)
   1. redis2.8正式提供sentinel（哨兵）来解决主从模式的问题。
   2. 哨兵的主要功能：
      1. 监控master和slave的工作状态。
      2. 在master挂掉的时候，选举切换新的master。
      3. 通知其他slave，更换master。
   3. 监控
      1. 哨兵和其他哨兵，master，slave之间保持一个心跳（每秒ping一下）。
      2. 其他节点会回复（pong，loading，masterdown），不回复的就会被标记成**主观下线**（主观的意思是我看不到了，可能对面是好的，我自己挂了）。
      3. 如果超过一半哨兵（所以一般都配置奇数个哨兵）标记master主观下线，那么msater就**客观下线**了（大家都看不到他，大概率是他挂了）。
   4. 自动切换主库
      1. 哨兵会按照一定的标准打分，选出新的master。
      2. 筛选条件
         1. 没下线的slave。
         2. 网络状态好的。
      3. 打分
         1. 可以手动设置优先级。
         2. slave_repl_offset最新的（哪个从库跟老master更接近哪个更好）。
         3. slave runID（大概可以理解为，哪个slave正常运行最久那就最可靠）。
   5. 通知
      1. 通知其他slave来replacaof新的master。
      2. 通知应用程序/客户端访问新的master（写redis）。
   6. 哨兵工作原理
      1. 配置哨兵的时候，每个哨兵都只设置了监控master ip和port。
      2. 哨兵与master通信，master有一个**__sentinel__:hello**的专用通道。  
      哨兵利用master的pub/sub机制，发布自身信息，并订阅其他哨兵的信息。
      3. 哨兵向master发送INFO命令，master将slave列表告诉哨兵。哨兵以此跟slave建立连接，并监控slave。
   7. 选择哨兵执行主从切换
      1. 跟选master一样，执行master切换的哨兵，也是选举出来的。
   8. 通过pub/sub实现客户端通知
      1. 客户端订阅哨兵消息。
3. [集群模式](https://mp.weixin.qq.com/s?__biz=MzkzMDI1NjcyOQ==&mid=2247487789&idx=1&sn=7f8245f8b4e4a98aa0a717011f7b7e24&chksm=c27c531bf50bda0da3bcc325b131dac2553eb508fed4175ab1d883fb4946557fb91053ddb525&scene=178&cur_album_id=1918295695426404359#rd `Redis 高可用篇：Cluster 集群能支撑的数据有多大？`)
   1. 起因
      1. redis单机保存数据太多的时候，fork耗时太高（fork耗时与数据量成正相关），导致阻塞。
      2. 单机读写10w qps，在千万级面前不够看。
   2. 集群的原里。
      1. 集群采用了哈希槽（Hash Slot）。
         1. 整个集群被划分为16384个slot，每个redis实例负责一部分slot。
         2. 根据键值对的key，使用CRC16算法，计算出一个16bit的值。
         3. 将16bit的值对16384取模，得到的余数表示对应的slot。
         4. 根据slot找到对应的redis实例，存储或者查询。
      2. Hash slot与redis实例映射。
         1. 创建redis集群的时候，可以让redis自动平均分配slot，也可以手动指定slot。
         2. 当16384个slot全部分配完，redis集群才能正常工作。
      3. 集群的复制与故障转移。
         1. 集群模式下，slave只作为备份，不做读写分离。若master故障，集群自动选slave升级。 
         2. 如果某master没有slave，当master故障时，整个集群都不可用。  
         **设置cluster-require-full-coverage参数**可以允许部分节点故障，其他节点继续提供访问。
         3. 大多数master发现某个master失联了，集群才认为它下线了，需要进行主从切换。
         4. 用raft算法选举新的master。新的master会把slot指派给自己，然后向集群广播一条pong。
      4. 用表保存键值对和实例的关联关系可行么。
         1. 如果使用全局表记录，假如键值对和实例之间的关系改变（重新分片、实例增减），需要修改表。
         2. 单线程，串行操作，性能太差。吞吐方面不行。多线程需要加锁，太复杂。
         3. 全局表占用空间还很大。使用全局表，跟单机redis区别不大。
         4. 计算Hash开销很小。记录slot和实例的关系，开销也不大。
   3. 客户端如何定位数据所在实例。
      1. Redis实例会将slot信息通过Gossip协议发送给其他实例，实现了slot信息的扩散。集群中每个实例，都有所有slot和实例的映射关系。
      2. 客户端连接任意实例，实例就将slot与实例的映射关系响应给客户端。然后客户端将映射信息缓存在本地。
      3. key的CRC16计算，取模，在客户端进行。算出对应的slot后，根据本地缓存的数据定位对应的实例。 
      4. hash slot与redis实例对应关系改变。
         1. 原因：
            1. 新增/减少实例。
            2. 负载均衡重新分配。
         2. 集群中通过Gossip协议互相传递/更新slot分配消息。、
         3. 集群提供了重定向机制。客户端给实例发送请求，实例没有对应数据，实例会高苏客户端将请求发给其他实例。
         4. 方案：
            1. moved错误。（由于slot的映射变了）数据已经迁移到其他实例上。
               1. 实例会返回一个MOVED错误，告诉客户端，该slot已经被映射到新的实例（IP+PORT）。
               2. 客户端与新的实例建立连接，发送请求。（同时更新本地缓存，将该slot映射到新的实例）
            2. ask错误。部分迁移到新实例，还有一部分没有迁移。
               1. 如果找到对应key的数据，直接返回。
               2. 如果没找到，返回ASK错误，然后告诉客户端，重定向请求到新的实例（IP+PORT）。
               3. 客户端给新的实例发送一个ASKING命令，然后再发送操作命令。但是**不更新slot和实例映射信息**。
   4. 集群可以设置多大
      1. 官方说1000个实例。
      2. 集群规模的增加，导致心跳消息占用网络带宽过多，降低吞吐量。所以集群中的通信限制了集群的规模。
      3. 实例的通信频率。
         1. 默认每秒从实力列表中选5个，从中找到一个最久没有手倒ping消息的实例，（所有实例）把ping消息发给该实例。
         2. 每100ms就会扫描本地实例列表，发现太久没收到PONG消息的实例（超过cluster-node-timeout/2），就立刻给他发PING。
         3. 修改cluster-node-timeout的值，可以让PONG消息频率缓解，避免心跳占用太多带宽。
         4. cluster-node-timeout也不能太大，不然服务故障不能及时被发现。

## 10. redis分布式锁，以及注意事项。

1. setnx key value PX expireTime这么写不合法，必须写成set key value NX pX expireTime。
2. 带上value是为了防止锁被别人解锁。
3. 删除锁要用lua，因为要判断value，又要删除，又要原子性。
4. 可重入，也可以通过lua实现，就是把value做成一个map（value:count）。

## 11. redisson及其原理。

1. 功能：
   1. 防止业务逻辑还没做完，锁已经过期了。
   2. 可重入。
2. 超时时间如果太大，那么万一程序crash，其他线程就都要等很久了。
3. redisson用守护线程，每隔一段时间去检查线程状态，还在运行，就给超时时间续费。
4. Redisson 类库就是通过 Redis Hash 来实现可重入锁。（其实就是key还是key，value换成了一个map，map的key是原本的value，map的value是count）
5. 也是用lua实现的（复杂操作+原子操作，问就是lua）。

## 12. redlock算法。（未完待续）


## 13. redis跳跃表。（未完待续）



## 14. [mysql和redis双写一致性。](https://mp.weixin.qq.com/s/2fARuMP5gACAI8VP2jGXpw `美团面试题：缓存一致性，我是这么回答的！`)

1. 先删缓存，再更新数据库。
   1. 缺点：更新之前又有人查了，那数据就不一致了。
   2. 方案，延迟双删。
      1. 延迟双删的问题：1. 一点点延迟。2. 第二次删除有可能失败，造成数据不一致。
2. 先更新数据库，再删缓存
   1. 缺点
      1. 更新完了，删缓存失败，数据不一致。
      2. 有延迟。
   2. 方案
      1. job，消息队列来做，复杂+有延迟。
      2. binlog来做，延迟还是有，但是延迟很少，就跟mysql主从一样，能忍。
3. 设置过期时间（最终一致性）
4. 删缓存，而不是更新缓存。
   1. 因为更新了不一定会用上，白搞。

## 15. 为什么redis6.0之后改多线程。

1. 之前，主流程用IO多路复用（包括读socket、解析、执行、写socket等）+单线程操作内存。
2. 之后，IO部分换成多线程来处理数据的读写和协议解析，是为了尽可能的增大QPS，增强性能。

## 16. redis事务机制。（未完待续）
## 17. redis的hash冲突。

1. 链式hash。
2. redis默认使用两个全局hash表，以提高rehash效率。

## 18. 生成RDB期间，redis可以同时处理写请求吗？

1. save会阻塞。
2. bgsave不阻塞，但是被操作的数据不能写，只能读。
3. 用了COW之后，就可以同时处理写请求了。

## 19. redis底层使用什么协议。

1. RESP：redis serialization protocol
2. 实现简单、解析速度快、可读性好

## 20. 布隆过滤器。

1. 组成：由一个很大的二进制向量+一组Hash函数组成。
2. 原理：
   1. 有新数据的时候，新数据要使用这几个Hash来计算，然后在向量的对应位置放1。
   2. 查询的时候，将被查询项也做进行这几个Hash计算，然后去对应位置上找，如果都是1，则有可能存在。如果任意一个不是1，就肯定不存在。
3. 优点：
   1. 用很小的空间+计算，就能判断数据是否存在，效率（时间/空间）高。
4. 缺点：
   1. 找到的，不一定存在，称为假阳性。
   2. 不能删除。

## 引用
[2W字！详解20道Redis经典面试题！（珍藏版）](https://mp.weixin.qq.com/s/7eat4HplDfMetaJdjUr9zg)
[Redis不是一直号称单线程效率也很高吗，为什么又采用多线程了？](https://mp.weixin.qq.com/s/mscKInWNAuhCbg183Um9_g)
[Redis 日志篇：无畏宕机快速恢复的杀手锏](https://mp.weixin.qq.com/s?__biz=MzkzMDI1NjcyOQ==&mid=2247487758&idx=1&sn=beb5918bb61948b2920907f54510311f&source=41#wechat_redirect)

