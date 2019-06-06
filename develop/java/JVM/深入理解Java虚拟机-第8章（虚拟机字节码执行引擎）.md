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
    >Java虚拟机中还有一种基本类型，是returnAddress，这种类型是用来实现finally条款的
5. long，double是**两个单位**。占两个连续的slot空间，高位在前。
6. 虚拟机使用局部变量表完成参数值到参数变量列表的传递过程。
7. 如果这个方法是**实例方法（非static方法）**，局部变量表的**第0位索引（index）**（内存中的第一个块），是一个对**堆当中的实例的引用**（代码中的`this`，就会用到这个引用）。
8. 局部变量表中的slot是**可重用的（内存可以重复使用）**，因为很多变量有自己的**作用范围**，超过范围之后就没用了，这块空间就可以留给后面的变量用了。
    
    举个例子，`if()`和`for()`中间的东西，只在两个{}之间有用，出了{}之后就没用了，这些内存就可以重复使用。
    
    还有一个例子
    ```text
    public static void main(String[] args) {
        byte[] placeholder = new byte[64 * 1024 * 1024];
        System.gc();
    }
    ```
    这种情况下，placeholder对象的内存不会被回收。
    ```text
    public static void main(String[] args) {
        {
            byte[] placeholder = new byte[64 * 1024 * 1024];
        }
        System.gc();
    }
    ```
    这种情况下，理论上应该被回收，但是也没有。
    ```text
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
    >通过虚拟机JIT编译器优化，并编译成本地代码之后，第二种情况好像也可以回收掉，不过这个例子用来学习原理还是很有意义的。
9. 局部变量表中的变量，跟类变量有一点区别。类变量会在准备阶段赋予默认值，局部变量表不会。
    ```text
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
    ```text
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

这一块不太懂，大概理解如下：
1. Class文件的常量池中有大量符号引用。
2. 有一部分符号引用，会在类加载阶段或者第一次使用的时候转化成为直接引用，这个过程叫做静态解析。
3. 其他一部分符号引用，在每一次的运行期间转化成直接引用，这个过程叫做动态连接（个人感觉跟动态加载类似？）。
看看后面的8.2部分怎么讲的。

#### 8.1.4 方法返回地址

1. 一个方法被执行之后，有两种方式退出，分别是，**正常完成出口**（Normal Method Invocation Completion），**异常完成出口**（Abrupt Method Invocation Completion）。
2. 正常完成出口：执行引擎遇到任意一个**方法返回的字节码指令**，正常完成出口有没有返回值看具体指令类型。
3. 异常完成出口：方法执行过程中遇到了**异常**（Exception），并且这个异常**没有在方法体内处理**（没有catch），异常完成出口一定**没有返回值**。
4. 方法退出的过程，实际上等同于**当前栈帧出栈**，所以可能会有如下操作：
    1. 恢复上层方法的**局部变量表**和**操作数栈**。
    2. 把**返回值**（如果有的话）压入调用者的栈帧的操作数栈。
    3. 调整**程序计数器**的值指向调用者的调用指令后的一个指令。

#### 8.1.5 附加信息

1. 文章中没有具体介绍。
2. 一般情况下，会把动态连接，方法返回地址，其他附加信息，全部归为一类，称为栈帧信息。

### 8.2 方法调用

1. 方法调用不是方法执行，调用阶段唯一的任务，就是确定被调用方法的版本（即调用哪个方法），暂时还不涉及方法内部具体运行过程。
2. Java中，方法调用在class文件里存储的是符号调用，而不是直接引用。
3. Java中，需要在类加载期间甚至运行期间，才能确定目标方法的直接引用。

#### 8.2.1 解析

1. 所有方法调用中的目标方法，在Class文件里都是常量池中的符号引用。
2. 在类加载的解析阶段，会将其中**一部分**符号引用转化为直接引用。
3. 解析的**前提条件**：方法在程序运行前，就有一个**可确定的调用版本**（编译器可知），并且**运行期间不可变**。符合要求的主要有**静态方法**和**私有方法**两大类。
    1. `invokestatic`：调用静态方法。
    2. `invokespecial`：调用实例构造器<init>方法，私有方法和父类方法。
4. 只要能被`invokestatic`和`invokespecial`指令调用的方法，都可以在解析阶段把符号引用转化为直接引用，这些方法被称为**非虚方法**。
    
    ```java
        /**
         * 方法静态解析演示
         *
         */
         public class StaticResolution{
            
            public static void sayHello() {
                System.out.println("hello world");
            }
            
            public static void main(String[] args) {
                StaticResolution.sayHello();
            }
            
         }
    ```
    上面是一个最常见的解析调用的例子，在这个例子中，静态方法sayHello()只可能属于类型StaticResolution。
    ```text
        D:\Develop\>javap -verbose StaticResolution
        public static void main(java.lang.String[]);
          code:
           Stack=0, Locals=1, Args_size=1
           0:   invokestatic    #31; //Method sayHello:()V
           3:   return
        LineNumberTable:
         line 15: 0
         line 16: 3
    ```
    使用javap命令查看这段程序的字节码，会发现的确是通过`invokestatic`命令来调用sayHello()方法的。
5. Java中的非虚方法，除了使用`invokestatic`和`invokespecial`调用的方法之外，还有一种，就是被final修饰的方法。
    >虽然final方法是使用`invokevirtual`指令来调用的，但是由于它无法被覆盖，没有其他版本，所以无须对方法接收者进行多态选择。Java语言规范中明确说明final方法是非虚方法。

#### 8.2.2 分派

1. 动态分派和静态分派机制是Java多态实现的原理。
2. 分派调用可能是静态的也可能是动态的，根据分派依据的宗量（8.2.2.3 单分派与多分派中有介绍）数可分为单分派和多分派，所以就可以分为四类：
    1. 静态单分派。
    2. 静态多分派。
    3. 动态单分派。
    4. 动态多分派。

#####  8.2.2.1 静态分派(Method Overload Resolution)

1. Dispatch这个词一般不用在静态环境，英文文档中的称呼是“Method Overload Resolution”，所以静态分派就是**方法重载解析**？
2. 先来看一个例子，后面会围绕这个类的方法来**重载（Overload）代码**，以分析虚拟机和编译器确定方法版本的过程。
    ```java
    package org.fenixsoft.polymorphic;

    /**
     * 方法静态分派演示
     */
    public class StaticDispatch {
    
        static abstract class Human {
        }
    
        static class Man extends Human {
        }
    
        static class Woman extends Human {
        }
    
        public void sayHello(Human guy) {
            System.out.println("hello,guy");
        }
    
        public void sayHello(Man guy) {
            System.out.println("hello,man");
        }
    
        public void sayHello(Woman guy) {
            System.out.println("hello,woman");
        }
    
        public static void main(String[] args) {
            Human man = new Man();
            Human woman = new Woman();
            StaticDispatch staticDispatch = new StaticDispatch();
            staticDispatch.sayHello(man);
            staticDispatch.sayHello(woman);
        }
    }
    ```
    运行结果是
    ```text
    hello,guy
    hello,guy
    ```
    这个代码实际上是在考察读者对**重载**的理解程度，从输出结果可以看出虚拟机选择执行参数类型为`Human`的重载，但是为什么呢？
    1. 上面的代码中的`Human`被称为变量的**静态类型**（static type）或者**外观类型**（apparent type）。
        1. 静态类型的变化，仅仅在使用时发生，变量本身的**静态类型不会改变**。
        2. 最终的静态类型，在**编译期可知**。
        ```text
        Human man = new Man();
        man = new Woman();
        // 静态类型变化
        staticDispatch.sayHello((Man) man);
        staticDispatch.sayHello((Woman) man);
        ```
    2. 后面的`Man`和`Woman`被称为变量的**实际类型**（actual type）。
        1. 实际类型变化的结果，在**运行期才可确定**，编译器在编译期并不知道实际类型是什么。
        ```text
        // 实际类型变化
        Human man = new Man();
        man = new Woman();
        ```
    3. 虚拟机在重载的时候是**通过参数的静态类型**而不是实际类型作为判断依据的。
        >所以Javac编译器在编译阶段，根据参数的静态类型，选择了sayHello(Human)作为调用目标，并把这个方法的**符号引用**写到main()方法里的两条`invokevirtual`指令的参数中。
    4. 所有依赖静态类型来定位方法执行版本的分派动作，都称为静态分派。（最典型应用就是方法重载）
3. 有一种**特殊情况**，重载版本并**不是唯一的**。
    
    由于字面量不需要定义，所以字面量没有**显式的静态类型**，它的静态类型，只能通过语言上的规则去理解和推断。
    
    ```java
    package org.fenixsoft.polymorphic;
    
    import java.io.Serializable;
    
    public class Overload {
    
        public static void sayHello(char arg) {
            System.out.println("hello char");
        }
    
        public static void sayHello(int arg) {
            System.out.println("hello int");
        }
    
        public static void sayHello(long arg) {
            System.out.println("hello long");
        }
    
        public static void sayHello(Character arg) {
            System.out.println("hello Character");
        }
    
        public static void sayHello(Serializable arg) {
            System.out.println("hello Serializable");
        }
    
        public static void sayHello(Object arg) {
            System.out.println("hello Object");
        }
    
        public static void sayHello(char... arg) {
            System.out.println("hello char...");
        }
    
        /**
         * 1. 输出 hello char
         * （'a'是一个char类型的数据，自然会找一个参数类型为char的重载方法）
         * 
         * 2. 注释掉sayHello(char arg)方法，输出 hello int
         * （这时候发生了一次类型转换，'a'除了可以代表字符串，还可以代表数字65）
         * 
         * 3. 注释掉sayHello(int arg)方法，输出 hello long
         * （这时发生了两次自动类型转换，'a'转换为整数65之后，进一步转换成长整数65L，匹配了参数类型为long的重载，
         * 除此之外，还有float和double，顺序是char->int->long->float->double）
         * 
         * 4. 注释掉sayHello(long arg)方法，输出 hello Character
         * （这时发生了一次自动装箱，'a'被包装成它的封装类型java.lang.Character）
         * 
         * 5. 注释掉sayHello(Character arg)方法，输出 hello Serializable
         * （这是因为Serializable是Character类实现的一个接口，自动装箱之后发现还是找不到装箱类，
         * 但是找到了装箱类实现了的接口类型，所以又发生了一次自动转型）
         * 
         * 6. 注释掉sayHello(Serializable arg)方法，输出 hello Object
         * （char装箱后转型为父类了，如果有多个父类，那么将在继承关系中从下往上搜索，越上层优先级越低）
         * 
         * 7. 注释掉sayHello(Object arg)方法，输出 hello char...
         * （七个重载方法只剩一个了，说明可变参数优先级最低，这时候'a'被当作一个数组元素）
         */
        public static void main(String[] args) {
            sayHello('a');
        }
    }
    ```
    上面的代码，演示了编译期间选择静态分派目标的过程，这个过程也是Java语言实现方法重载的本质。
    
#####  8.2.2.2 动态分派

1. 动态分派和多态性中的**重写**（Override）有着很密切的关联。下面来看一个例子。
    ```java
    package org.fenixsoft.polymorphic;
    
    public class DynamicDispatch {
    
        static abstract class Human {
            protected abstract void sayHello();
        }
    
        static class Man extends Human {
            @Override
            protected void sayHello() {
                System.out.println("man say hello");
            }
        }
    
        static class Women extends Human {
            @Override
            protected void sayHello() {
                System.out.println("women say hello");
            }
        }
    
        public static void main(String[] args) {
            Human man = new Man();
            Human women = new Women();
            man.sayHello();
            women.sayHello();
            man = new Women();
            man.sayHello();
        }
    }
    ```
    运行结果是
    ```text
    man say hello
    women say hello
    women say hello
    ```
    导致这个现象的原因很明显，是这两个变量的**实际类型不同**，Java虚拟机如何根据实际类型来分派方法执行版本呢？使用javap命令输出这段代码的字节码来看看。
    ```text
      public static void main(java.lang.String[]);
        flags: ACC_PUBLIC, ACC_STATIC
        Code:
          stack=2, locals=3, args_size=1
             0: new           #16                 // class org/fenixsoft/polymorphic/DynamicDispatch$Man
             3: dup
             4: invokespecial #18                 // Method org/fenixsoft/polymorphic/DynamicDispatch$Man."<init>":()V
             7: astore_1
             8: new           #19                 // class org/fenixsoft/polymorphic/DynamicDispatch$Woman
            11: dup
            12: invokespecial #21                 // Method org/fenixsoft/polymorphic/DynamicDispatch$Woman."<init>":()V
            15: astore_2
            16: aload_1
            17: invokevirtual #22                 // Method org/fenixsoft/polymorphic/DynamicDispatch$Human.sayHello:()V
            20: aload_2
            21: invokevirtual #22                 // Method org/fenixsoft/polymorphic/DynamicDispatch$Human.sayHello:()V
            24: new           #19                 // class org/fenixsoft/polymorphic/DynamicDispatch$Woman
            27: dup
            28: invokespecial #21                 // Method org/fenixsoft/polymorphic/DynamicDispatch$Woman."<init>":()V
            31: astore_1
            32: aload_1
            33: invokevirtual #22                 // Method org/fenixsoft/polymorphic/DynamicDispatch$Human.sayHello:()V
            36: return
    ```
    1. 0-15行是准备动作，建立man和woman的**内存（实例）空间**、调用Man和Woman类型的**实例构造器**，将这两个实例的**引用**存放在**局部变量表**的头两格之中，这个动作也就对应了代码中的这两句：
        ```text
        Human man = new Man();
        Human woman = new Woman();
        ```
    2. 之后，16和20句，分别把刚刚创建好的两个对象的引用，**压入操作数栈的栈顶**。
        1. 这两个对象是将要执行的sayHello()方法的所有者，称为**接收者**（Receiver）。
    3. 第17和21行是方法调用指令。
        1. 从字节码角度来看，这两条指令（`invokevirtual`）和参数（常量池中第22项的常量，注释显示这个常量是Human.sayHello()的符号引用）都是**完全一样**的。
        2. 但是这两个指令的最终执行**目标方法不同**。
        
        原因与`invokevirtual`指令有关，具体如下：
        1. 找到操作数**栈顶的第一个元素**所执行的对象的**实际类型**，记作C。
        2. 如果在类型C中找到与常量中的**描述符合****简单名称**都相符的方法，则进行访问权限校验，如果通过则返回这个方法的**直接引用**，查找过程结束；如果不通过，则返回java.lang.IllegalAccessError异常。
        3. 否则，按照继承关系从下往上一次对C的各个父类进行第2步的搜索和验证过程。
        4. 如果始终没有找到合适的方法，则抛出java.lang.AbstractMethodError异常。
        
        根据第一步可以发现，第17行和21行的指令，分别找到了第16行和第20行压入操作数栈的对象的引用。两次调用中的`invokevirtual`指令把常量池中的类方法符号引用**解析到了不同的直接引用上**，这个过程就是Java 语言中方法**重写的本质**。
    
#####  8.2.2.3 单分派与多分派

1. 方法的接收者与方法的参数，统称为方法的**宗量**。
2. 根据分派基于多少种宗量，可以将分派划分为**单分派**和**多分派**。
    1. 单分派：根据一个宗量对目标方法进行选择。
    2. 多分派：根据多于一个的宗量对目标方法进行选择。
    具体例子如下
    ```java
    class Dispatch {
    
        static class QQ{}
    
        static class _360 {}
    
        public static class Father {
            public void hardChoice(QQ arg) {
                System.out.println("father choose qq");
            }
    
            public void hardChoice(_360 arg) {
                System.out.println("father choose 360");
            }
        }
    
        public static class Son extends Father {
            public void hardChoice(QQ arg) {
                System.out.println("son choose qq");
            }
    
            public void hardChoice(_360 arg) {
                System.out.println("son choose 360");
            }
        }
    
        public static void main(String[] args) {
            Father father = new Father();
            Father son = new Son();
            father.hardChoice(new _360());
            son.hardChoice(new QQ());
        }
    }
    ```
    输出结果如下：
    ```text
    father choose 360
    son choose qq
    ```
    字节码指令如下所示：
    ```text
    public static void main(java.lang.String[]);
      Code:
       Stack=3, Locals=3, Args_size=1
       0:   new     #2; //class Dispatcher$Father
       3:   dup
       4:   invokespecial   #3; //Method Dispatcher$Father."<init>":()V
       7:   astore_1
       8:   new     #4; //class Dispatcher$Son
       11:  dup
       12:  invokespecial   #5; //Method Dispatcher$Son."<init>":()V
       15:  astore_2
       16:  aload_1
       17:  new     #6; //class Dispatcher$_360
       20:  dup
       21:  invokespecial   #7; //Method Dispatcher$_360."<init>":()V
       24:  invokevirtual   #8; //Method Dispatcher$Father.hardChoice:(LDispatcher$_360;)V
       27:  aload_2
       28:  new     #9; //class Dispatcher$QQ
       31:  dup
       32:  invokespecial   #10; //Method Dispatcher$QQ."<init>":()V
       35:  invokevirtual   #11; //Method Dispatcher$Father.hardChoice:(LDispatcher$QQ;)V
       38:  return
    ```
    1. 来看看编译阶段编译器的选择过程，即静态分派过程。
        1. 首先确定方法的接收者，从上面的字节码指令中可以看到，两次方法调用
            ```text
            father.hardChoice(new _360());
            son.hardChoice(new QQ());
            ```
            对应的字节码指令都是一样的，只是参数不同而已
            ```text
            24:  invokevirtual   #8; //Method Dispatcher$Father.hardChoice:(LDispatcher$_360;)V
            35:  invokevirtual   #11; //Method Dispatcher$Father.hardChoice:(LDispatcher$QQ;)V
            ```
            由此可见，在class文件中都是调用Father的hardChoice()方法。(上面的8.2.2.2 动态分派种的例子，man和women调用的也都是Human.sayHello())
        2. 然后对于方法参数，一个是_360对象，一个是QQ对象，按照静态类型匹配的原则，自然找到各自的方法。
        
        >上面的两步都是在**编译器中**做出的，属于**静态分派**，在选择目标方法时根据了**两个宗量**，是**多分派**的。因此，**静态分派属于多分派类型**。 
    2. 来看看运行阶段，虚拟机的选择，即动态分派的过程。
        1. 当java执行时，当执行到`son.hardChoice(new QQ());`，`aload_2`指令发现son的实际类型是Son（第15句压入操作数栈的son的引用），因此会调用Son类中的方法。
        2. 在执行`father.hardChoice(new _360());`时也有这个过程，只不过father的实际类型就是Father而已。
        
        >在目标选择时只依据了**一个宗量**，是**单分派**的。因此，**动态分派属于单分派类型**。

#####  8.2.2.4 虚拟机动态分派的实现

### 8.3 基于栈的字节码解释执行引擎
#### 8.3.1 解释执行
#### 8.3.2 基于栈的指令集与基于寄存器的指令集
#### 8.3.3 基于栈的解释器执行过程

## 参考资料

>1. 深入理解Java虚拟机第八章
>
>2. [栈帧、局部变量表、操作数栈](https://blog.csdn.net/a616413086/article/category/6205912)
>
>3. [java方法调用之单分派与多分派（二）](https://blog.csdn.net/fan2012huan/article/details/51004615)