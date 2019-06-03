## 第八章 虚拟机字节码执行引擎

### 8.1 运行时栈帧结构

1. 栈帧是用于支持虚拟机进行**方法调用**和**方法执行**的数据结构。
2. 栈帧是虚拟机运行时数据区中的**虚拟机栈**的栈元素。
3. 栈帧存储了**局部变量表**，**操作数栈**，**动态连接**和**方法返回地址**等信息。
4. 每一个方法从调用开始到执行完成，对应一个栈帧在虚拟机栈里**入栈到出栈**。
5. 在编译代码的时候，栈帧需要多大的局部变量表和多深的操作数栈都**已经完全确定**了，并写入**方法表的Code属性**中。所以栈帧需要分配多少内存，**不受程序运行期间变量影响**。
6. 一个线程中，方法调用链可能很长，有多个方法都处于执行状态（意味着虚拟机栈中有多个栈帧）。对于执行引擎来说，只有**栈顶的栈帧**是有效的，叫做**当前栈帧（Current Stack Frame）**，对应的方法叫**当前方法（Current Method）**。

#### 8.1.1 局部变量表

![栈帧的概念结构](http://dl.iteye.com/upload/picture/pic/116489/d522394a-14a5-3833-bad5-b77979f67d0d.bmp "栈帧的概念结构")

1. 用于存放**方法参数**和**方法内部定义**的**局部变量**。
2. Java程序被编译成class时，方法的**Code属性**的**max_locals数据**项中确定了该方法需要分配的**局部变量表最大容量**。
3. 局部变量表的容量以slot（也有叫word的）为单位。使用索引（index）来访问元素。
4. boolean，byte，char，short，int，float，reference，returnAddress都是**一个单位**。
    > Java虚拟机中还有一种基本类型，是returnAddress，这种类型是用来实现finally条款的
5. long，double是**两个单位**。占两个连续的slot空间，高位在前。
6. 虚拟机使用局部变量表完成参数值到参数变量列表的传递过程。
7. 如果这个方法是**实例方法（非static方法）**，局部变量表的**第0位索引（index）**（内存中的第一个块），是一个对**堆当中的实例的引用**（代码中的`this`，就会用到这个引用）。
8. 局部变量表中的slot是**可重用的（内存可以重复使用）**，因为很多变量有自己的**作用范围**，超过范围之后就没用了，这块空间就可以留给后面的变量用了。
    
    举个例子，`if()`和`for()`中间的东西，只在两个{}之间有用，出了{}之后就没用了，这些内存就可以重复使用。
    
    还有一个例子
    ```
    public static void main(String[] args) {
        byte[] placeholder = new byte[64 * 1024 * 1024];
        System.gc();
    }
    ```
    这种情况下，placeholder对象的内存不会被回收。
    ```
    public static void main(String[] args) {
        {
            byte[] placeholder = new byte[64 * 1024 * 1024];
        }
        System.gc();
    }
    ```
    这种情况下，理论上应该被回收，但是也没有。
    ```
    public static void main(String[] args) {
        {
            byte[] placeholder = new byte[64 * 1024 * 1024];
        }
        int[] a = 0;
        System.gc();
    }
    ```
    这种情况下，内存可以被回收。
    
    这是因为：
    1. 首先，这边的`System.gc()`回收的是**堆中**的placeholder对象。
    2. 判断是否要回收的标准是，**堆中**的placeholder对象是否有来自GC Roots的引用（有就不回收，没有就回收），在这里，局部变量表中的reference就是这个引用。
    3. 在第一个例子中，变量placeholder还**在作用域中**。所以局部变量表中还存在这个reference，所以placeholder对象不会被回收。
    4. 在第二个例子中，变量placeholder**不在作用域中了**。但是，之后**没有**对局部变量表的读写操作，所以局部变量表**没变**，局部变量表中还存在这个reference，所以placeholder对象不会被回收。
    5. 在第三个例子中，变量placeholder**不在作用域中了**。同时，`int[] a = 0;`这句代码，对局部变量表**实现了写操作**，由于局部变量表中的slot是**可重用**的，之前reference的那个slot就被`int[] a`占用了。失去了reference的引用之后，通过GC Roots就找不到placeholder对象了，于是GC的时候就把它回收了。
    > 通过虚拟机JIT编译器优化，并编译成本地代码之后，第二种情况好像也可以回收掉，不过这个例子用来学习原理还是很有意义的。
9. 局部变量表中的变量，跟类变量有一点区别。类变量会在准备阶段赋予默认值，局部变量表不会。
    ```
        public static void main(String[] args) {
            int a;
            System.out.println(a);
        }
    ```
    这段代码会报错，无法运行。

#### 8.1.2 操作数栈

1. Java虚拟机的解释执行引擎被称为**基于栈的执行引擎**，其中所指的栈就是指－操作数栈。
2. 操作数栈**后入先出**（局部变量表使用index访问元素，两者的差距**类似Stack和array[]**）。
3. 最大深度在编译的时候被写入到Code属性的max_stacks之中。
4. 在规范和概念模型中，两个栈帧是完全独立的元素。但是在大多数虚拟机实现中，都会做一些优化，让两个栈帧的一部分重叠，节省空间，并避免了多余的参数复制传递操作。
    ![栈帧重叠共享数据](http://img0.tuicool.com/VZj2ue.png "栈帧重叠共享数据")
5. 虚拟机把操作数栈作为它的工作区，大多数指令都要从这里弹出数据，执行运算，然后把结果压回操作数栈。
    
    例如，iadd指令就要从操作数栈中弹出两个整数，执行加法运算，其结果又压回到操作数栈中，看看下面的示例，它演示了虚拟机是如何把两个int类型的局部变量相加，再把结果保存到第三个局部变量的：
    ```
    begin  
    iload_0    // push the int in local variable 0 onto the stack  
    iload_1    // push the int in local variable 1 onto the stack  
    iadd       // pop two ints, add them, push result  
    istore_2   // pop int, store into local variable 2  
    end
    ```
    1. 在这个字节码序列里，前两个指令iload_0和iload_1将存储在局部变量中索引为0和1的整数压入操作数栈中。
    2. 其后iadd指令从操作数栈中弹出那两个整数相加，再将结果压入操作数栈。
    3. 第四条指令istore_2则从操作数栈中弹出结果，并把它存储到局部变量区索引为2的位置。
    
    下图详细表述了这个过程中局部变量和操作数栈的状态变化，图中没有使用的局部变量区和操作数栈区域以空白表示。
    ![Adding two local variables](https://www.artima.com/insidejvm/ed2/images/fig5-10.gif "Adding two local variables")

#### 8.1.3 动态连接



#### 8.1.4 方法返回地址
#### 8.1.5 附加信息
### 8.2 方法调用
#### 8.2.1 解析
#### 8.2.2 分派
### 8.3 基于栈的字节码解释执行引擎
#### 8.3.1 解释执行
#### 8.3.2 基于栈的指令集与基于寄存器的指令集
#### 8.3.3 基于栈的解释器执行过程

## 参考资料

> 1. 深入理解Java虚拟机第八章
>
> 2. [栈帧、局部变量表、操作数栈](https://blog.csdn.net/a616413086/article/category/6205912)