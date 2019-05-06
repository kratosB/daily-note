## 第二章 Java内存区域与内存溢出异常

### 2.1 运行时数据区域

Java虚拟机会把运行过程中的内存分为几个不同的数据区。每个区域用途不统，创建和销毁的时间不统。主要包括一下几个数据区

![Java运行时数据区](https://ws1.sinaimg.cn/large/006tNc79ly1fmk5v19cmvj30g20anq3y.jpg "Java运行时数据区")

#### 2.1.1 程序计数器

程序计数器（Program Counter Register）主要用来记录当前线程所执行的字节码的行号，
Java虚拟机通过改编计数器的值来选取下一条要被执行的字节码指令，

程序计数器（Program Counter Register）主要用来记录当前线程所执行的字节码的行号，Java虚拟机通过改编计数器的值来选取下一条要被执行的字节码指令，

### 2.2 对象访问




### 2.3 实战：OutOfMemoryError