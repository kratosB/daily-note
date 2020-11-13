# [MySql(单表)查询执行原理](https://mp.weixin.qq.com/s/cvrMKoawIbIJjMZhAEYV-g)

MySQL Server有一个称为查询优化器的模块，一条查询语句进行语法解析之后就会被交给查询优化器来进行优化，优化的结果就是生成一个所谓的执行计划，这个执行计划表明了应该使用哪些索引进行查询，表之间的连接顺序是啥样的，最后会按照执行计划中的步骤调用存储引擎提供的方法来真正的执行查询，并将查询结果返回给用户。不过查询优化这个主题有点儿大，在学会跑之前还得先学会走，所以本章先来瞅瞅MySQL怎么执行单表查询（就是FROM子句后边只有一个表，最简单的那种查询～）。

我们先得有个表：

    CREATE TABLE single_table (
        id INT NOT NULL AUTO_INCREMENT,
        key1 VARCHAR(100),
        key2 INT,
        key3 VARCHAR(100),
        key_part1 VARCHAR(100),
        key_part2 VARCHAR(100),
        key_part3 VARCHAR(100),
        common_field VARCHAR(100),
        PRIMARY KEY (id),
        KEY idx_key1 (key1),
        UNIQUE KEY idx_key2 (key2),
        KEY idx_key3 (key3),
        KEY idx_key_part(key_part1, key_part2, key_part3)
    ) Engine=InnoDB CHARSET=utf8;
    
我们为这个`single_table`表建立了1个聚簇索引和4个二级索引，分别是：

* 为`id`列建立的聚簇索引。
* 为`key1`列建立的`idx_key1`二级索引。
* 为`key2`列建立的`idx_key2`二级索引，而且该索引是唯一二级索引。
* 为`key3`列建立的`idx_key3`二级索引。
* 为`key_part1`、`key_part2`、`key_part3`列建立的`idx_key_part`二级索引，这也是一个联合索引。

然后我们需要为这个表插入10000行记录，除id列外其余的列都插入随机值。

## 访问方法（access method）的概念

我们平时所写的那些查询语句本质上只是一种声明式的语法，只是告诉MySQL我们要获取的数据符合哪些规则，至于MySQL背地里是怎么把查询结果搞出来的那是MySQL自己的事儿。对于单个表的查询来说，设计MySQL的大叔把查询的执行方式大致分为下边两种：

* 使用全表扫描进行查询  
这种执行方式很好理解，就是把表的每一行记录都扫一遍嘛，把符合搜索条件的记录加入到结果集就完了。不管是啥查询都可以使用这种方式执行，当然，这种也是最笨的执行方式。
* 使用索引进行查询  
因为直接使用全表扫描的方式执行查询要遍历好多记录，所以代价可能太大了。如果查询语句中的搜索条件可以使用到某个索引，那直接使用索引来执行查询可能会加快查询执行的时间。使用索引来执行查询的方式五花八门，又可以细分为许多种类：
    * 针对主键或唯一二级索引的等值查询
    * 针对普通二级索引的等值查询
    * 针对索引列的范围查询
    * 直接扫描整个索引

设计MySQL的大叔把MySQL执行查询语句的方式称之为访问方法或者访问类型。同一个查询语句可能可以使用多种不同的访问方法来执行，虽然最后的查询结果都是一样的，但是执行的时间可能差老鼻子远了，就像是从钟楼到大雁塔，你可以坐火箭去，也可以坐飞机去，当然也可以坐乌龟去

## const(常数级别)

有的时候我们可以通过主键列来定位一条记录，比方说这个查询：

    SELECT * FROM single_table WHERE id = 1438;
    
MySQL会直接利用主键值在聚簇索引中定位对应的用户记录，就像这样：

![聚簇索引示意图](https://mmbiz.qpic.cn/mmbiz/RLmbWWew55FFlCVpOzklAMrPSibkRInMzMGichCRIUk5ibZokQ93hbFxOotAqKqop8ic9MhRSNYE7iaZBYrH88X23fw/640 "聚簇索引示意图")    

原谅我把聚簇索引对应的复杂的B+树结构搞了一个极度精简版，为了突出重点，我们忽略掉了页的结构，直接把所有的叶子节点的记录都放在一起展示，而且记录中只展示我们关心的索引列，对于single_table表的聚簇索引来说，展示的就是id列。我们想突出的重点就是：B+树叶子节点中的记录是按照索引列排序的，对于的聚簇索引来说，它对应的B+树叶子节点中的记录就是按照id列排序的。B+树本来就是一个矮矮的大胖子，所以这样根据主键值定位一条记录的速度贼快。

类似的，我们根据唯一二级索引列来定位一条记录的速度也是贼快的，比如下边这个查询：

    SELECT * FROM single_table WHERE key2 = 3841;

这个查询的执行过程的示意图就是这样：

![唯一二级索引示意图](https://mmbiz.qpic.cn/mmbiz/RLmbWWew55FFlCVpOzklAMrPSibkRInMzxdBa4ibrOJHM9Y3hspp0icYv5auWympB8JbcsMGDRQhuibIEvptghk5LQ/640 "唯一二级索引示意图")

可以看到这个查询的执行分两步，第一步先从idx_key2对应的B+树索引中根据key2列与常数的等值比较条件定位到一条二级索引记录，然后再根据该记录的id值到聚簇索引中获取到完整的用户记录。

设计MySQL的大叔认为通过主键或者唯一二级索引列与常数的等值比较来定位一条记录是像坐火箭一样快的，所以他们把这种通过主键或者唯一二级索引列来定位一条记录的访问方法定义为：const，意思是常数级别的，代价是可以忽略不计的。不过这种const访问方法只能在主键列或者唯一二级索引列和一个常数进行等值比较时才有效，如果主键或者唯一二级索引是由多个列构成的话，索引中的每一个列都需要与常数进行等值比较，这个const访问方法才有效（这是因为只有该索引中全部列都采用等值比较才可以定位唯一的一条记录）。
>其实这里还有eq_ref：主键或者唯一索引，跟const基本类似吧，速度上const>eq_ref，两者区别没研究过，有兴趣可以研究下

对于唯一二级索引来说，查询该列为NULL值的情况比较特殊，比如这样：

    SELECT * FROM single_table WHERE key2 IS NULL;
    
因为唯一二级索引列并不限制NULL值的数量，所以上述语句可能访问到多条记录，也就是说上边这个语句不可以使用const访问方法来执行。

## ref

有时候我们对某个普通的二级索引列与常数进行等值比较，比如这样：

    SELECT * FROM single_table WHERE key1 = 'abc';

对于这个查询，我们当然可以选择全表扫描来逐一对比搜索条件是否满足要求，我们也可以先使用二级索引找到对应记录的id值，然后再回表到聚簇索引中查找完整的用户记录。由于普通二级索引并不限制索引列值的唯一性，所以可能找到多条对应的记录，也就是说使用二级索引来执行查询的代价取决于等值匹配到的二级索引记录条数。如果匹配的记录较少，则回表的代价还是比较低的，所以MySQL可能选择使用索引而不是全表扫描的方式来执行查询。设计MySQL的大叔就把这种搜索条件为二级索引列与常数等值比较，采用二级索引来执行查询的访问方法称为：ref。我们看一下采用ref访问方法执行查询的图示：

![二级索引示意图](https://mmbiz.qpic.cn/mmbiz/RLmbWWew55FFlCVpOzklAMrPSibkRInMz3oI26iadpNlGkSP0tdtuYPZs7XoUG1vnewPwHW8jeh4ozMicdCSQaTZA/640 "二级索引示意图")

从图示中可以看出，对于普通的二级索引来说，通过索引列进行等值比较后可能匹配到多条连续的记录，而不是像主键或者唯一二级索引那样最多只能匹配1条记录，所以这种ref访问方法比const差了那么一丢丢，但是在二级索引等值比较时匹配的记录数较少时的效率还是很高的（如果匹配的二级索引记录太多那么回表的成本就太大了），跟坐高铁差不多。不过需要注意下边两种情况：

* 二级索引列值为NULL的情况  
不论是普通的二级索引，还是唯一二级索引，它们的索引列对包含NULL值的数量并不限制，所以我们采用key IS NULL这种形式的搜索条件最多只能使用ref的访问方法，而不是const的访问方法。

* 对于某个包含多个索引列的二级索引来说，只要是最左边的连续索引列是与常数的等值比较就可能采用ref的访问方法，比方说下边这几个查询：

    ```sql
    SELECT * FROM single_table WHERE key_part1 = 'god like';
    
    SELECT * FROM single_table WHERE key_part1 = 'god like' AND key_part2 = 'legendary';
    
    SELECT * FROM single_table WHERE key_part1 = 'god like' AND key_part2 = 'legendary' AND key_part3 = 'penta kill';
    ```
    但是如果最左边的连续索引列并不全部是等值比较的话，它的访问方法就不能称为ref了，比方说这样：    
    ```sql
    SELECT * FROM single_table WHERE key_part1 = 'god like' AND key_part2 > 'legendary';
    ```

## ref_or_null

有时候我们不仅想找出某个二级索引列的值等于某个常数的记录，还想把该列的值为NULL的记录也找出来，就像下边这个查询：

    SELECT * FROM single_demo WHERE key1 = 'abc' OR key1 IS NULL;
    
当使用二级索引而不是全表扫描的方式执行该查询时，这种类型的查询使用的访问方法就称为ref_or_null，这个ref_or_null访问方法的执行过程如下：

![ref_or_null](https://mmbiz.qpic.cn/mmbiz/RLmbWWew55FFlCVpOzklAMrPSibkRInMzkqgP2OwlsFS5kwkicWArKGYdAwyuYBXeeGS5MA5mCa4fEqFjnWFKbBg/640 "ref_or_null")

可以看到，上边的查询相当于先分别从idx_key1索引对应的B+树中找出key1 IS NULL和key1 = 'abc'的两个连续的记录范围，然后根据这些二级索引记录中的id值再回表查找完整的用户记录。

## range

我们之前介绍的几种访问方法都是在对索引列与某一个常数进行等值比较的时候才可能使用到（ref_or_null比较奇特，还计算了值为NULL的情况），但是有时候我们面对的搜索条件更复杂，比如下边这个查询：

    SELECT * FROM single_table WHERE key2 IN (1438, 6328) OR (key2 >= 38 AND key2 <= 39);

我们当然还可以使用全表扫描的方式来执行这个查询，不过也可以使用二级索引 + 回表的方式执行，如果采用二级索引 + 回表的方式来执行的话，那么此时的搜索条件就不只是要求索引列与常数的等值匹配了，而是索引列需要匹配某个或某些范围的值，在本查询中key2列的值只要匹配下列3个范围中的任何一个就算是匹配成功了：

* key2的值是1438
* key2的值是6328
* key2的值在38和79之间

设计MySQL的大叔把这种利用索引进行范围匹配的访问方法称之为：range。

> 此处所说的使用索引进行范围匹配中的 `索引` 可以是聚簇索引，也可以是二级索引。

如果把这几个所谓的key2列的值需要满足的范围在数轴上体现出来的话，那应该是这个样子：

![](https://mmbiz.qpic.cn/mmbiz/RLmbWWew55FFlCVpOzklAMrPSibkRInMzPspOWcsjvQbAU79BafMw03yAFoHJ0OwO6pl3Gkuv9XXyQ6Yiaff5HNA/640)

也就是从数学的角度看，每一个所谓的范围都是数轴上的一个区间，3个范围也就对应着3个区间：

* 范围1：key2 = 1438
* 范围2：key2 = 6328
* 范围3：key2 ∈ [38, 39]，注意这里是闭区间。

我们可以把那种索引列等值匹配的情况称之为单点区间，上边所说的范围1和范围2都可以被称为单点区间，像范围3这种的我们可以称为连续范围区间。

## index

看下边这个查询：

    SELECT key_part1, key_part2, key_part3 FROM single_table WHERE key_part2 = 'abc';

由于key_part2并不是联合索引idx_key_part最左索引列，所以我们无法使用ref或者range访问方法来执行这个语句。但是这个查询符合下边这两个条件：

* 它的查询列表只有3个列：key_part1, key_part2, key_part3，而索引idx_key_part又包含这三个列。
* 搜索条件中只有key_part2列。这个列也包含在索引idx_key_part中。

也就是说我们可以直接通过遍历idx_key_part索引的叶子节点的记录来比较key_part2 = 'abc'这个条件是否成立，把匹配成功的二级索引记录的key_part1, key_part2, key_part3列的值直接加到结果集中就行了。由于二级索引记录比聚簇索记录小的多（聚簇索引记录要存储所有用户定义的列以及所谓的隐藏列，而二级索引记录只需要存放索引列和主键），而且这个过程也不用进行回表操作，所以直接遍历二级索引比直接遍历聚簇索引的成本要小很多，设计MySQL的大叔就把这种采用遍历二级索引记录的执行方式称之为：index。

## all

最直接的查询执行方式就是我们已经提了无数遍的全表扫描，对于InnoDB表来说也就是直接扫描聚簇索引，设计MySQL的大叔把这种使用全表扫描执行查询的方式称之为：all。

## 引用
> [单表查询是如何执行的](https://mp.weixin.qq.com/s/cvrMKoawIbIJjMZhAEYV-g)