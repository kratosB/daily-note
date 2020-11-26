# [这8种常见的SQL错误用法，80%的人还在使用](https://mp.weixin.qq.com/s/RDaCUV_EvJqbuRnE4UeAYA)

本文引用自[这8种常见的SQL错误用法，80%的人还在使用](https://mp.weixin.qq.com/s/RDaCUV_EvJqbuRnE4UeAYA)，这里只是存储供自己复习使用，原文可至原地址查看。

## 1. LIMIT 语句

分页查询是最常用的场景之一，但也通常也是最容易出问题的地方。比如对于下面简单的语句，一般DBA想到的办法是在`type`,`name`,`create_time`字段上加组合索引。这样条件排序都能有效的利用到索引，性能迅速提升。
```
SELECT * 
FROM   operation 
WHERE  type = 'SQLStats' 
       AND name = 'SlowLog' 
ORDER  BY create_time 
LIMIT  1000, 10;
```
好吧，可能90%以上的DBA解决该问题就到此为止。但当`LIMIT`子句变成`LIMIT 1000000,10`时，程序员仍然会抱怨：我只取10条记录为什么还是慢？

要知道数据库也并不知道第1000000条记录从什么地方开始，即使有索引也需要从头计算一次。出现这种性能问题，多数情形下是程序员偷懒了。

在前端数据浏览翻页，或者大数据分批导出等场景下，是可以将上一页的最大值当成参数作为查询条件的。SQL重新设计如下：
```
SELECT   * 
FROM     operation 
WHERE    type = 'SQLStats' 
AND      name = 'SlowLog' 
AND      create_time > '2017-03-16 14:00:00' 
ORDER BY create_time limit 10;
```
在新设计下查询时间基本固定，不会随着数据量的增长而发生变化。

---

在[一次SQL查询优化原理分析（900W+数据，从17s到300ms）](https://mp.weixin.qq.com/s/CobdICM1vOUumLS2POVhSA)这个贴子中，看到了另一种优化方式。

在不考虑`order by`的情况下，可以把语句改成如下形式：
```
SELECT * 
FROM   operation tablea
LEFT JOIN
(
SELECT id 
FROM   operation 
WHERE  type = 'SQLStats' 
       AND name = 'SlowLog' 
LIMIT  1000, 10
)
tableb
ON tablea.id = tableb.id;
```
原理应该是减少回表。原语句应该是要回表1000次，新语句应该只需要回表10次，所以速度快了许多。当然，前提是，所有查询条件都要有覆盖索引。

然后，我们再来看一下考虑`order by`的情况。

参考《Mysql实战45讲，第16章，“order by”是怎么工作的》中的说法，使用覆盖索引，可以让order by的回表次数减少。那么，理论上说，把`type`,`name`,`create_time`做成一个联合索引（索引本来就自带id），应该就可以不用回表1000次了。

## 2. 隐式转换

SQL语句中查询变量和字段定义类型不匹配是另一个常见的错误。比如下面的语句：
```
mysql> explain extended SELECT * 
     > FROM my_balance b 
     > WHERE b.bpn = 14000000123 
     > AND b.isverified IS NULL ;
mysql> show warnings;
| Warning | 1739 | Cannot use ref access on index 'bpn' due to type or collation conversion on field 'bpn'
```
其中字段 bpn 的定义为 varchar(20)，MySQL 的策略是将字符串转换为数字之后再比较。函数作用于表字段，索引失效。

上述情况可能是应用程序框架自动填入的参数，而不是程序员的原意。现在应用框架很多很繁杂，使用方便的同时也小心它可能给自己挖坑。

## 3. 关联更新、删除

虽然MySQL5.6引入了物化特性，但需要特别注意它目前仅仅针对查询语句的优化。对于更新或删除需要手工重写成JOIN。

比如下面`UPDATE`语句，MySQL实际执行的是循环/嵌套子查询（DEPENDENT SUBQUERY)，其执行时间可想而知。
```
UPDATE operation o 
SET    status = 'applying' 
WHERE  o.id IN (SELECT id 
                FROM   (SELECT o.id, 
                               o.status 
                        FROM   operation o 
                        WHERE  o.group = 123 
                               AND o.status NOT IN ( 'done' ) 
                        ORDER  BY o.parent, 
                                  o.id 
                        LIMIT  1) t);
```
执行计划
```
+----+--------------------+-------+-------+---------------+---------+---------+-------+------+-----------------------------------------------------+
| id | select_type        | table | type  | possible_keys | key     | key_len | ref   | rows | Extra                                               |
+----+--------------------+-------+-------+---------------+---------+---------+-------+------+-----------------------------------------------------+
| 1  | PRIMARY            | o     | index |               | PRIMARY | 8       |       | 24   | Using where; Using temporary                        |
| 2  | DEPENDENT SUBQUERY |       |       |               |         |         |       |      | Impossible WHERE noticed after reading const tables |
| 3  | DERIVED            | o     | ref   | idx_2,idx_5   | idx_5   | 8       | const | 1    | Using where; Using filesort                         |
+----+--------------------+-------+-------+---------------+---------+---------+-------+------+-----------------------------------------------------+
```
重写为`JOIN`之后，子查询的选择模式从DEPENDENT SUBQUERY 变成DERIVED，执行速度大大加快，从7秒降低到2毫秒。
```
UPDATE operation o 
       JOIN  (SELECT o.id, 
                            o.status 
                     FROM   operation o 
                     WHERE  o.group = 123 
                            AND o.status NOT IN ( 'done' ) 
                     ORDER  BY o.parent, 
                               o.id 
                     LIMIT  1) t
         ON o.id = t.id 
SET    status = 'applying' 
```
执行计划简化为：
```
+----+-------------+-------+------+---------------+-------+---------+-------+------+-----------------------------------------------------+
| id | select_type | table | type | possible_keys | key   | key_len | ref   | rows | Extra                                               |
+----+-------------+-------+------+---------------+-------+---------+-------+------+-----------------------------------------------------+
| 1  | PRIMARY     |       |      |               |       |         |       |      | Impossible WHERE noticed after reading const tables |
| 2  | DERIVED     | o     | ref  | idx_2,idx_5   | idx_5 | 8       | const | 1    | Using where; Using filesort                         |
+----+-------------+-------+------+---------------+-------+---------+-------+------+-----------------------------------------------------+
```

## 4. 混合排序(这个点只能说比较讨巧，我觉得学习意义不大)

MySQL 不能利用索引进行混合排序。但在某些场景，还是有机会使用特殊方法提升性能的。
```
SELECT * 
FROM   my_order o 
       INNER JOIN my_appraise a ON a.orderid = o.id 
ORDER  BY a.is_reply ASC, 
          a.appraise_time DESC 
LIMIT  0, 20 
```
执行计划显示为全表扫描：
```
+----+-------------+-------+--------+-------------+---------+---------+---------------+---------+-+
| id | select_type | table | type   | possible_keys     | key     | key_len | ref      | rows    | Extra    
+----+-------------+-------+--------+-------------+---------+---------+---------------+---------+-+
|  1 | SIMPLE      | a     | ALL    | idx_orderid | NULL    | NULL    | NULL    | 1967647 | Using filesort |
|  1 | SIMPLE      | o     | eq_ref | PRIMARY     | PRIMARY | 122     | a.orderid |       1 | NULL           |
+----+-------------+-------+--------+---------+---------+---------+-----------------+---------+-+
```
由于 is_reply 只有0和1两种状态，我们按照下面的方法重写后，执行时间从1.58秒降低到2毫秒。
```
SELECT * 
FROM   ((SELECT *
         FROM   my_order o 
                INNER JOIN my_appraise a 
                        ON a.orderid = o.id 
                           AND is_reply = 0 
         ORDER  BY appraise_time DESC 
         LIMIT  0, 20) 
        UNION ALL 
        (SELECT *
         FROM   my_order o 
                INNER JOIN my_appraise a 
                        ON a.orderid = o.id 
                           AND is_reply = 1 
         ORDER  BY appraise_time DESC 
         LIMIT  0, 20)) t 
ORDER  BY  is_reply ASC, 
          appraisetime DESC 
LIMIT  20;
```

## 5. EXISTS语句

MySQL对待`EXISTS`子句时，仍然采用嵌套子查询的执行方式。如下面的SQL语句：
```
SELECT *
FROM   my_neighbor n 
       LEFT JOIN my_neighbor_apply sra 
              ON n.id = sra.neighbor_id 
                 AND sra.user_id = 'xxx' 
WHERE  n.topic_status < 4 
       AND EXISTS(SELECT 1 
                  FROM   message_info m 
                  WHERE  n.id = m.neighbor_id 
                         AND m.inuser = 'xxx') 
       AND n.topic_type <> 5 
```
执行计划为：
```
+----+--------------------+-------+------+-------------------+------------------+---------+-------+---------+ -----+
| id | select_type        | table | type | possible_keys     | key              | key_len | ref   | rows    | Extra   |
+----+--------------------+-------+------+-------------------+------------------+---------+-------+---------+ -----+
|  1 | PRIMARY            | n     | ALL  |                   | NULL             | NULL    | NULL  | 1086041 | Using where                   |
|  1 | PRIMARY            | sra   | ref  |                   | idx_user_id      | 123     | const |       1 | Using where          |
|  2 | DEPENDENT SUBQUERY | m     | ref  |                   | idx_message_info | 122     | const |       1 | Using index condition; Using where |
+----+--------------------+-------+------+-------------------+------------------+---------+-------+---------+ -----+
```
去掉`exists`更改为`join`，能够避免嵌套子查询，将执行时间从1.93秒降低为1毫秒。
```
SELECT *
FROM   my_neighbor n 
       INNER JOIN message_info m 
               ON n.id = m.neighbor_id 
                  AND m.inuser = 'xxx' 
       LEFT JOIN my_neighbor_apply sra 
              ON n.id = sra.neighbor_id 
                 AND sra.user_id = 'xxx' 
WHERE  n.topic_status < 4 
       AND n.topic_type <> 5 
```
新的执行计划：
```

+----+-------------+-------+--------+-------------------+-----------------------+---------+-------------+------+ -----+
| id | select_type | table | type   | possible_keys     | key                   | key_len | ref         | rows | Extra                 |
+----+-------------+-------+--------+-------------------+-----------------------+---------+-------------+------+ -----+
|  1 | SIMPLE      | m     | ref    |                   | idx_message_info      | 122     | const       |    1 | Using index condition |
|  1 | SIMPLE      | n     | eq_ref |                   | PRIMARY               | 122     | ighbor_id   |    1 | Using where            |
|  1 | SIMPLE      | sra   | ref    |                   | idx_user_id           | 123     | const       |    1 | Using where           |
+----+-------------+-------+--------+-------------------+-----------------------+---------+-------------+------+ -----+
```

## 6. 条件下推

未完待续

## 7. 提前缩小范围

未完待续

## 8. 中间结果集下推

未完待续

## 总结


## 参考链接
>1. [这8种常见的SQL错误用法，80%的人还在使用](https://mp.weixin.qq.com/s/RDaCUV_EvJqbuRnE4UeAYA)
>2. [一次SQL查询优化原理分析（900W+数据，从17s到300ms）](https://mp.weixin.qq.com/s/CobdICM1vOUumLS2POVhSA)