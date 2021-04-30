#[SQL 优化极简法则，还有谁不会？](https://mp.weixin.qq.com/s?__biz=MzAxNjk4ODE4OQ==&mid=2247494887&idx=3&sn=f521a3e6b9addec2ab2c006fc18592c5&chksm=9beecd95ac994483e3f83baa79c389ddeec05662d664a0b83063bd1c72791529e599e3e0c3ba&scene=126&sessionid=1605066663&key=860282314ab71371952c227f7ac036895a2e768bbd85481f5df896bab8c1c0e362b119d227017994ac35bc72c3310bccd04dc1f1ad7f3a07b2fa9f59f59c2b4fd6ad0444ab0f903df7f9ac7a1fb3d947d1cb48eebb0d2b0c382529ba73b5c9c971e3393b7f469875bce26d39af5b04d0fdadd5b632e3e151af4bb196a640ff38&ascene=1&uin=MjgwMjUxMjM1&devicetype=Windows+10+x64&version=6300002f&lang=zh_CN&exportkey=Aq9EFvNa6vJm2a2Orx%2FI2l8%3D&pass_ticket=OXXUXTiyz6HRc8uHyA9nVtzh4ILR8YbM5iKJCvQ9AtNyXsggt78edP7rFfGCx3u6&wx_header=0)

## 法则一：只返回需要的结果

1. 节省带宽。
2. 如果走覆盖索引，就不用回表了，搜*需要回表。
3. 如果前端只需要10条数据，不要返回全部数据（例如10000条），节约带宽。

## 法则二：确保查询使用了正确的索引

1. 用explain查看是不是走了索引。（<>，not in，like "%"之类的操作不走索引），如果没有，可以使用USE INDEX强制指定。
2. where之后的查询条件不要用函数，例如 where a+1 = 100，这种不走索引。还有隐式转换，也是函数的意思。
3. 使用联合索引时，注意最左前缀。
4. 先筛选再排序，能where别having。
5. 索引区分度。
6. 避免回表。
   1. 有一些请求例如limit 1000000,10，可能要回表1000000次，然后查到对应的10条数据。可以先查出对应的id，然后再用id去主表查询（使用join），这样能减少回表次数。
7. order上最好也要有索引。
8. 太长的数据，用前缀索引。
9. 联合索引太长，可以适当缩短，比方联合索引（a,b,c）能把数据缩到1000条，(a,b,c,d)能缩到100条，其实d加进来意义不大。
10. 用慢查询去找到有问题的sql语句，进行优化。
11. 只要一条数据的时候用limit 1。（我觉得用处不是很大）
12. 用union带头or。（没研究过，感觉没啥用）
    >select id from t where num=10 union all select id from t where num=20
13. 用exists代替in。（这个不太懂，有空研究下）
    > select num from a where num in(select num from b)。
    > 用下面的语句替换：select num from a where exists(select 1 from b where num=a.num)。

## 法则三：尽量避免使用子查询

## 法则四：不要使用 OFFSET 实现分页

## 法则五：了解 SQL 子句的逻辑执行顺序

补充一下，避免大事务（例如insert xxxx select xxxx）。
1. 建表最好不要有null列。
2. 索引数量不要太多，会影响insert和update，还会影响加锁。
3. 能用int就不用varchar，据说性能高。
4. 用长度可变的varchar代替char。
5. 当有一批处理的插入或更新时，用批量插入或批量更新，绝不会一条条记录的去更新。
   >这个跟大事务好像不太一样，逐条插入，好像跟连接池什么有关，很慢，批量快很多。但是批量数量太多，又有大事务问题。
6. 事务应经可能地缩短，在一个事务中应尽可能减少涉及到的数据量；永远不要在事务中等待用户输入。
7. 要有自增主键，insert速度快，数据页整齐。

## 引用
>[SQL 优化极简法则，还有谁不会？](https://mp.weixin.qq.com/s?__biz=MzAxNjk4ODE4OQ==&mid=2247494887&idx=3&sn=f521a3e6b9addec2ab2c006fc18592c5&chksm=9beecd95ac994483e3f83baa79c389ddeec05662d664a0b83063bd1c72791529e599e3e0c3ba&scene=126&sessionid=1605066663&key=860282314ab71371952c227f7ac036895a2e768bbd85481f5df896bab8c1c0e362b119d227017994ac35bc72c3310bccd04dc1f1ad7f3a07b2fa9f59f59c2b4fd6ad0444ab0f903df7f9ac7a1fb3d947d1cb48eebb0d2b0c382529ba73b5c9c971e3393b7f469875bce26d39af5b04d0fdadd5b632e3e151af4bb196a640ff38&ascene=1&uin=MjgwMjUxMjM1&devicetype=Windows+10+x64&version=6300002f&lang=zh_CN&exportkey=Aq9EFvNa6vJm2a2Orx%2FI2l8%3D&pass_ticket=OXXUXTiyz6HRc8uHyA9nVtzh4ILR8YbM5iKJCvQ9AtNyXsggt78edP7rFfGCx3u6&wx_header=0)