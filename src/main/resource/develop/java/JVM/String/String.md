# [深入理解Java中的String](https://blog.csdn.net/qq_34490018/article/details/82110578)

## 字符串常量池

1. 字符串常量池存在运行时常量池之中（在JDK7之前存在运行时常量池之中，在JDK7已经将其转移到堆中）。
2. 字符串常量池在JVM内部就是一个**HashTable**。

## String在JVM层解析

### 两种创建字符串基本形式

```text
String s1 = "1";
String s2 = new String("1");
```
![例子图1](https://img-blog.csdn.net/20180827123540873?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzM0NDkwMDE4/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)
1. s1创建流程。s1使用""引号（也是平时所说的字面量/常量）创建字符串。
    1. 在编译期的时候就对字符串常量池进行判断是否存在该字符串。
        1. 如果存在则不创建直接返回对象的引用。
        2. 如果不存在，则先在字符串常量池中创建该字符串实例再返回实例的引用。
    >编译期的字符串常量池是静态字符串常量池。
2. s2创建流程。s2使用关键词new创建字符串。
    1. JVM会首先检查字符串常量池。
        1. 如果该字符串已经存在字符串常量池中，那么不再在字符串常量池创建该字符串对象。
        2. 如果字符串不存在字符串常量池中，就会实例化该字符串并且将其放到字符串常量池中。
    2. 然后在堆中复制该对象的副本。
    3. 然后将堆中对象的地址赋值给引用s2。
    >此时是运行期，那么字符串常量池是在运行时字符串常量池中的。

### "+"连接形式创建字符串：

```text
String s1 = "1" + "2" + "3";
```
![例子图2](https://img-blog.csdn.net/20180827123632152?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzM0NDkwMDE4/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)
1. 使用包含常量的字符串连接创建。
    1. 也是常量，编译期就能确定了。
    2. 直接入字符串常量池，当然同样需要判断是否已经存在该字符串。
    >这个应该也是"编译期的字符串常量池是静态字符串常量池。"

```text
String s2 = "1" + "3" + new String("1") + "4";
```
![例子图3](https://img-blog.csdn.net/20180827123647339?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzM0NDkwMDE4/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)
1. 使用"+"连接字符串中含有变量。
    1. 运行期才能确定的。
        >但是，13，1，4这几个字符串，编译器就会放在字符串常量池中。
    2. 首先连接操作最开始时如果都是字符串常量，编译后将尽可能多的字符串常量连接在一起，形成新的字符串常量参与后续的连接。
        >我理解如果这个例子最后是`+ "4" + "5";`，那么编译期就会把13，1，45放在字符串常量池。（可通过反编译工具jd-gui进行查看）
    3. 接下来的字符串连接是从左向右依次进行，以最左边的字符串为参数创建StringBuilder对象，然后依次对右边进行append，最后将toString()转换成String。
        >这里的过程就是`String s2 = new StringBuilder("13").append(new String("1")).append("4").toString();`。
    4. 这个例子中，实际上产生了一个StringBuilder对象，一个String("1")对象，以及最后的String("1314")对象。

```text
final String s1 = "1";
String s2 = "12";
String s3 = s1 + "2";
System.out.println(s2 == s3);
```
1. 上面这种情况，虽然s3是字符串+字符串常量，但是由于s1有final修饰，在编译期也可以确定，所以s3跟s2都是字符串常量池中的引用，所以是true。去掉final则是false。

```text
String s3 = new String("1") + new String("1");
```
![例子图4](https://img-blog.csdn.net/20180827123737708?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzM0NDkwMDE4/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)
1. 过程跟上面类似，这个例子中，静态字符串常量池中有一个"1"，堆中有两个String("1")和一个StringBuilder，以及最后的String("11")。

### String.intern()

```text
String s3 = new String("java1");
String s4 = s3.intern();
System.out.println(s3 == s4);//false

String s5 = new String("1") + new String("1");
String s6 = s5.intern();
System.out.println(s5 == s6);//jdk6 false, jdk7和jdk8 true
```

1. s3 == s4 false很好理解，一个是字符串常量池中的直接引用，一个是String对象引用。

![jdk6例子图](https://img-blog.csdn.net/20180827123836350?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzM0NDkwMDE4/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

1. s5 == s6 jdk6中false，跟上面s3 == s4 false的情况差不多。
2. JDK6中的字符串常量池是放在永久代的，永久代和Java堆是两个完全分开的区域。
3. intern之后：
    1. 如果字符串常量池中已有该字符串，则返回池中的字符串。
    2. 如果字符串常量池中没有该字符串，将此字符串添加到字符串常量池中，并返回字符串的引用。
4. s5是两个new出来的String append到一起的，所以字符串常量池中原本没有这个值。
5. s5是堆中String的引用，s6是字符串常量池中的引用。所以不相等

![jdk7，8例子图](https://img-blog.csdn.net/20180827123854818?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzM0NDkwMDE4/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

1. s5 == s6 jdk7，8中true。
2. jdk7，8中，字符串常量池已经被转移至Java堆中，intern也优化了。
3. intern时：
    1. 如果字符串常量池中已有该字符串，则返回池中字符串。
    2. 如果字符串常量池中没有该字符串，则直接存储堆中的引用。（意思是，字符串常量池中存储的是指向堆里的对象的引用）
4. s5是两个new出来的String append到一起的，所以字符串常量池中原本没有这个值。所以这里s5和s6，都是堆内存中的引用，所以就是true。
    >从这个角度分析jdk7，8下的s3 == s4，因为s3是直接new出来的，所以字符串常量池中有这个字符串，所以intern返回（s4）的是字符串常量池中的，所以是false








## 引用

>1. [String类详解](https://www.cnblogs.com/zhangyinhua/p/7689974.html#_lab2_0_0)
>2. [深入理解Java String类](https://blog.csdn.net/ifwinds/article/details/80849184)
>3. [深入理解Java中的String（大坑）](https://blog.csdn.net/qq_34490018/article/details/82110578)
>3. [深入理解Java中的String](https://www.cnblogs.com/xiaoxi/p/6036701.html)
>3. [深入解析String#intern](https://tech.meituan.com/2014/03/06/in-depth-understanding-string-intern.html)