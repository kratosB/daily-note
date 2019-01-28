### 1. Mysql索引作域

MySQL只对<，<=，=，>，>=，BETWEEN，IN，以及某些时候的LIKE才会使用索引 
（NOT IN和<>不会使用索引，想想索引的原理基本也明白了）
    
    用like的情况下，
    SELECT * FROM mytable WHERE username like'admin%' 这种情况会用索引
    SELECT * FROM mytable WHEREt Name like'%admin' 这种情况下，不会用索引
    
### 2. 索引也会有它的缺点

1. 虽然索引大大提高了查询速度，同时却会降低更新表的速度，如对表进行INSERT、UPDATE和DELETE。
因为更新表时，MySQL不仅要保存数据，还要保存一下索引文件
2. 建立索引会占用磁盘空间的索引文件。一般情况这个问题不太严重，
但如果你在一个大表上创建了多种组合索引，索引文件的会膨胀很快。

### 3. 单列索引，多列索引

假设目前有这么一个查询语句
    
    SELECT ｀uid｀ FROM people WHERE lname｀='Liu'  AND ｀fname｀='Zhiqun' AND ｀age｀=26;
    
* 单列索引：

        ALTER TABLE people ADD INDEX lname (lname);

    将lname列建索引，这样就把范围限制在lname='Liu'的结果集1上，之后扫描结果集1，
产生满足fname='Zhiqun'的结果集2，再扫描结果集2，找到 age=26的结果集3，即最终结果。
由于建立了lname列的索引，与执行表的完全扫描相比，效率提高了很多，
但我们要求扫描的记录数量仍旧远远超过了实际所需要的。虽然我们可以删除lname列上的索引，
再创建fname或者age列的索引，但是，不论在哪个列上创建索引搜索效率仍旧相似。

* 多列索引：
        
        ALTER TABLE people ADD INDEX lname_fname_age (lame,fname,age);

    为了提高搜索效率，我们需要考虑运用多列索引,由于索引文件以B－Tree格式保存，
所以我们不用扫描任何记录，即可得到最终结果。

>在mysql中执行查询时，只能使用一个索引，如果我们在lname,fname,age上分别建索引,执行查询时，
只能使用一个索引， mysql会选择一个最严格(获得结果集记录数最少)的索引。
 
>在创建多列索引时，要根据业务需求，where子句中使用最频繁的一列放在最左边。

>最左前缀：顾名思义，就是最左优先，上例中我们创建了lname_fname_age多列索引,
相当于创建了`(lname)单列索引`,`(lname,fname)组合索引`以及`(lname,fname,age)组合索引`。

### 4. 联合索引原理介绍

假设有一张订单表(orders)，包含order_id和product_id二个字段。一共有31条数据。
我们要执行下面这个语句

    select product_id from orders where order_id in (123, 312, 223, 132, 224);

索引(order_id, product_id)的情况

    mysql> create index orderid_productid on orders(order_id, product_id)
    mysql> explain select product_id from orders where order_id in (123, 312, 223, 132, 224) \G
    *************************** 1. row ***************************
               id: 1
      select_type: SIMPLE
            table: orders
             type: range
    possible_keys: orderid_productid
              key: orderid_productid
          key_len: 5
              ref: NULL
             rows: 5
            Extra: Using where; Using index
    1 row in set (0.00 sec)
可以看到，这个组合索引被用到了,扫描的范围也很小，只有5行。

索引(product_id, order_id)的情况
把组合索引的顺序换成product_id, order_id的话，mysql就会去索引中搜索 *123 *312 *223 *132 *224，必然会有些慢了。

    mysql> create index orderid_productid on orders(product_id, order_id);
    mysql> explain select product_id from orders where order_id in (123, 312, 223, 132, 224) \G
    *************************** 1. row ***************************
               id: 1
      select_type: SIMPLE
            table: orders
             type: index
    possible_keys: NULL
              key: orderid_productid
          key_len: 10
              ref: NULL
             rows: 31
            Extra: Using where; Using index
    1 row in set (0.00 sec)
这次索引搜索的性能显然不能和上次相比了。rows:31，我的表中一共就31条数据。索引被使用部分的长度：key_len:10，比上一次的key_len:5多了一倍。
不知道是这样在索引里面查找速度快，还是直接去全表扫描更快呢

再试试另外一个查询

    mysql> alter table orders add modify_a char(255) default 'aaa';
    Query OK, 31 rows affected (0.01 sec)
    Records: 31  Duplicates: 0  Warnings: 0

    mysql> explain select modify_a from orders where order_id in (123, 312, 223, 132, 224) \G
    *************************** 1. row ***************************
               id: 1
      select_type: SIMPLE
            table: orders
             type: ALL
    possible_keys: NULL
              key: NULL
          key_len: NULL
              ref: NULL
             rows: 31
            Extra: Using where
    1 row in set (0.00 sec)
这样就不会用到索引了。刚才是因为select的product_id与where中的order_id都在索引里面的。

为什么要创建组合索引呢？这么简单的情况直接创建一个order_id的索引不就行了吗？果只有一个order_id索引，没什么问题，
会用到这个索引，然后mysql要去磁盘上的表里面取到product_id。如果有组合索引的话，mysql可以完全从索引中取到product_id，速度自然会快。
再多说几句组合索引的最左优先原则：
组合索引的第一个字段必须出现在查询组句中，这个索引才会被用到。如果有一个组合索引(col_a,col_b,col_c)，下面的情况都会用到这个索引：
* col_a = "some value";
* col_a = "some value" and col_b = "some value";
* col_a = "some value" and col_b = "some value" and col_c = "some value";
* col_b = "some value" and col_a = "some value" and col_c = "some value";

对于最后一条语句，mysql会自动优化成第三条的样子~~。下面的情况就不会用到索引：
* col_b = "aaaaaa";
* col_b = "aaaa" and col_c = "cccccc";

>[mysql组合索引与字段顺序](https://blog.csdn.net/gang_strong/article/details/54289433)。

### 5. 注意事项

1. 索引不会包含有NULL值的列  
只要列中包含有NULL值都将不会被包含在索引中，复合索引中只要有一列含有NULL值，
那么这一列对于此复合索引就是无效的。

2. 不使用NOT IN和<>操作，因为不会使用索引

3. 不要在列上进行运算，因为不会使用索引
        
        select * from users where YEAR(adddate)<2007;
    
    将在每个行上进行运算，这将导致索引失效而进行全表扫描，因此我们可以改成
    
        select * from users where adddate<'2007-01-01';
4. like语句操作  
一般情况下不鼓励使用like操作，如果非使用不可，如何使用也是一个问题。like “%aaa%” 
不会使用索引而like “aaa%”可以使用索引。

5. 索引列排序

6. 使用短索引

>[mysql组合索引与字段顺序](https://blog.csdn.net/gang_strong/article/details/54289433)。

