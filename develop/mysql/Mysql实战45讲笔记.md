## 3，事务隔离：为什么你改了我还看不见？


### 引用
>* [关于MySQL的可重复读的理解](https://blog.csdn.net/qq_32573109/article/details/98610368)
>* [隔离级别+不可重复读+幻读](https://blog.csdn.net/zzp448561636/article/details/80917085)
>
>可重复读没看懂，搜了点资料，第一个帖子看了，有点似懂非懂。第二个帖子还没看，就扫了一眼。奇怪的是，这俩帖子的参考资料，就是我看的45讲，我也是醉了，我怎么没看出来这么多信息。
>
>* [MySQL使用可重复读作为默认隔离级别的原因](https://www.cnblogs.com/vinchen/archive/2012/11/19/2777919.html)
>
>有空可以看看上面这个帖子，可能有用。

## 4，深入浅出索引（上）

1. Hash查询快，但是区间查询慢。
2. 有序数组查询快，新增慢。
3. 二叉树层数多，读磁盘多，速度慢，所以用**多叉树**。
4. 自增主键的好处:
    1. 有序，插入快。
    2. 二级索引节省空间。（因为二级索引等于是key-value结构，key是索引，value是主键，自增主键占用空间小）
5. 主键唯一，没有其他索引，没有其他查询需求，可以用业务字段做索引。

## 5，深入浅出索引（下）

1. 使用覆盖索引，避免回表，尤其，**范围查询要回表很多次**。
2. 最左前缀原则。
3. 索引下推:
   1. 比如select * from tuser where name like '张%' and age=10 and ismale=1;
   2. 先通过(name,age)的索引，找到'张%'匹配的地方。
   3. 然后MySQL5.6之前的版本，数据库会直接开始回表比对字段。
   4. 5.6之后的版本引入了索引下推优化。可以在索引遍历过程中，对索引中包含的字段先做判断，直接过滤掉不满足条件的记录，减少回表次数。
   
## 6，全局锁和表锁 ：给表加个字段怎么有这么多阻碍？

>锁太复杂了，不涉及锁的实现细节。
1. 全局锁（类似全库逻辑备份的场景可以用，避免表不一致。如果不加锁，备份的时候如果用户在做操作，部分操作被备份了，部分没有）:
    1. 方法1：通过Flush tables with read lock命令，让整个库处于只读状态。其他线程的以下语句会被阻塞：数据更新语句（数据的增删改）、数据定义语句（包括建表、修改表结构等）和更新类事务的提交语句。
    2. 方法2：mysqldump使用参数–single-transaction启动一个事务，能拿到一致性读视图，可以达到全库只读。但是只有innodb可以用。
    3. 方法3：set global readonly=true也可以达到全库只读。但是readonly有别的用处（比如主从备份）。而且readonly风险更高（连接断开时，FTWRL会自动释放锁，readonly永存）。
2. 表级锁（分为两种，表锁，和，元数据锁（meta data lock，MDL））:
    1. 表锁：
        1. 语法是lock tables ... read/write。可以用unlock tables主动释放，也可以连接断开的时候自动释放。
        2. 表锁也限制自身。比如，在A线程中执行lock tables t1 read, t2 write;。那么，其他线程写t1，读t2都会被阻塞。同时，在unlock之前，A线程也只能读t1，写t2（甚至写t1都不行）。
            >其实也很好理解，如果能写t1，那么不就等于悄悄升级读锁为写锁？其他线程的读t1岂不是出错了？
        3. innodb一般用行锁。
    2. 元数据锁（应该是指表结构层面）：
        1. MDL是自动的。为了保证读写的正确性（比如线程A在遍历，线程B删了一列，那不是出错了）。
        2. 给表加字段的时候，如果改动时间久，MDL会造成后面的查询的阻塞，要慎重（设置超时，或者其他别的途径）。

## 7，行锁功过：怎么减少行锁对性能的影响？

1. 两阶段锁
    1. 在innodb事务中，行锁是在需要的时候才加上的，但并不是不需要了就立刻释放，而是要等到**事务结束时才释放**。这个就是两阶段锁协议。
    2. 如果你的事务中需要锁多个行，要把最可能造成锁冲突、最可能影响并发度的锁尽量往后放。
        ```text
       假设你负责实现一个电影票在线交易业务，顾客A要在影院B购买电影票。我们简化一点，这个业务需要涉及到以下操作：
       1 从顾客A账户余额中扣除电影票价；
       2 给影院B的账户余额增加这张电影票价；
       3 记录一条交易日志。
       
       首先，一个人很难在自己买票的时候做其他操作，所以1比较难冲突。
       如果有另一个顾客C也要买票，那么影院B的帐户又会变动，所以第二句更容易冲突。
       所以应该设计成132或者312这种顺序。这就最大程度地减少了事务之间的锁等待，提升了并发度。
        ```
2. 死锁和死锁检测
    1. 不同线程出现循环资源依赖，涉及的线程都在等待别的线程释放资源时，就会导致这几个线程都进入无限等待的状态，称为死锁。
    2. 解决死锁有两种策略：
        1. 超时处理。
            1. 超时时间太大，造成长时间等待，在线服务难以接受。
            2. 超时时间太小，普通（非死锁）等待容易被误伤。
        2. 发起死锁检测（innodb_deadlock_detect），发现死锁，并主动回滚一个死锁事务。
            1. 消耗cpu资源。
            2. 关掉不必要的死锁检测（有风险，可能会出现大量超时）。
            3. 控制并发度。
                1. 需要专业人才。
                2. 可以考虑通过将一行改成逻辑上的多行来减少锁冲突。
                
## 8，事务到底是隔离的还是不隔离的？

1. begin/start transaction并不是事务马上执行，它之后的第一个操作innodb表的语句，事务才真正启动。如果你想要马上启动一个事务，可以使用start transaction with consistent snapshot。
2. 没有显式使用begin/commit的update语句本身就是一个事务，语句完成的时候自动提交。
3. 一致性读视图（consistent read view），用于支持RC（Read Committed）和RR（Repeatable Read）隔离级别的实现。
    1. 每个事务有一个唯一的transaction id，按申请顺序递增。
    2. 每行数据都有多个版本。事务更新时生成新数据版本，把transaction id赋值给这个版本，记为row trx_id。
        > * 旧版本保留，新版本可追溯旧版本
        > * 这些数据版本，是根据对应的undo log计算出来的。
    3. 事务创建的瞬间，创建**一致性读视图**（构造一个数组，存储了当前正在"活跃（启动但未提交）"的所有事务的ID）。
        >所有ID从小到大排列，最后一个是系统里已经创建过的的事务的ID的最大值+1（这个事务自己的ID）。
        1. 如果一个数据版本的row trx_id比数组中最小ID的还小，这个版本是已提交的事务或者是当前事务自己生成的，这个数据是可见的。
        2. 如果row trx_id比数组中最大ID的还大，这个版本是由将来启动的事务生成的，是肯定不可见。
        3. 如果row trx_id介于两者之间，且在数组中，表示这个版本是由还没提交的事务生成的，不可见。
        4. 如果row trx_id介于两者之间，且不在数组中，表示这个版本是已经提交了的事务生成的，可见。
            >情况4和情况1的区别是
            >* 1当中的事务，启动时间比当前存活的事务都早，且已经提交。
            >* 4当中的事务，启动时间介于当前存活的事务之间，但是也已经提交，而且提交时间早于当前事务。
    4. 事务查询逻辑：
        1. 自己的更新，总是可见。
        2. 版本未提交，不可见。
        3. 版本已经提交，但是在视图创建之后提交，不可见。
        4. 版本已经提交，在视图创建之前提交，可见。
    5. 事务更新逻辑：
        1. 更新数据都是先读后写的，而这个读，只能读当前的值，称为"当前读"（current read）。
        >select语句如果加锁，也是当前读。select语句加上lock in share mode（读锁，共享锁）或for update（写锁，排他锁），就变成当前读。
    6. 读提交：
        1. 逻辑和可重复读基本类似，区别是，在读提交隔离级别下，每一个语句执行前都会重新算出一个新的视图。
```text
举几个例子，

* 例子1，可重复读情况下

|transaction A(ID100)           |transaction B(ID101)           |transaction C(ID102)           |
|start transaction              |                               |                               |
|                               |start transaction              |                               |
|                               |                               |update t set k=k+1 where id=1; |
|                               |update t set k=k+1 where id=1; |                               |
|                               |select k from t where id=1;    |                               |
|select k from t where id=1;    |                               |                               |
|commit;                        |                               |                               |
|                               |commit;                        |                               |
例子1中，
事务A查询的时候，事务B还没提交，事务B影响的数据的row trx_id是101。
从3-iii-b角度分析，101比数组中最大的ID还大，所以不可见。
从3-iv-b角度分析，事务B还没提交，所以不可见。
此时事务C已经提交了，但是事务C提交的数据的row trx_id是102。
从3-iii-b角度分析，102比数组中最大的ID还大，所以不可见。
从3-iv-c角度分析，C已经提交，但是在视图创建之后提交，所以也是不可见。

事务B update之前，C已经update，并且commit了，所以B要使用当前读。
所以事务B的update语句获取到的k=2，+1之后=3。
又因为这个update是自己的更新，总是可见，所以事务B select出来就是3（符合3-iii-a和3-iv-a）。

* 例子2，可重复读情况下

|transaction A(ID100)           |transaction B(ID101)           |transaction C(ID102)           |
|start transaction              |                               |                               |
|                               |start transaction              |                               |
|                               |                               |start transaction              |
|                               |                               |update t set k=k+1 where id=1; |
|                               |update t set k=k+1 where id=1; |                               |
|                               |select k from t where id=1;    |                               |
|                               |                               |commit;                        |
|select k from t where id=1;    |                               |                               |
|commit;                        |                               |                               |
|                               |commit;                        |                               |
例子2中，
事务C，没有立即提交，但是row trx_id是102的数据版本已经生成了。这时候，就要结合"两阶段锁协议"。
事务C还没提交，所以锁还没有释放。
事务B是当前读，必须读最新版本，而且必须加锁，因此就阻塞。必须等到事务C释放这个锁，才能继续它的当前读。

* 例子3，读提交情况

|transaction A(ID100)           |transaction B(ID101)           |transaction C(ID102)           |
|start transaction              |                               |                               |
|                               |start transaction              |                               |
|                               |                               |update t set k=k+1 where id=1; |
|                               |update t set k=k+1 where id=1; |                               |
|                               |select k from t where id=1;    |                               |
|select k from t where id=1;    |                               |                               |
|commit;                        |                               |                               |
|                               |commit;                        |                               |
例子3中，
事务A查询的时候，
事务B还没提交，符合3-iii-c和3-iv-b，所以不可见。
事务C已经提交，符合3-iii-a和3-iv-d，所以可见。
所以A查询返回的是k=2。


事务B查询的时候，
事务C已经提交，符合3-iii-a和3-iv-d，所以可见。
事务B自己的提交符合3-iii-a和3-iv-a，所以可见。
所以A查询返回的是k=3。
```
这三个例子，把一致性读、当前读和行锁就串起来了。

## 9，普通索引和唯一索引，应该怎么选择？

>基于业务已经确定不会有重复写入的情况来探讨。
1. 查询角度，有区别，性能差距微乎其微。
    1. 唯一索引：找到对应的记录之后就不查了。
    2. 普通索引：找到对应记录之后，继续找，直到找不到为止。
        >innodb按照数据页为单位来读写，每个数据页的大小是16KB，能存放近千个key，所以找到对应记录之后，下一个记录大概率也已经被加载到内存中了。所以这个操作只需要一次指针寻找和一次机算就够了，影响微乎其微。
2. change buffer。
    1. 需要更新一个数据页时，如果这个数据页没有在内存中，在不影响数据一致性的前提下，innodb会将这些更新操作缓存在change buffer中。
        >这样就不需要从磁盘中读入这个数据页了。在下次查询需要访问这个数据页的时候，将数据页读入内存，然后执行change buffer中与这个页有关的操作。通过这种方式就能保证这个数据逻辑的正确性。
    2. 减少了随机磁盘访问，对更新性能有明显的提升。
    3. 虽然名字叫作change buffer，实际上它是可以持久化的数据。change buffer在内存中有拷贝，也会被写入到磁盘上。
    4. 将change buffer中的操作应用到原数据页，得到最新结果的过程称为merge。
        1. 从磁盘读入数据页到内存（老版本的数据页）。
        2. 从change buffer里找出这个数据页的change buffer 记录(可能有多个），依次应用，得到新版数据页。
        3. 写redo log。这个redo log包含了数据的变更和change buffer的变更。
        4. merge结束，此时磁盘还没修改，刷回物理数据是另一个过程。
        >除了访问这个数据页会触发merge外，系统有后台线程会定期merge。在数据库正常关闭（shutdown）的过程中，也会执行merge操作。
    5. 适合写多读少的业务，不适合写少读多的业务。
        >因为每次读都会出发merge，如果一直merge，随机访问IO次数不会减少，反而增加了change buffer的维护代价。
3. 更新角度，更新是有差别的，涉及到change buffer。
    1. 唯一索引：
        1. 更新操作需要判断这个操作是否违反唯一约束，所以必须读数据页并判断，没必要用change buffer。
    2. 普通索引：
        1. 如果要更新的记录在内存中，找到对应位置，更新数据，结束。
        2. 如果要更新的记录不在内存中，直接在change buffer上更新记录，结束。
4. change buffer 和 redo log
    1. 一个更新操作的执行流程。
        ```text
       insert into t(id,k) values(id1,k1),(id2,k2);
       假设要执行这个插入语句。k1的数据页在内存中，K2的数据页不在内存中。
       * k1部分：直接更新内存中k1的数据，然后把动作记入redo log。（后台会把内存中的数据更新到表）
       * k2部分：直接把k2的操作写入change buffer，然后把动作记入redo log。（后台自动持久化change buffer）
       
       select * from t where k in (k1, k2);
       假设这个时候（更新操作刚刚完成）要执行这个语句。
       * k1部分：直接从内存中返回k1的数据。
       * k2部分：把k2的数据页从磁盘读入内存，应用change buffer中的k2的操作日志，生成一个正确的版本并返回。
        ```
    2. redo log 主要节省的是随机写磁盘的IO消耗（转成顺序写）。
    3. change buffer主要节省的则是随机读磁盘的IO消耗。

## 10，MySQL为什么有时候会选错索引？

不太感兴趣，而且感觉不是很实用，没怎么看

## 11，怎么给字符串字段加索引？

1. 前缀索引。
    1. 优势：占用的空间会更小。
    2. 劣势：可能会增加额外的记录扫描次数（额外次数包括回表，在第三点可以看到）。
    3. 使用前缀索引，定义好长度，就可以做到既节省空间，又不用额外增加太多的查询成本。
2. 索引区分度。
    1. 区分度越高越好。因为区分度越高，意味着重复的键值越少。
    2. 太高失去了意义，所以找合适的，大概90%-95%就可以。
    3. 索引区分度语句：select count(distinct left(key,5))/count(*) from table;
3. 前缀索引对覆盖索引的影响
    1. 如果查询的内容都在索引上，覆盖索引不用回表，前缀索引要回表。
4. 其他方式
    1. 倒序存储。
        >比方说身份证，把身份证倒过来存/读，区分度就足够了。  
        select * from t where id_card = reverse('input_id_card_string');  
        实践中，记得用count(distinct)方法去做个验证。
    2. hash字段。
        >可以在表上再创建一个整数字段，来保存身份证的hash码，同时在这个字段上创建索引。  
        alter table t add id_card_hash int unsigned, add index(id_card_hash);  
        实践中查询，除了判断hash码，还要判断id_card是否相同，因为hash还会冲突。
    3. 两种方式相同点：
        1. 不支持范围查询。
    4. 两种方式不同点：
        1. 存储空间方面，hash额外消耗一个字段。倒序要看前缀长度，如果长的话，也要消耗资源。
        2. cpu消耗方面，每次读写得时候，hash需要调用crc32()函数，倒序徐奥调用reverse()函数。
        3. 查询效率方面，hash性能稳定（hash冲突概率较小），倒序还是会部分重叠。

## 12，为什么我的MySQL会"抖"一下（因为刷脏页突然变慢）？

1. 可能是在刷脏页（flush）。
2. 刷脏页。
    1. innodb处理更新语句的时候，直接更新在内存里，然后只做了写日志（redo log）这一个磁盘操作。
    2. 所以内存中的数据页，可能跟磁盘数据页不一致，我们称这个内存页为脏页。
    3. 内存数据写入到磁盘后，内存和磁盘上的数据页的内容就一致了，称为干净页。
    4. 把内存里的数据写入磁盘的过程就叫做刷脏页。
3. 什么情况刷脏页。
    1. redo log写满了。这时候系统会停止所有更新操作，把脏页数据flush进磁盘数据页，然后把checkpoint往前推进，redo log留出空间可以继续写。
        >课件中有图和详细说明。
    2. 系统内存不足，需要读新的内存页，就要淘汰没用的老内存页（最久不使用的）。这时候如果被淘汰内存页是脏页，就要先将脏页写到磁盘。
        >为什么不直接淘汰脏页，下次请求的时候，从磁盘读入数据，拿redo log出来应用？  
        因为这种情况不利于查询。刷脏页的时候写磁盘，保证了读的时候只有以下两种情况，效率高。
        >* 数据存在于内存里，内存里就肯定是正确的结果，直接返回。
        >* 内存里没有数据，就可以肯定数据文件上是正确的结果，读入内存后返回。（不然不论有没有脏页情况，每次取出都得先跟redo log做对比，确认没有脏页，效率低）
    3. MySQL认为系统空闲的时候。
    4. MySQL正常关闭的时候。
4. 上述4种情况对性能的影响。
    1. 第一种情况应该尽量避免（敏感业务必须避免），因为这时候整个系统就不能再接受更新（严重影响性能）。
    2. 第二种情况最常见，影响看情况。如果一个查询要淘汰的脏页个数太多，会导致查询的响应时间明显变长。
    3. 第三，四种没影响。
    
    所以，innodb需要有控制脏页比例的机制，来尽量避免上面的这两种情况。
5. innodb刷脏页的控制策略。
    1. 设置innodb_io_capacity。这个参数可以告诉innodb所在主机的IO能力，innodb可以知道刷脏页可以多快。
        >建议设置成磁盘的IOPS，文章里有方法。
    2. **没看懂**
    3. 刷脏页的时候，如果邻居也是脏页，也会被刷掉，还会蔓延到下一个邻居。
        1. innodb_flush_neighbors这个参数是1的时候会带邻居，0则不带。
        2. 机械硬盘建议带，可以减少随机IO，ssd的IOPS高，不建议带邻居。
        3. MySQL8中默认是0。

## 13，为什么表数据删掉一半，表文件大小不变（数据库表的空间回收）？

1. 一个innodb表包含两部分：表结构定义和数据。
2. MySQL8以前，表结构是存在以.frm为后缀的文件里。MySQL8允许把表结构定义放在系统数据表中。
3. innodb_file_per_table参数。
    1. 这个参数设置为OFF的时候，表的数据放在系统共享表空间，跟数据字典放在一起。
    2. 这个参数设置为ON的时候，每个innodb表数据存储在一个以 .ibd为后缀的文件中。
    3. MySQL5.6.6开始，默认值是ON。
    4. 建议设置成ON，单独存储为一个文件便于管理。
        >是ON的时候，如果通过drop table删除表，文件也会被删除。如果放在共享表空间，即使表删了，空间也不会回收。
4. 数据删除流程（造成"空洞"）：
    >delete命令其实只是把记录的位置，或者数据页标记为了“可复用”，但磁盘文件的大小不变。
    1. 假设要删除记录R4，innodb只会把R4标记成删除，之后如果插入R4-1也在这个位置，可能会直接复用这个位置。磁盘文件大小并不会缩小。
    2. 假设要删除整个数据页上所有记录，那么整个数据页都可以被复用了。数据页复用更灵活。
        >数据页复用和记录复用略有区别，文档里有解释。
    3. 如果用delete命令把整个表的数据删除，所有的数据页都会被标记为可复用。磁盘上文件不会变小。
5. 随机插入数据，可能造成索引的数据页分裂，造成"空洞"。
6. 重新建表，可以去除增删改查导致的"空洞"。
    1. 可以使用`alter table A engine=InnoDB`命令重新建表。MySQL5.5之前，通过这个命令，MySQL自动帮你建表，转存数据，交换表名，删除旧表。
        >通过上面的方式建新表，整个DDL过程中A表（原表）不能插入新数据，否则有可能会丢失。
    2. MySQL5.6版本开始引入的Online DDL，对这个操作流程做了优化。
        1. 建立一个临时文件，扫描表A主键的所有数据页。
        2. 用数据页中表A的记录生成B+树，存储到临时文件中。
        3. 生成临时文件的过程中，将所有对A的操作记录在一个日志文件（row log）中。
        4. 临时文件生成后，将日志文件中的操作应用到临时文件，得到一个逻辑数据上与表A相同的数据文件。
        5. 用临时文件替换表A的数据文件。
        >整个DDL过程，一开始建表的时候，申请了一个MDL写锁，建表完成之后，迁移数据的时候，退化成MDL读锁。这样既能确保迁移的时候可以接受增删改查，又能防止其他线程同时对这个表做DDL操作。
    3. 上述两种方式，需要扫描原表数据和构建临时文件。在大表情况虾很消耗IO和CPU资源，需要小心控制操作时间。建议使用GitHub上开源的gh-ost来做。
7. Online和inplace。
    1. **没看懂**

## 14，count(*)这么慢，我该怎么办？

1. count(*)的实现方式（在没有过滤条件（where）的情况下）。
    1. MyISAM把一个表的总行数存在磁盘上，count直接返回，效率很高。
        >因为不支持事务。
    2. innodb需要把数据一行一行地从引擎里面读出来，然后累积计数。
        >因为支持事务，默认的隔离级别是可重复读。通过多版本并发控制（MVCC）实现。同一时刻每个会话看到的count都不一样，所以需要计算。
        
        >不过innodb优化了count(*)，MySQL优化器会找到最小的那棵树来遍历（因为每颗索引树上的节点数是一样的，无非数据长短不一样）。
2. `show table stat``us`命令的行数只是估算，不准确。
3. 其他方式：
    1. 用缓存系统保存计数（新增+1，删除-1）。
        1. 缓存可能会丢失。
        2. 不精确（逻辑不一致）。
            >数据库增加/减少数据和缓存中的count更新有间隔。
            
            >这两个不同的存储构成的系统，不支持分布式事务，无法拿到精确一致的视图。
    2. 在数据库保存计数。
        1. 还是会逻辑不一致。但是可以用事务解决。
4. 不同的count用法
    >以下按照速度快慢排序
    1. count(*):专门做了优化，不判空，不取值，直接按行累加。
    2. count(1):innodb引擎遍历整张表，但不取值。server层对于返回的每一行，放一个数字“1”进去，判断是不可能为空的，按行累加。
    3. count(id):innodb引擎会遍历整张表，把每一行的id值都取出来，返回给server层。server层拿到id后，判断是不可能为空的，就按行累加。
    4. count(字段):跟count(id)操作差不多，不过判断null的时候有点区别。如果是not null字段，按行累加行数。如果是可以为null的字段，要取出来判断是不是null，不是再累加行数。
    >实际上count(id)也可以优化成不用判断是否能为null，但是因为有count(*)可以用了，就懒得优化了，要用直接用count(*)就行了。


## 15，答疑文章（一）：日志和索引相关问题

### 日志相关问题

1. 两段式提交（redo log prepare--1-->写binlog--2-->redo log commit）,在1和2两个阶段MySQL异常，怎么保证数据完整性。
    1. 在1的阶段crash，则直接回滚。
    2. 在2的阶段crash，奔溃恢复的时候事务会被提交。
    >崩溃恢复判断规则
    >1. 如果redo log里面的事务是完整的，也就是已经有了commit标识，则直接提交。（上述没有这种情况）
    >2. 如果redo log里面的事务只有完整的prepare，则判断对应的事务binlog是否存在并完整：
    >   1. 如果是，则提交事务。
    >   2. 如果不是，则回滚。
    
    >上面"在2的阶段crash"就是2a的情况。
2. 追问1：MySQL怎么知道binlog是完整的?
    1. 一个事务的binlog是有完整格式的。
3. 追问2：redo log 和 binlog是怎么关联起来的?
    1. 它们有一个共同的数据字段，叫XID。
    2. 崩溃恢复的时候，会按顺序扫描redo log:
        1. 碰到既有prepare、又有commit的redo log，就直接提交。
        2. 碰到只有prepare、而没有commit的redo log，就拿着XID去binlog找对应的事务。
4. 追问3：MySQL为什么要设计成redo log prepare + binlog就能恢复数据？
    1. 因为binlog已经写入，所以从库可能已经有这个数据了，采用恢复策略是为了保证主从一致性。
5. 追问4：为什么要两阶段提交呢?干脆先redo log写完，再写binlog。崩溃恢复的时候，必须得两个日志都完整才可以。
    1. 两阶段提交是经典的分布式系统问题，并不是MySQL独有的。
    2. 如果redo log直接提交，那么就没法回滚。这时候如果binlog写入失败，就会造成redo log和binlog不一致。
    3. 两段式提交可以保证数据一致性。
6. 追问5：只用binlog来支持崩溃恢复，又能支持归档，不就可以了。为什么要引入两个日志？
    1. binlog不支持崩溃恢复。（原因帖子里有，比较复杂，这边不写了，而且没太看懂）
    2. 如果使用优化过的binlog，来记录数据页的更改，实现支持崩溃恢复，那其实就是做了一个redo log。
    3. 文章里没写，但是我觉得应该还有这个因素，redo log支持事务。
7. 追问6：那能不能反过来，只用redo log，不要binlog?
    >redo log好像是类似snapshot，binlog类似backup。不确定。
    1. 从crash-safe角度来看可以。（没看懂，不明白两个日志有啥区别）
    2. redo log起不到归档作用。
    3. MySQL高可用基础就是binlog。
8. redo log一般设置多大？
    1. redo log太小会导致很快写满，经常强行刷redo log。一般建议至少4个1g的文件。
9. 数据最终落盘，是redo log来的还是buffer pool。
    1. 刷脏页落盘跟redo log没关系。
    2. crash恢复时，innodb如果判断到一个数据页可能在崩溃恢复的时候丢失了更新，会先从磁盘读到内存，然后结合redo log更新内存内容。更新完后，内存页变脏页，等于回到上面一种状态。
10. redo log buffer是什么？是先修改内存，还是先写redo log文件？
    1. 一个事务的更新过程中，日志可能要写多次的。就先写在redo log buffer，commit的时候再写入redo log。

### 业务设计问题

```text
业务上有这样的需求，A、B两个用户，如果互相关注，则成为好友。
设计上是有两张表，一个是like表，一个是friend表。
like表有user_id、liker_id两个字段，我设置为复合唯一索引即uk_user_id_liker_id。
语句执行逻辑是这样的：

以A关注B为例：
第一步，先查询对方有没有关注自己（B有没有关注A）
select * from like where user_id = B and liker_id = A;

如果有，则成为好友
insert into friend;

没有，则只是单向关注关系
insert into like;

但是如果A、B同时关注对方，会出现不会成为好友的情况。
因为上面第1步，双方都没关注对方。第1步即使使用了排他锁也不行，因为记录不存在，行锁无法生效。
请问这种情况，在MySQL锁层面有没有办法处理？

这个题目的问题是，
在并发场景下，同时有两个人，设置为关注对方，就可能导致无法成功加为朋友关系。
因为判断的时候，查到对方没有like己方，所以各自插入一条like，没有插入friend。
```
1. 我个人理解:
    1. 先insert like。
    2. 再查有没有对面对我的like。
    3. 根据查询结果，判断要不要insert friend。
2. 文档里的方法：
    1. 首先，要给“like”表增加一个字段，比如叫作 relation_ship，并设为整型，取值1、2、3。
        1. 1表示user_id关注liker_id;
        2. 2表示liker_id关注user_id;
        3. 3表示互相关注;
    2. 然后，在A关注B发生的时候，先判断A和B的大小，小的放在user_id，大的放在like_id。
        >* 这样无论A关注B，还是B关注A，在库里的顺序都是一样的。所以当AB同时操作时，有一个操作会触发锁，用insert ... on duplicate语句就可以实现更新。
        >* 不过这样查询是不是得查两列？

## 16，“order by”是怎么工作的？

`select city,name,age from t where city='杭州' order by name limit 1000;`假设要执行这个语句。索引是`index city (city)`。

1. 全字段排序（用全部待返回字段排序）
    1. 初始化sort_buffer，确定放入name、city、age这三个字段。
    2. 从索引city找到第一个满足city='杭州’条件的主键id。
    3. 到主键id索引取出整行，取name、city、age三个字段的值，存入sort_buffer中。
    4. 从索引city取下一个记录的主键id。
    5. 重复步骤3、4直到city的值不满足查询条件为止。
    6. 对sort_buffer中的数据按照字段name做快速排序。
    7. 按照排序结果取前1000行返回给客户端。
    
    其中第六步的”按照name排序“，如果数据量太大/sort_buffer_size不够大，会用到磁盘或其他外部资源。（还会拆分文件，文章中有具体介绍，太复杂略）
    
    如果单行过大，也会影响数据量的大小。（很好理解，10*100行和100*199行明显不一样）
2. rowid排序  
    1. 初始化sort_buffer，确定放入两个字段，即name和id。
    2. 从索引city找到第一个满足city='杭州’条件的主键id。
    3. 到主键id索引取出整行，取name、id这两个字段，存入sort_buffer中。
    4. 从索引city取下一个记录的主键id。
    5. 重复步骤3、4直到不满足city='杭州’条件为止。
    6. 对sort_buffer中的数据按照字段name进行排序。
    7. 遍历排序结果，取前1000行，并按照id的值回到原表中取出city、name和age三个字段返回。
    
    `max_length_for_sort_data`修改这个参数，可以只用要排序的列（这里是name）和主键id排序。
    
    对比全字段排序流程你会发现，rowid排序多访问了一次表t的主键索引，就是步骤7。
3. 全字段排序 VS rowid排序
    1. MySQL的设计思想：尽量多利用内存，尽量减少磁盘访问。
    2. 全字段排序需要更多内存（如果内存不够，会借用磁盘），但是少回表一次。
    3. rowid排序需要内存更小，排序数量更多，但是需要一次多余的回表（造成多余的磁盘读）。
4. 优化方法
    1. MySQL排序成本高，可以使用有序的数据（联合索引）。上面的例子中，如果表中的索引不是`index city (city)`而是`index city_name(city, name)`，那么获取到的数据就是有序的，整个执行流程就会改变。
        1. 从索引(city,name)找到第一个满足city='杭州’条件的主键id。
        2. 到主键id索引取出整行，取name、city、age三个字段的值，作为结果集的一部分直接返回。
        3. 从索引(city,name)取下一个记录主键id。
        4. 重复步骤2、3，直到查到第1000条记录，或者是不满足city='杭州’条件时循环结束。
        
        这种场景下，不需要临时表，不需要排序，也不需要额外的回表。
    2. 使用覆盖索引，可以比上面的方法少一次回表。把索引改成`index city_user_age(city, name, age)`，新的流程如下。
        1. 从索引(city,name,age)找到第一个满足city='杭州’条件的记录，取出其中的city、name和age这三个字段的值，作为结果集的一部分直接返回。
        2. 从索引(city,name,age)取下一个记录，同样取出这三个字段的值，作为结果集的一部分直接返回。
        3. 重复执行步骤2，直到查到第1000条记录，或者是不满足city='杭州’条件时循环结束。
        
        这种场景，比上面的方法少一次回表。不过这种场景也不是必要的，因为索引还是有维护代价的。这是一个需要权衡的决定。
5. 课后习题
    1. 问题：假设你的表里面已经有了city_name(city, name)这个联合索引，然后你要查杭州和苏州两个城市中所有的市民的姓名，并且按名字排序，显示前100条记录。如果SQL查询语句是这么写的：`select * from t where city in ('杭州',"苏州") order by name limit 100;` 
        1. 这个语句执行的时候会有排序过程吗？
        2. 需要实现一个在数据库端不需要排序的方案，怎么实现呢？
        3. 如果有分页需求，要显示第101页，也就是说语句最后要改成 “limit 10000,100”，怎么实现呢？
    2. 答案
        1. 有排序过程，因为索引上city-name有序，但是name是无序的，很好理解。
        2. 两个方案：
            1. `select * from t where city = '杭州' limit 100;` + `select * from t where city = '苏州' limit 100;`，然后java内存排序。
            2. 直接`select * from ( select * from t where city = '杭州' limit 100 union all select * from t where city = '苏州' limit 100 ) as tt order by name limit 100`，跟第一种原理其实一样，就是java排序和MySQL排序。
        3. 两个方案：
            1. 为了意义不大的功能优化，可能会得不偿失。建议砍掉这个不实用的功能。
            2. `select id,name from t where city="杭州" order by name limit 10100;` + `select id,name from t where city="苏州" order by name limit 10100。`，然后java内存排序name，获取对应id，再用`where id in()`来查。
                
                这个答案让人失望，其实本质上跟上面的分页取前100没啥区别（以为会有其他骚操作），无非数据量多了不好处理，多了个id回表，很好理解。
            
## 17，如何正确地显示随机消息？（MySQL中随机取几个值）

感觉不是很重要，没仔细看，略

## 18，为什么这些SQL语句逻辑相同，性能却差异巨大？（对索引字段做函数操作的三个案例）

1. 条件字段函数操作

    `select count(*) from tradelog where month(t_modified)=7;`这个语句，t_modified上有索引，但不会走**树搜索功能**。
    1. 对索引字段做函数操作，可能会破坏索引值的有序性，因此优化器就决定放弃走**树搜索功能**。
        >即使`select * from tradelog where id + 1 = 10000`这种不影响有序性的，也不会走**树搜索功能**。
    2. 虽然不走**树搜索功能**，但还是会走索引的，这里优化器会选择`遍历主键索引`或者`遍历t_modified索引`，因为t_modified索引更小，所以会选择后者。
    3. 又因为count直接可以在索引上完成，所以这里会走覆盖索引，不回表。
    4. 应该改成类似`select count(*) from tradelog where (t_modified>='2016-07-01' and t_modified<='2016-08-01') or (t_modified>='2017-07-01' and t_modified<='2017-08-01');`这种语句，就可以走**树搜索功能**。
2. 隐式类型转换

    再来看`select * from tradelog where tradeid=110717;`这个语句，tradeid有索引，但是查询走全表扫描。因为tradeid在表里的类型是varchar(32)，可查询输入的是整型，需要做类型转换。
    1. 在MySQL中，字符串和数字做比较的话，是将字符串转换成数字。
    2. 根据上面（2-i）的理论，上面那个例子等于`select * from tradelog where CAST(tradid AS signed int) = 110717;`，这里中了本章第1点的《条件字段函数操作》，放弃走**树搜索功能**。
    3. 根据上面（2-i）的理论，`select * from tradelog where id="83126";`等于`select * from tradelog where id=83126;`，可以走**树搜索功能**。
3. 隐式字符编码转换
    
    这次在老表`tradelog`（utf8mb4）的基础上，又增加了新表`trade_detail`（utf8）。其中id，tradeid啥的都有索引。
    
    新的语句如下`select d.* from tradelog l, trade_detail d where d.tradeid=l.tradeid and l.id=2;`，这个查询也有问题。
    1. explain的结果如下：
        1. 第一行显示优化器会先在交易记录表tradelog上查到id=2的行，这个步骤用上了主键索引，rows=1表示只扫描一行。
        2. 第二行key=NULL，表示没有用上交易详情表trade_detail上的tradeid索引，进行了全表扫描。
    2. 为什么3-i-b进性全表扫描呢
        1. 首先，我们把第二行单独改成SQL，就是`select * from trade_detail where tradeid=$L2.tradeid.value;`。
        2. 我们再来看，`tradelog`是utf8mb4的，`trade_detail`是utf8的，字符集utf8mb4是utf8的超集。这里又因为本章第2点的《隐式类型转换》，他们对比的时候，MySQL先把utf8转化成utf8mb4。
        3. 转换完的语句如下`select * from trade_detail where CONVERT(traideid USING utf8mb4)=$L2.tradeid.value;`，很明显，又中了本章第1点的《条件字段函数操作》，所以就走全表扫描了。
        4. 作为对比，`select l.operator from tradelog l, trade_detail d where d.tradeid=l.tradeid and d.id=4;`，这个语句就可以正确走索引和树搜索功能。
            
            这里的语句第一步是找对应的trade_detail，按照utf8->utf8mb4的转换的规则，第二步可以被写成`select operator from tradelog where traideid =CONVERT($R4.tradeid.value USING utf8mb4);`，参数上的函数，影响查询。
        5. 原语句的优化有两种方案
            1. 直接把`trade_detail`改成utf8mb4。
            2. `select d.* from tradelog l , trade_detail d where d.tradeid=CONVERT(l.tradeid USING utf8) and l.id=2;`
4. 课后习题
    
    挺有意思，不过太复杂，不写了。
    
## 19，为什么我只查一行的语句，也执行这么慢？

1. 查询长时间不返回
    1. 等MDL锁
        1. 找到谁持有MDL写锁，然后把它kill掉。
            >文章里讲了怎么找。
    2. 等flush
        1. flush通常很快，有可能是被别的线程堵住了。可以用的show processlist，找到对应的请求，kill掉。
    3. 等行锁
        1. 找到迟迟不释放锁的线程，kill连接（断开自动释放锁）。
2. 查询慢
    1. 主键索引全表扫描的查询
        1. 建议`set long_query_time=0;`把所有语句记录到slow log里。通过慢查询日志，发现全表扫描的查询，优化索引。
    2. `select * from t where id=1;`
        
        `select * from t where id=1 lock in share mode;`
        
        上面这个查询用了800ms，下面这个只用了1ms。因为这时候边上有另一个线程把id=1这一行更新了100w次（假设）。`in share mode`是当前读，直接返回数值。而一致性视图读，需要把当前结果结合100w个undo log做运算，所以就很慢。

3. 课后习题
    1. 问题：`begin;select * from t where c=5 for update;commit;`，这个语句怎么加锁，怎么释放。
    2. 答案：RC隔离级别下，对全表记录做行锁（写锁）。RR隔离级别下，锁全表记录+GAP。具体细节20章会讲到。

## 20，幻读是什么，幻读有什么问题？（涉及gap锁）

1. 幻读的定义：一个事务在前后两次查询同一个范围的时候，后一次查询看到了前一次查询没有看到的行。
2. 幻读的问题：会造成数据不一致。
3. 幻读的解决办法：
    1. innodb引入了间隙锁（Gap Lock）。
        1. 间隙锁会与"往这个间隙中插入一个记录"这个操作冲突。
        2. 间隙锁与间隙锁之间不会冲突。
        3. 间隙锁是开区间。
        4. 间隙锁和行锁，合称next-key lock。每个next-key lock是前开后闭区间。
4. 间隙锁的问题和解决：
    1. 可能会导致同样的语句锁住更大的范围，这其实是影响了并发度。
        >文中的例子是，两个session都用间隙锁锁住了一个区间，间隙锁不冲突。但是他们随后都往这个区间插入一条数据，都被对方的间隙锁锁住了，所以就死锁了。
    2. innodb中，可重复读级别下，才有间隙锁。
        1. 把隔离级别设置为读提交，就没有间隙锁了。但是，你要解决可能出现的数据和日志不一致问题，需要把binlog格式设置为row。
5. 课后习题
    1. 问题：间隙锁和insert语句的冲突关系（分析怎么加间隙锁）。具体太长了，看原文吧。
    2. 答案：

## 21，为什么我只改一行的语句，锁这么多？（涉及加锁规则）

>本问加锁规则，只限于5.x系列<=5.7.24，8.0系列 <=8.0.13。后面的MySQL版本可能会改。
1. 规则：
    1. 原则1：加锁的基本单位是next-key lock。希望你还记得，next-key lock是前开后闭区间。
    2. 原则2：查找过程中访问到的对象才会加锁。
        >在`lock in share mode`的时候，覆盖索引这种没有回表的操作，就不会锁表，只锁索引。
        >> `for update`模式下，系统认为你要更新数据，会顺便给主键索引上满足条件的行加上行锁。
    3. 优化1：索引上的等值查询，给唯一索引加锁的时候，next-key lock退化为行锁。
    4. 优化2：索引上的等值查询，向右遍历时且最后一个值不满足等值条件的时候，next-key lock退化为间隙锁。
    5. 一个bug：唯一索引上的范围查询会访问到不满足条件的第一个值为止。
2. 案例
    1. 等值查询间隙锁。
    2. 非唯一索引等值锁（命中覆盖索引）。
    3. 主键索引范围锁。
    4. 非唯一索引范围锁。
    5. 唯一索引范围锁bug。
    6. 非唯一索引上存在"等值"的例子。
    7. limit 语句加锁。
    8. next-key lock（加锁分成两个阶段）的例子。

## 22，MySQL有哪些“饮鸩止渴”提高性能的方法？

1. 连接问题。
    1. 先处理掉那些占着连接但是不工作的线程。
    2. 减少连接过程的消耗。（比如全限验证）
2. 慢查询性能问题
    1. 索引没有设计好。
        1. 直接加索引，切换主从库。
    2. SQL语句没写好。
        1. 改写SQL语句。（query_rewrite功能）
    3. MySQL选错了索引。
        1. 把慢查询日志（slow log）打开。
        2. 慢查询语句加上force index。
3. QPS突增问题
    1. 如果是bug引起。下掉这个功能。
        1. 如果白名单做得很规范，可以从白名单移除。
        2. 如果新功能使用的是单独的数据库用户，可以删除用户并断开现有连接。
        3. 或者可以用重写功能为类似`select 1`的语句。（query_rewrite功能）
            1. 缺点1：如果别的功能里面也用到了这个SQL语句模板，会有误伤；
            2. 缺点2：如果单独把这一个语句以`select 1`的结果返回的话，可能会导致后面的业务逻辑一起失败。

## 23，MySQL是怎么保证数据不丢的？

1. binlog的写入机制。
    1. 事务执行过程中，先把日志写到binlog cache。事务提交时，再把binlog cache写到binlog文件。并清空binlog cache。
    2. 一个事务的binlog是不能被拆开的，因此不论这个事务多大，也要确保一次性写入。
    3. 参数`binlog_cache_size`用于控制单个线程内binlog cache所占内存的大小。如果超过了这个参数规定的大小，就要暂存到磁盘。
        >每个线程一个binlog cache，共用binlog文件。
    4. 提交的时候，先把日志写入到文件系统的page cache，并没有把数据持久化到磁盘，所以速度比较快。
    5. fsync阶段，才是将数据持久化到磁盘的操作。一般情况下，我们认为fsync才占磁盘的IOPS。
    6. write和fsync的时机，由参数`sync_binlog`控制：
        1. `sync_binlog=0`的时候，表示每次提交事务都只write，不fsync。
        2. `sync_binlog=1`的时候，表示每次提交事务都会执行fsync。
        3. `sync_binlog=N(N>1)`的时候，表示每次提交事务都write，但累积N个事务后才fsync。
        >* 实际业务中，考虑到丢失日志的可能性，一般不设置成0。
        >* 在出现IO瓶颈的场景里，将sync_binlog设置成一个比较大的值，可以提升性能。  
        >* 将`sync_binlog`设置为N，对应的风险是：如果主机发生异常重启，会丢失最近N个事务的binlog日志。
2. redo log的写入机制。
    1. redo log有三种状态：
        1. 存在redo log buffer中，物理上是在MySQL进程内存中。
        2. 写到磁盘(write)，但是没有持久化（fsync)，物理上是在文件系统的page cache里面。
        3. 持久化到磁盘，对应的是hard disk。
    2. 日志写到redo log buffer是很快的，write到page cache也差不多，但是持久化到磁盘的速度就慢多了。
    3. 为了控制redo log的写入策略，innodb提供了`innodb_flush_log_at_trx_commit`参数：
        1. 0 - 每次事务提交时都只是把redo log留在redo log buffer中。
        2. 1 - 每次事务提交时都将redo log直接持久化到磁盘。
        3. 2 - 每次事务提交时都只是把redo log写到page cache。
    4. innodb有一个后台线程，每隔1秒，会把redo log buffer的日志，调用write写到page cache，然后调用fsync持久化到磁盘。
        >事务执行中间过程的redo log也是直接写在redo log buffer中的，也会被后台线程一起持久化到磁盘。（一个没有提交的事务的redo log，也是可能已经持久化到磁盘的。）
    5. 即使没提交，redo log buffer写了`innodb_log_buffer_size`一半的时候，会触发write，但是不触发fsync，数据在page cache。
    6. 即使没提交，如果`innodb_flush_log_at_trx_commit=1`，事务B提交的时候，会把事务A写在buffer的内容顺便持久化到磁盘。
    7. 如果`innodb_flush_log_at_trx_commit=1`，redo log在prepare的时候就会持久化。然后commit的时候就不需要fsync了，指挥write到page cache。
        >我觉得应该是数据已经持久化了，标签是prepare，然后write到page cache里面的是把标签改成commit，不需要立即磁盘化，每秒一次的后台线程会持久化这个标签。个人理解。
3. 组提交（group commit）
    1. 解决TPS过高的情况。（按照一次提交redo log和binlog都刷盘来看，一次提交2次刷盘，那么20000的TPS就要刷盘40000次，组提交可以大大减少）
        >其实我觉得就跟顺带提交差不多。具体细节见文章。
    2. 为了让组提交效果更好，两段式提交`redolog prepare->写binlog->commit`的实现实际上是`redolog prepare write->binlog write->redolog prepare fsync->binlog fsync->redolog commit write`这样的。
        >在这种情况下，如果多个事务的binlog写完了，binlog实际上也是一起持久化了，见少IOPS消耗。
    3. `binlog_group_commit_sync_delay`表示延迟多少微秒后才调用fsync。
    4. `binlog_group_commit_sync_no_delay_count`表示累积多少次以后才调用fsync。
    5. 这两个条件是或的关系，只要有一个满足条件就会调用fsync。所以`delay`设置为0的时候，`count`就无所谓了。
4. WAL机制主要得益于两个方面
    1. redo log 和 binlog都是顺序写，磁盘的顺序写比随机写速度要快。
    2. 组提交机制，可以大幅度降低磁盘的IOPS消耗。
5. 如果MySQL出现了性能瓶颈，而且瓶颈在IO，可以通过哪些方法来提升性能呢？
    1. 设置`binlog_group_commit_sync_delay`和`binlog_group_commit_sync_no_delay_count`，减少binlog写盘次数。缺点：增加语句相应时间。
    2. 设置`sync_binlog`为大于1的值（通常100-1000）。缺点：停电丢binlog日志。
    3. 设置`innodb_flush_log_at_trx_commit`为2。缺点：停电丢redo log数据。
    * 不建议`innodb_flush_log_at_trx_commit`设置成0，风险太大，设置程2性能差不多，风险小很多。

## 24，MySQL是怎么保证主备一致的？(介绍binlog)

1. 主从备份流程。
    1. 主库与备库建立长连接，启动dump_thread专门服务备库。
    2. 备库启动io_thread和sql_thread，io_thread与主库连接。
    3. 主库校验用户名密码后，读本地binlog，发给备库。
    4. 备库拿到binlog，写到本地，叫做中转日志（relay log）。
    5. 备库sql_thread读取中转日志，解析命令，执行。
2. binlog的三种格式对比。
    1. statement：
        1. 特点：记录了真实执行的语句。
        2. 优点：节约空件。
        3. 缺点：有时候不够精确，导致主备不一致(比如带条件和limit的delete语句）。
    2. row：
        1. 特点：没有原文，换成了各种event。
        2. 优点：比statement更精确（可以用于数据恢复）。
        3. 缺点：占空间，耗IO。
    3. mixed：
        1. 特点：MySQL自动判断语句是否会导致主备不一致，会的话用row，不会则用statement。
        2. 优点：灵活，节约空件。
        3. 缺点：不能用于数据恢复。
    >建议至少设置成mixed，最好row。
3. 恢复数据。
    1. row格式下：delete的binlog会记录整行信息，恢复直接把delete改成insert就行了。
    2. row格式下：insert的binlog会记录所有字段信息，可以精确定位插入的那一行，直接delete就行了。
    3. row格式下：update的binlog会记录修改前后的两行数据的信息，直接对调信息再update就行了。
    >注意，就算是statement模式，也不能直接复制binlog的语句执行，因为类似current_time之类的依赖于其他上下文的数据会有影响。对于这些数据，binlog中也有记录，要用工具解析之后再执行。
4. 循环复制问题（互为主备的模式）。
    1. 面临的问题。
        1. A的binlog发给B，B执行完后生成自己的binlog发给A，A又继续执行，如此循环。
    2. 解决方法。
        1. 两个库的server id必须不同。
        2. 备库接到binlog并在重放的过程中，生成与原binlog的server id相同的新的binlog。
        3. 每个库收到主库发过来的日志后，先判断server id。如果跟自己的相同，表示这个日志是自己生成的，直接丢弃这个日志。
5. 

## 25，MySQL是怎么保证高可用的？（主备切换，主备延迟）

1. 主备切换
    1. 主库A执行完成一个事务，写入binlog，我们把这个时刻记为T1。
    2. 之后传给备库B，我们把备库B接收完这个binlog的时刻记为T2。
    3. 备库B执行完成这个事务，我们把这个时刻记为T3。
2. 主备延迟
    1. 就是同一个事务，在备库执行完成的时间和主库执行完成的时间之间的差值，也就是T3-T1。
    2. 在备库上执行`show slave status`，返回结果的`seconds_behind_master`表示当前备库延迟了多少秒。
    3. 主备延迟最直接的表现是，备库消费中转日志（relay log）的速度，比主库生产binlog的速度要慢。
    4. 
3. 主备延迟的来源
    1. 备库所在机器的性能要比主库所在的机器性能差。
    2. 备库的压力大。（主库负责写，备库读，所以压力大）
        1. 解决办法1：一主多从。
        2. 解决办法1：通过binlog输出到外部系统，比如Hadoop这类系统，让外部系统提供统计类查询的能力。
    3. 大事务。
        1. 例子1：一次性删除过多数据。
        2. 例子2：大表DDL。
    4. 备库的并行复制能力。（下一章介绍）
4. 主备切换策略。
    1. 可靠性优先策略。（双M结构下）
        1. 判断备库B现在的`seconds_behind_master`，如果小于某个值（比如5秒）继续下一步。
        2. 把主库A改成只读状态，即把readonly设置为true。
        3. 判断备库B的`seconds_behind_master`的值，直到这个值变成0。
        4. 把备库B改成可读写状态，也就是把readonly 设置为false。
        5. 把业务请求切到备库B。
    2. 可用性优先策略。
        1. 把主库A改成只读状态，即把readonly设置为true。
        2. 把备库B改成可读写状态，也就是把readonly 设置为false。
        3. 把业务请求切到备库B。
    3. 使用可用性优先策略时，row格式的binlog梗好没数据不一致更容易被发现。
    4. 可用性优先策略可能会导致数据不一致，大多数情况建议用可靠性优先策略。可靠性一般比可用性更重要。

## 26，备库为什么会延迟好几个小时？（备库并行复制能力）

1. 如果主库并发高、TPS高，而备库只支持单线程复制，就会出现严重的主备延迟问题。
2. 5.6版本之前，MySQL只支持单线程复制。
3. 多线程任务派发基本要求。
    1. 不能造成更新覆盖。这就要求更新同一行的两个事务，必须被分发到同一个worker中。
    2. 同一个事务不能被拆开，必须放到同一个worker中。
4. MySQL 5.5版本的并行复制策略（楼主自治的两种）。
    1. 按表分发策略。
        1. 原理：
            1. 如果两个事务更新不同的表，可以保证不会更新同一行，它们就可以分给两个worker并行。
            2. 每个更新操作的唯一标识是“库名.表名”的hash值。用这个值判断是否更新同一个表。
        2. 操作：
            1. 如果事务跟所有worker都不冲突，coordinator线程就会把这个事务分配给最空闲的worker。
            2. 如果事务跟多于一个worker冲突，coordinator线程就进入等待状态，直到和这个事务存在冲突关系的worker只剩下1个。
            3. 如果事务只跟一个worker冲突，coordinator线程就会把这个事务分配给这个存在冲突关系的worker。
        3. 缺点
            1. 热点表只能单线程，还是很慢。
    2. 按行分发策略。
        1. 原理：
            1. 如果两个事务没有更新相同的行，它们在备库上可以并行执行。
            2. 每个更新操作的唯一标识是“库名+表名+唯一键的值（不止pk，还包括唯一键）”的hash值。
                ```text
               比方
               update t1 set a=1 where id=2
               算hash的时候，就要算
               hash(库名+表明+id2)
               hash(库名+表明+a1)
               hash(库名+表明+a2)
               这三个hash
               
               这样如果有其他事务在处理a=2的行，或者a=1的行的时候，就不会冲突了。
               （例如update XX set a=xx where a=2，也会定位到这一行）
                ```
        2. 操作
            1. 基本跟“表分发策略”差不多，就是计算hash的时候要多算几个。
        3. 缺点：
            1. 耗费内存。（比方删除语句涉及100w数据，需要100w个hash值，按表分只需要1个）
            2. 耗费cpu。（解析binlog，算hash，成本高很多）
            3. 可以设置阈值，如果来了一个超大事务，临时把并行模式变成单线程模式，超大事务结束再重新开启并行模式。
    3. 这两个方案其实都有一些约束条件。
        1. 要能从binlog里面解析出表名、主键和唯一索引。也就是说，主库的binlog格式必须是row。
        2. 表必须有主键。
        3. 不能有外键。
        不过， 这三条约束，本来就是DBA之前要求业务开发人员必须遵守的线上使用规范，所以关系不大。
5. MySQL 5.6版本的并行复制策略。
    1. 按库（DB）并行。
        1. 优点：
            1. 构造hash值的时候很快，只需要库名。DB不会很多，消耗资源小。
            2. 不要求binlog的格式。因为statement格式的binlog也可以很容易拿到库名。
        2. 缺点：
            1. 并发度不够。如果所有逻辑表放在一个库，等于单线程。
6. MariaDB的并行复制策略。
    1. 原理：
        1. 能够在同一组里提交的事务，一定不会修改同一行。
        2. 主库上可以并行执行的事务，备库上也一定是可以并行。
    2. 操作：
        1. 在一组里面一起提交的事务，有一个相同的commit_id，下一组就是commit_id+1
        2. commit_id直接写到binlog里面。
        3. 传到备库应用的时候，相同commit_id的事务分发到多个worker执行。
        4. 这一组全部执行完成后，coordinator再去取下一批。
    3. 优点：
        1. 系统改造少。
        2. 实现优雅。
    4. 缺点：
        1. 吞吐量不够。无法真正模拟主库并发。比方主库事务123一起在提交的时候，456处于执行状态，123提交之后456很快可以进入提交。从库只能并发提交123，结束之后，再让456处于执行，再进入提交。
        2. 容易被大事务影响。
7. MySQL 5.7的并行复制策略。
    1. slave-parallel-type=DATABASE，使用MySQL5.6的按库并行策略。
    2. slave-parallel-type=LOGICAL_CLOCK，使用类似MariaDB的策略。比之前的版本升级了。
        1. 原理：
            1. 实际上，只要能够到达redo log prepare阶段，就表示事务已经通过锁冲突检验，就可以并行。
            2. 同时处于prepare状态的事务，在备库执行时可以并行。
            3. 处于prepare状态的事务，与处于commit状态的事务之间，在备库执行时也可以并行。
        2. 操作：
            1. 通过binlog的`binlog_group_commit_sync_delay`和`binlog_group_commit_sync_no_delay_count`参数，减少主库binlog写盘字数，可以制造更多“同时处于prepare阶段的事务”，有利于备库并行提交。
7. MySQL 5.7.22的并行复制策略。
    1. 增加了一个新的并行复制策略，基于WRITESET的并行复制。
    2. 新增了一个参数`binlog-transaction-dependency-tracking`，用来控制是否启用这个新策略。这个参数的可选值有以下三种。
        1. COMMIT_ORDER：根据同时进入prepare和commit来判断是否可以并行的策略。
        2. WRITESET：对于事务涉及更新的每一行，计算出这一行的hash值，组成集合writeset。如果两个事务没有操作相同的行，也就是说它们的writeset没有交集，就可以并行。
        3. WRITESET_SESSION：在WRITESET的基础上多了一个约束，即在主库上同一个线程先后执行的两个事务，在备库执行的时候，要保证相同的先后顺序。
            >为了唯一标识，这个hash值是通过“库名+表名+索引名+值”计算出来的。如果表上有唯一索引，那么对于每个唯一索引，insert语句对应的writeset就要多增加一个hash值。
    3. 这个策略，跟作者在MySQL5.5自己做的按行分发策略差不多，不过官方版本有很多优势。
        1. writeset是在主库生成后直接写入到binlog里面的，这样在备库执行的时候，不需要解析binlog内容（event里的行数据），节省了很多计算量。
        2. 不需要把整个事务的binlog都扫一遍才能决定分发到哪个worker，更省内存。
        3. 由于备库的分发策略不依赖于binlog内容，所以binlog是statement格式也是可以的。

## 27，主库出问题了，从库怎么办？（一主多从切换）

1. 基于位点的主备切换。
    1. 执行change master命令的时候，需要`MASTER_LOG_FILE`和`MASTER_LOG_POS`这两个参数来确定同步文件和日志偏移量。
2. 
3. 
4. 
5. 

## 28，读写分离有哪些坑？（怎么处理主备延迟导致的问题）

1. 
2. 
3. 
4. 








## 引用
>[深挖计算机基础：MySQL实战45讲学习笔记](https://www.cnblogs.com/luoahong/p/11792027.html)
>这个人转载之后还截取了一些精彩留言，想看可以看一点