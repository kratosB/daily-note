- [深入浅出索引](#4深入浅出索引上)
- [深入浅出索引](#5深入浅出索引下)
- [深入浅出索引](#9普通索引和唯一索引应该怎么选择)
- [深入浅出索引](#11怎么给字符串字段加索引)
- [深入浅出索引](#16order-by是怎么工作的)
- [深入浅出索引](#18为什么这些sql语句逻辑相同性能却差异巨大对索引字段做函数操作的三个案例)
- [深入浅出索引](#19为什么我只查一行的语句也执行这么慢)

## 4，深入浅出索引（上）

1. Hash查询快，但是区间查询慢。
2. 有序数组查询快，新增慢。
3. 二叉树层数多，读磁盘多，速度慢，所以用**多叉树**。
4. 自增主键的好处:
    1. 有序，插入快。不会触发叶子节点的分裂。
    2. 二级索引节省空间。（因为二级索引等于是key-value结构，key是索引，value是主键，自增主键占用空间小）
5. 业务字段做主键：
    1. 只有一个索引。
    2. 索引必须是唯一索引。
    3. 最典型的就是K-V场景。
    
    
## 5，深入浅出索引（下）

1. 使用覆盖索引，避免回表，尤其，**范围查询要回表很多次**。
2. 最左前缀原则。
3. 索引下推:
   1. 比如select * from tuser where name like '张%' and age=10 and ismale=1;
   2. 先通过(name,age)的索引，找到'张%'匹配的地方。
   3. 然后MySQL5.6之前的版本，数据库会直接开始回表比对字段。
   4. 5.6之后的版本引入了索引下推优化。可以在索引遍历过程中，对索引中包含的字段先做判断，直接过滤掉不满足条件的记录（在这个例子中，就是先判断age=10，先把age!=10的数据排除），减少回表次数。


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
        >因为每次读都会触发merge，如果一直merge，随机访问IO次数不会减少，反而增加了change buffer的维护代价。
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

>在[线上MySQL慢查询现象案例--Impossible WHERE noticed after reading const tables](https://yq.aliyun.com/articles/393774) 中看到一种说法，在用唯一索引的时候，如果where XXX=“”这个值在表中不存在，会走全扫描。但是用普通索引，就没有这种情况。从这个角度看，也是普通索引好。


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




























