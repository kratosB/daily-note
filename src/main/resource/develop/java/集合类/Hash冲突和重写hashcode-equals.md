# [Hash冲突+hashcode重写+equals重写](https://mp.weixin.qq.com/s/1V304OsOPPuBdI6GAzBUYQ)

1. 介绍hash冲突。
2. 构建HashCode相同的的String，例如Aa和BB，hashcode都是2112。
    >这里我之前的理解有点误区。  
    >hashMap的key放在哪个格子，使用hashcode和CAPACITY`与`出来的（例如一个hashcode是2，一个是10，跟CAPACITY=8`与`了之后都在2这个格子）。  
    >我开始以为这边的相同的是与出来的结果是相同的，后来发现是hashcode相同，所以这两个字符串不论在size是多么大的hashMap里，都会冲突。  
3. 针对hashcode是相同的这种情况，分析一下hashmap的“链表<->红黑树”的条件（链表长度大于>8，size>64）。
4. 实体类当作key的时候，为什么要重写hashcode+equals。（更新的时候会出错）
    >1. 不重写hashcode：新老key的hashcode不一样，两组数据共存，被放在（hashmap中的）数组的不同的两个格子里。![不重写hashcode](https://mmbiz.qpic.cn/mmbiz_png/lnCqjsQ6QHeHKjcpvlHpKNLxF11ELa8awpkjEbSUljDkicTEKCKK5gRprfcicdpicyxCPJGC84MicCF57NliaHOnKhA/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)
    >2. 不重写equals：新老key的hashcode一样，放在（hashmap中的）数组的同一个格子里，但是不equals，所以作为链表上的两个结点共存，而不是覆盖。![不重写equals](https://mmbiz.qpic.cn/mmbiz_png/lnCqjsQ6QHeHKjcpvlHpKNLxF11ELa8aXlXXZSJvl1fr3pmhia56qOYhtEzyDdIBW6eoHQicYxDd4EFhY4c5HWYQ/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)
5. 为什么之前hashcode计算用37，现在用31。
   >1. 37和31都是奇素数。
   >2. 31算乘除的时候可以直接用移位，效率高。

## 引用
>1. [Hash冲突+hashcode重写+equals重写](https://mp.weixin.qq.com/s/1V304OsOPPuBdI6GAzBUYQ)