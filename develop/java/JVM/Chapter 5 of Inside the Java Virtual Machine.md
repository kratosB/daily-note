## Chapter 5 of [Inside the Java Virtual Machine](https://www.artima.com/insidejvm/ed2/index.html)
# [The Java Virtual Machine](https://www.artima.com/insidejvm/ed2/jvm.html)

1. [What is a Java Virtual Machine?](https://github.com/kratosB/daily-note/blob/master/develop/java/JVM/Chapter%205%20of%20Inside%20the%20Java%20Virtual%20Machine.md#what-is-a-java-virtual-machine)
2. [The Lifetime of a Java Virtual Machine](https://github.com/kratosB/daily-note/blob/master/develop/java/JVM/Chapter%205%20of%20Inside%20the%20Java%20Virtual%20Machine.md#the-lifetime-of-a-java-virtual-machine)
3. [The Architecture of the Java Virtual Machine](https://github.com/kratosB/daily-note/blob/master/develop/java/JVM/Chapter%205%20of%20Inside%20the%20Java%20Virtual%20Machine.md#the-architecture-of-the-java-virtual-machine)
    1. [Data Types](https://github.com/kratosB/daily-note/blob/master/develop/java/JVM/Chapter%205%20of%20Inside%20the%20Java%20Virtual%20Machine.md#data-types)
    2. [Word Size](https://github.com/kratosB/daily-note/blob/master/develop/java/JVM/Chapter%205%20of%20Inside%20the%20Java%20Virtual%20Machine.md#word-size)
    3. [The Class Loader Subsystem](https://github.com/kratosB/daily-note/blob/master/develop/java/JVM/Chapter%205%20of%20Inside%20the%20Java%20Virtual%20Machine.md#the-class-loader-subsystem)
    4. [The Method Area](https://github.com/kratosB/daily-note/blob/master/develop/java/JVM/Chapter%205%20of%20Inside%20the%20Java%20Virtual%20Machine.md#the-method-area)
    5. [The Heap](https://github.com/kratosB/daily-note/blob/master/develop/java/JVM/Chapter%205%20of%20Inside%20the%20Java%20Virtual%20Machine.md#the-heap)
    6. [The Program Counter](https://github.com/kratosB/daily-note/blob/master/develop/java/JVM/Chapter%205%20of%20Inside%20the%20Java%20Virtual%20Machine.md#the-program-counter)
    7. [The Java Stack](https://github.com/kratosB/daily-note/blob/master/develop/java/JVM/Chapter%205%20of%20Inside%20the%20Java%20Virtual%20Machine.md#the-java-stack)
    8. [The Stack Frame](https://github.com/kratosB/daily-note/blob/master/develop/java/JVM/Chapter%205%20of%20Inside%20the%20Java%20Virtual%20Machine.md#the-stack-frame)
    9. [Native Method Stacks](https://github.com/kratosB/daily-note/blob/master/develop/java/JVM/Chapter%205%20of%20Inside%20the%20Java%20Virtual%20Machine.md#native-method-stacks)
    10. [Execution Engine](https://github.com/kratosB/daily-note/blob/master/develop/java/JVM/Chapter%205%20of%20Inside%20the%20Java%20Virtual%20Machine.md#execution-engine)
    11. [Native Method Interface](https://github.com/kratosB/daily-note/blob/master/develop/java/JVM/Chapter%205%20of%20Inside%20the%20Java%20Virtual%20Machine.md#native-method-interface)
4. [The Real Machine](https://github.com/kratosB/daily-note/blob/master/develop/java/JVM/Chapter%205%20of%20Inside%20the%20Java%20Virtual%20Machine.md#the-real-machine)
5. [Eternal Math: A Simulation](https://github.com/kratosB/daily-note/blob/master/develop/java/JVM/Chapter%205%20of%20Inside%20the%20Java%20Virtual%20Machine.md#eternal-math-a-simulation)
6. [On the CD-ROM](https://github.com/kratosB/daily-note/blob/master/develop/java/JVM/Chapter%205%20of%20Inside%20the%20Java%20Virtual%20Machine.md#on-the-cd-rom)
7. [The Resources Page](https://github.com/kratosB/daily-note/blob/master/develop/java/JVM/Chapter%205%20of%20Inside%20the%20Java%20Virtual%20Machine.md#the-resources-page)

## [What is a Java Virtual Machine?](https://www.artima.com/insidejvm/ed2/jvm.html)

To understand the Java virtual machine you must first be aware that you may be talking about any of three different things when you say "Java virtual machine." You may be speaking of:

* the abstract specification,
* a concrete implementation, or
* a runtime instance.

The abstract specification is a concept, described in detail in the book: _The Java Virtual Machine Specification_, by Tim Lindholm and Frank Yellin. Concrete implementations, which exist on many platforms and come from many vendors, are either all software or a combination of hardware and software. A runtime instance hosts a single running Java application.

Each Java application runs inside a runtime instance of some concrete implementation of the abstract specification of the Java virtual machine. In this book, the term "Java virtual machine" is used in all three of these senses. Where the intended sense is not clear from the context, one of the terms "specification," "implementation," or "instance" is added to the term "Java virtual machine".

---
## [The Lifetime of a Java Virtual Machine](https://www.artima.com/insidejvm/ed2/jvm.html)

A runtime instance of the Java virtual machine has a clear mission in life: to run one Java application. When a Java application starts, a runtime instance is born. When the application completes, the instance dies. If you start three Java applications at the same time, on the same computer, using the same concrete implementation, you'll get three Java virtual machine instances. Each Java application runs inside its own Java virtual machine.

A Java virtual machine instance starts running its solitary application by invoking the main() method of some initial class. The main() method must be public, static, return void, and accept one parameter: a String array. Any class with such a main() method can be used as the starting point for a Java application.
>虚拟机实例通过调用main()方法来启动，main方法必须是public，static，void，而且还要有一个String[]类型的参数。任何一个这种main()都可以作为程序入口。

For example, consider an application that prints out its command line arguments:

    // On CD-ROM in file jvm/ex1/Echo.java
    class Echo {
    
        public static void main(String[] args) {
            int len = args.length;
            for (int i = 0; i < len; ++i) {
                System.out.print(args[i] + " ");
            }
            System.out.println();
        }
    }

You must in some implementation-dependent way give a Java virtual machine the name of the initial class that has the main() method that will start the entire application. One real world example of a Java virtual machine implementation is the java program from Sun's Java 2 SDK. If you wanted to run the Echo application using Sun's java on Window98, for example, you would type in a command such as:

    java Echo Greetings, Planet.
    
The first word in the command, "java," indicates that the Java virtual machine from Sun's Java 2 SDK should be run by the operating system. The second word, "Echo," is the name of the initial class. Echo must have a public static method named main() that returns void and takes a String array as its only parameter. The subsequent words, "Greetings, Planet.," are the command line arguments for the application. These are passed to the main() method in the String array in the order in which they appear on the command line. So, for the previous example, the contents of the String array passed to main in Echo are: arg[0] is "Greetings," arg[1] is "Planet."

The main() method of an application's initial class serves as the starting point for that application's initial thread. The initial thread can in turn fire off other threads.

Inside the Java virtual machine, threads come in two flavors: daemon and non- daemon. A daemon thread is ordinarily a thread used by the virtual machine itself, such as a thread that performs garbage collection. The application, however, can mark any threads it creates as daemon threads. The initial thread of an application--the one that begins at main()--is a non- daemon thread.

A Java application continues to execute (the virtual machine instance continues to live) as long as any non-daemon threads are still running. When all non-daemon threads of a Java application terminate, the virtual machine instance will exit. If permitted by the security manager, the application can also cause its own demise by invoking the exit() method of class Runtime or System.
>只要有任意一个非守护线程还在运行，java应用就会继续执行。当所有非守护线程都结束了，虚拟机实例会退出。

In the Echo application previous, the main() method doesn't invoke any other threads. After it prints out the command line arguments, main() returns. This terminates the application's only non-daemon thread, which causes the virtual machine instance to exit.

---
## [The Architecture of the Java Virtual Machine](https://www.artima.com/insidejvm/ed2/jvm2.html)

In the Java virtual machine specification, the behavior of a virtual machine instance is described in terms of subsystems, memory areas, data types, and instructions. These components describe an abstract inner architecture for the abstract Java virtual machine. The purpose of these components is not so much to dictate an inner architecture for implementations. It is more to provide a way to strictly define the external behavior of implementations. The specification defines the required behavior of any Java virtual machine implementation in terms of these abstract components and their interactions.
>在Java虚拟机规范中，虚拟机实例的行为被定义为子系统，内存区域，数据类型和指令。

Figure 5-1 shows a block diagram of the Java virtual machine that includes the major subsystems and memory areas described in the specification. As mentioned in previous chapters, each Java virtual machine has a class loader subsystem: a mechanism for loading types (classes and interfaces) given fully qualified names. Each Java virtual machine also has an execution engine: a mechanism responsible for executing the instructions contained in the methods of loaded classes.

![The internal architecture of the Java virtual machine](https://www.artima.com/insidejvm/ed2/images/fig5-1.gif "The internal architecture of the Java virtual machine")

**Figure 5-1. The internal architecture of the Java virtual machine.**

When a Java virtual machine runs a program, it needs memory to store many things, including bytecodes and other information it extracts from loaded class files, objects the program instantiates, parameters to methods, return values, local variables, and intermediate results of computations. The Java virtual machine organizes the memory it needs to execute a program into several runtime data areas.
>当Java虚拟机运行一个程序的时候，它需要内存，用来存储很多东西，包括从加载的类文件中提取的字节码和其他信息，程序实例化的对象，方法的参数，返回值，局部变量和计算的中间结果。Java虚拟机将执行程序所需的内存组织到多个运行时数据区域中。

Although the same runtime data areas exist in some form in every Java virtual machine implementation, their specification is quite abstract. Many decisions about the structural details of the runtime data areas are left to the designers of individual implementations.

Different implementations of the virtual machine can have very different memory constraints. Some implementations may have a lot of memory in which to work, others may have very little. Some implementations may be able to take advantage of virtual memory, others may not. The abstract nature of the specification of the runtime data areas helps make it easier to implement the Java virtual machine on a wide variety of computers and devices.

Some runtime data areas are shared among all of an application's threads and others are unique to individual threads. Each instance of the Java virtual machine has one method area and one heap. These areas are shared by all threads running inside the virtual machine. When the virtual machine loads a class file, it parses information about a type from the binary data contained in the class file. It places this type information into the method area. As the program runs, the virtual machine places all objects the program instantiates onto the heap. See Figure 5-2 for a graphical depiction of these memory areas.
>有些运行时数据区被所有线程共享，有一些则只属于每个线程自己。每个Java虚拟机都会有一个方法区和一个堆。这些数据区被所有该虚拟机内运行的线程共享。
>
>当虚拟机加载一个class文件时，它会解析包含在这个class文件中的二进制数据的类型（类或接口）信息。它将此类型（类或接口）信息放入方法区域。 当程序运行时，虚拟机将程序实例化的所有对象放置到堆上。

![Runtime data areas shared among all threads](https://www.artima.com/insidejvm/ed2/images/fig5-2.gif "Runtime data areas shared among all threads")

**Figure 5-2. Runtime data areas shared among all threads.**

As each new thread comes into existence, it gets its own pc register (program counter) and Java stack. If the thread is executing a Java method (not a native method), the value of the pc register indicates the next instruction to execute. A thread's Java stack stores the state of Java (not native) method invocations for the thread. The state of a Java method invocation includes its local variables, the parameters with which it was invoked, its return value (if any), and intermediate calculations. The state of native method invocations is stored in an implementation-dependent way in native method stacks, as well as possibly in registers or other implementation-dependent memory areas.
>伴随着每个新线程的出现，它会有一个他自己的pc寄存器（程序计数器）和Java堆栈。如果线程在执行一个Java方法（不是本地方法），那么pc寄存器（程序计数器）的值表示下一条要执行的指令。该线程的Java堆栈存储了方法调用（非本地方法）的状态。方法调用的状态包括局部变量，调用的参数，返回值（如果有），以及中间计算。本机方法调用的状态以依赖于实现的方式存储在本机方法堆栈中，也可能存储在寄存器或其他依赖于实现的存储区域中。

The Java stack is composed of stack frames (or frames). A stack frame contains the state of one Java method invocation. When a thread invokes a method, the Java virtual machine pushes a new frame onto that thread's Java stack. When the method completes, the virtual machine pops and discards the frame for that method.
>Java堆栈由栈帧组成。一个栈帧包含一个Java方法调用的状态。当一个线程调用一个方法，虚拟机会push一个新的栈帧到该线程的Java堆栈中，当方法结束的时候，虚拟机会pop并丢弃这个方法对应的栈帧。

The Java virtual machine has no registers to hold intermediate data values. The instruction set uses the Java stack for storage of intermediate data values.This approach was taken by Java's designers to keep the Java virtual machine's instruction set compact and to facilitate implementation on architectures with few or irregular general purpose registers. In addition, the stack-based architecture of the Java virtual machine's instruction set facilitates the code optimization work done by just-in-time and dynamic compilers that operate at run-time in some virtual machine implementations.
>Java虚拟机没有用于保存中间数据值的寄存器。 指令集使用Java堆栈存储中间数据值。这个方法被Java开发者用来保持Java虚拟机的指令集更紧凑，并便于在具有少量或不规则通用寄存器的体系结构上实现。除此之外，Java虚拟机指令集的这种基于栈的体系结构，有助于在有些虚拟机实现中的实时和动态编译的代码精简工作的完成。

See Figure 5-3 for a graphical depiction of the memory areas the Java virtual machine creates for each thread. These areas are private to the owning thread. No thread can access the pc register or Java stack of another thread.
>图5-3是Java虚拟机为每个线程创建内存区域的图。这些内存区市每个线程私有的，没有线程能访问其他线程的程序计数器和Java堆栈。

![Runtime data areas exclusive to each thread](https://www.artima.com/insidejvm/ed2/images/fig5-3.gif "Runtime data areas exclusive to each thread")

**Figure 5-3. Runtime data areas exclusive to each thread.**

Figure 5-3 shows a snapshot of a virtual machine instance in which three threads are executing. At the instant of the snapshot, threads one and two are executing Java methods. Thread three is executing a native method.
>图5-3展示了一个“Java虚拟机实例执行3个线程”的切片。在这个切片中，线程1和线程2在执行Java方法，线程3在执行本地方法。

In Figure 5-3, as in all graphical depictions of the Java stack in this book, the stacks are shown growing downwards. The "top" of each stack is shown at the bottom of the figure. Stack frames for currently executing methods are shown in a lighter shade. For threads that are currently executing a Java method, the pc register indicates the next instruction to execute. In Figure 5-3, such pc registers (the ones for threads one and two) are shown in a lighter shade. Because thread three is currently executing a native method, the contents of its pc register--the one shown in dark gray--is undefined.
>在图5-3中，与本书中Java堆栈的所有图形描述一样，栈显示为向下增长。 每个栈的“顶部”显示在图的底部。 当前执行方法的栈帧以较浅的阴影显示。 对于当前正在执行Java方法的线程，pc寄存器指示要执行的下一条指令。 在图5-3中，这些pc寄存器（线程1和2的寄存器）以较浅的阴影显示。 由于线程3当前正在执行本机方法，因此其pc寄存器的内容（以深灰色显示的内容）未定义。

---
### [Data Types](https://www.artima.com/insidejvm/ed2/jvm3.html)

The Java virtual machine computes by performing operations on certain types of data. Both the data types and operations are strictly defined by the Java virtual machine specification. The data types can be divided into a set of primitive types and a reference type. Variables of the primitive types hold primitive values, and variables of the reference type hold reference values. Reference values refer to objects, but are not objects themselves. Primitive values, by contrast, do not refer to anything. They are the actual data themselves. You can see a graphical depiction of the Java virtual machine's families of data types in Figure 5-4.
>Java虚拟机通过对某些类型的数据执行操作来计算。数据类型和操作都由Java虚拟机规范严格定义。数据类型被分为基本类型和引用类型。基本类型不指向其他东西，引用类型指向其他对象。

![Data types of the Java virtual machine](https://www.artima.com/insidejvm/ed2/images/fig5-4.gif "Data types of the Java virtual machine")

**Figure 5-4. Data types of the Java virtual machine.**

All the primitive types of the Java programming language are primitive types of the Java virtual machine. Although boolean qualifies as a primitive type of the Java virtual machine, the instruction set has very limited support for it. When a compiler translates Java source code into bytecodes, it uses ints or bytes to represent booleans. In the Java virtual machine, false is represented by integer zero and true by any non-zero integer. Operations involving boolean values use ints. Arrays of boolean are accessed as arrays of byte, though they may be represented on the heap as arrays of byte or as bit fields.
>Java语言的基本类型和Java虚拟机的基本类型是一样的。不过boolean虽然是基本类型，但是指令集对它的支持很有限。当编译器把Java代码翻译成字节码的时候，boolean汇呗表示成二进制数或int数。在Java虚拟机中，false就是int0，true是非0的int。

The primitive types of the Java programming language other than boolean form the numeric types of the Java virtual machine. The numeric types are divided between the integral types: byte, short, int, long, and char, and the floating- point types: float and double. As with the Java programming language, the primitive types of the Java virtual machine have the same range everywhere. A long in the Java virtual machine always acts like a 64-bit signed twos complement number, independent of the underlying host platform.
>除了boolean之外的数字型基本类型，分为整数类（int）: byte, short, int, long, char和浮点类（flout）: float, double。

The Java virtual machine works with one other primitive type that is unavailable to the Java programmer: the returnAddress type. This primitive type is used to implement finally clauses of Java programs. The use of the returnAddress type is described in detail in Chapter 18, "Finally Clauses."
>Java虚拟机中还由一种基本类型，是returnAddress，这种类型是用来实现finally条款的，在第十八章`Finally Clauses`中会介绍到。（电子版问当没有这个第十八章，只有目录没有内容）

The reference type of the Java virtual machine is cleverly named reference. Values of type reference come in three flavors: the class type, the interface type, and the array type. All three types have values that are references to dynamically created objects. The class type's values are references to class instances. The array type's values are references to arrays, which are full-fledged objects in the Java virtual machine. The interface type's values are references to class instances that implement an interface. One other reference value is the null value, which indicates the reference variable doesn't refer to any object.
>Java虚拟机的引用类型巧妙地命名为引用。引用类型的值有三种类型：类引用，接口引用，数组引用。数组类型的值是对数组的引用，数组是Java虚拟机中的完整对象。接口类型的值是实现这个接口的类的实例。另一个参考值是空值，表示引用变量不引用任何对象。

The Java virtual machine specification defines the range of values for each of the data types, but does not define their sizes. The number of bits used to store each data type value is a decision of the designers of individual implementations. The ranges of the Java virtual machines data type's are shown in Table 5-1. More information on the floating point ranges is given in Chapter 14, "Floating Point Arithmetic."
>Java虚拟机规范定义了各种数据类型的值的范围，但是没定义大小。具体大小取决于不同虚拟机得实现者。下面的表格5-1就是各种数据类型的范围，具体信息可以在第十四章`Floating Point Arithmetic`中找到（电子版问当没有这个第十八章，只有目录没有内容）

|Type|Range|
| ------ | ------ |
| byte | 8-bit signed two's complement integer (-27 to 27 - 1, inclusive) |
| short | 16-bit signed two's complement integer (-215 to 215 - 1, inclusive) |
| int | 32-bit signed two's complement integer (-231 to 231 - 1, inclusive) |
| long | 64-bit signed two's complement integer (-263 to 263 - 1, inclusive) |
| char | 16-bit unsigned Unicode character (0 to 216 - 1, inclusive) |
| float | 32-bit IEEE 754 single-precision float |
| double | 64-bit IEEE 754 double-precision float |
| returnAddress | address of an opcode within the same method |
| reference | reference to an object on the heap, or null |

---
### [Word Size](https://www.artima.com/insidejvm/ed2/jvm3.html)

The basic unit of size for data values in the Java virtual machine is the word--a fixed size chosen by the designer of each Java virtual machine implementation. The word size must be large enough to hold a value of type byte, short, int, char, float, returnAddress, or reference. Two words must be large enough to hold a value of type long or double. An implementation designer must therefore choose a word size that is at least 32 bits, but otherwise can pick whatever word size will yield the most efficient implementation. The word size is often chosen to be the size of a native pointer on the host platform.
>Java虚拟机的数据的基本大小单位是word，一个由虚拟机实现的开发者自定义的固定的大小。一个word的大小必须要足够放下 byte, short, int, char, float, returnAddress, 或者 reference。两个word的大小必须要足够放下 long 或者 double。所以开发者需要选区最小32bits的大小作为word的大小，通常word的大小取决于本地指针大小。

The specification of many of the Java virtual machine's runtime data areas are based upon this abstract concept of a word. For example, two sections of a Java stack frame--the local variables and operand stack-- are defined in terms of words. These areas can contain values of any of the virtual machine's data types. When placed into the local variables or operand stack, a value occupies either one or two words.

As they run, Java programs cannot determine the word size of their host virtual machine implementation. The word size does not affect the behavior of a program. It is only an internal attribute of a virtual machine implementation.

---
### [The Class Loader Subsystem](https://www.artima.com/insidejvm/ed2/jvm4.html)

The part of a Java virtual machine implementation that takes care of finding and loading types is the class loader subsystem. Chapter 1, "Introduction to Java's Architecture," gives an overview of this subsystem. Chapter 3, "Security," shows how the subsystem fits into Java's security model. This chapter describes the class loader subsystem in more detail and show how it relates to the other components of the virtual machine's internal architecture.
>这一章更详细地描述了类加载器子系统，并展示了它与虚拟机内部体系结构的其他组件的关系。

As mentioned in Chapter 1, the Java virtual machine contains two kinds of class loaders: a bootstrap class loader and user-defined class loaders. The bootstrap class loader is a part of the virtual machine implementation, and user-defined class loaders are part of the running Java application. Classes loaded by different class loaders are placed into separate name spaces inside the Java virtual machine.
>在第一章中介绍过，Java虚拟机包含两种类加载器，引导类加载器和用户定义的类加载器。引导类加载器是Java虚拟机地一部分，用户定义类加载器是运行地Java应用地一部分。由不同类加载器加载的类被放置在Java虚拟机内的单独名称空间中。

The class loader subsystem involves many other parts of the Java virtual machine and several classes from the java.lang library. For example, user-defined class loaders are regular Java objects whose class descends from java.lang.ClassLoader. The methods of class ClassLoader allow Java applications to access the virtual machine's class loading machinery. Also, for every type a Java virtual machine loads, it creates an instance of class java.lang.Class to represent that type. Like all objects, user-defined class loaders and instances of class Class reside on the heap. Data for loaded types resides in the method area.
>类加载器子系统涉及Java虚拟机的许多其他部分以及java.lang库中的几个类。例如，用户定义地类加载器是普通Java对象，是java.lang.ClassLoader地后代。ClassLoader的方法允许Java应用访问虚拟机的类加载机制。此外，对于Java虚拟机加载的每种类型（类或接口），它都会创建类java.lang.Class的实例来表示该类型（类或接口）。与所有对象一样，用户定义的类加载器和类Class的实例放在堆上。加载类型（类或接口）的代码数据放在方法区域中。

#### Loading, Linking and Initialization

The class loader subsystem is responsible for more than just locating and importing the binary data for classes. It must also verify the correctness of imported classes, allocate and initialize memory for class variables, and assist in the resolution of symbolic references. These activities are performed in a strict order:
>这个类加载子系统不但负责定位并输入类的二进制数据，而且验证输入的类的正确性，为类的变量初始化并分配内存，并在解析上协助符号引用。这些活动按严格的顺序执行：

1. Loading: finding and importing the binary data for a type
2. Linking: performing verification, preparation, and (optionally) resolution
    1. Verification: ensuring the correctness of the imported type
    2. Preparation: allocating memory for class variables and initializing the memory to default values
    3. Resolution: transforming symbolic references from the type into direct references.
3. Initialization: invoking Java code that initializes class variables to their proper starting values.
>加载：查找并导入类型（类或接口）的二进制数据
>
>链接：执行验证，准备，和（可选）解析
>
>>验证：确认导入的类型的正确性
>>
>>准备：为类的变量分配内存，并且初始化内存为默认值
>>
>>解析：将`符号引用`从类型转换为`直接引用`。
>
>初始化：调用Java代码，将类变量初始化为正确的起始值。

The details of these processes are given Chapter 7, "The Lifetime of a Type."
>具体细节在第七章`The Lifetime of a Type`可以找到

#### The Bootstrap Class Loader

Java virtual machine implementations must be able to recognize and load classes and interfaces stored in binary files that conform to the Java class file format. An implementation is free to recognize other binary forms besides class files, but it must recognize class files.
>Java虚拟机的实现，必须要能够识别并加载存在（符合Java类文件格式的）二进制文件里的类和接口。

Every Java virtual machine implementation has a bootstrap class loader, which knows how to load trusted classes, including the classes of the Java API. The Java virtual machine specification doesn't define how the bootstrap loader should locate classes. That is another decision the specification leaves to implementation designers.
>每个Java虚拟机实现，都有一个bootstrap类加载器，它知道如何加载可信赖的类，包括Java API的类。

Given a fully qualified type name, the bootstrap class loader must in some way attempt to produce the data that defines the type. One common approach is demonstrated by the Java virtual machine implementation in Sun's 1.1 JDK on Windows98. This implementation searches a user-defined directory path stored in an environment variable named CLASSPATH. The bootstrap loader looks in each directory, in the order the directories appear in the CLASSPATH, until it finds a file with the appropriate name: the type's simple name plus ".class". Unless the type is part of the unnamed package, the bootstrap loader expects the file to be in a subdirectory of one the directories in the CLASSPATH. The path name of the subdirectory is built from the package name of the type. For example, if the bootstrap class loader is searching for class java.lang.Object, it will look for Object.class in the java\lang subdirectory of each CLASSPATH directory.

In 1.2, the bootstrap class loader of Sun's Java 2 SDK only looks in the directory in which the system classes (the class files of the Java API) were installed. The bootstrap class loader of the implementation of the Java virtual machine from Sun's Java 2 SDK does not look on the CLASSPATH. In Sun's Java 2 SDK virtual machine, searching the class path is the job of the system class loader, a user-defined class loader that is created automatically when the virtual machine starts up. More information on the class loading scheme of Sun's Java 2 SDK is given in Chapter 8, "The Linking Model."

#### User-Defined Class Loaders

Although user-defined class loaders themselves are part of the Java application, four of the methods in class ClassLoader are gateways into the Java virtual machine:
>虽然用户定义的类加载器本身是Java应用程序的一部分，但类ClassLoader中的四个方法是进入Java虚拟机的门户（网关）：

    // Four of the methods declared in class java.lang.ClassLoader:
    protected final Class defineClass(String name, byte data[], int offset, int length);
    protected final Class defineClass(String name, byte data[], int offset, int length, ProtectionDomain protectionDomain);
    protected final Class findSystemClass(String name);
    protected final void resolveClass(Class c);

Any Java virtual machine implementation must take care to connect these methods of class ClassLoader to the internal class loader subsystem.
>任何Java虚拟机实现都必须注意将类ClassLoader的这些方法连接到内部类加载器子系统。

The two overloaded defineClass() methods accept a byte array, data[], as input. Starting at position offset in the array and continuing for length bytes, class ClassLoader expects binary data conforming to the Java class file format--binary data that represents a new type for the running application -- with the fully qualified name specified in name. The type is assigned to either a default protection domain, if the first version of defineClass() is used, or to the protection domain object referenced by the protectionDomain parameter. Every Java virtual machine implementation must make sure the defineClass() method of class ClassLoader can cause a new type to be imported into the method area.
>没仔细看

The findSystemClass() method accepts a String representing a fully qualified name of a type. When a user-defined class loader invokes this method in version 1.0 and 1.1, it is requesting that the virtual machine attempt to load the named type via its bootstrap class loader. If the bootstrap class loader has already loaded or successfully loads the type, it returns a reference to the Class object representing the type. If it can't locate the binary data for the type, it throws ClassNotFoundException. In version 1.2, the findSystemClass() method attempts to load the requested type from the system class loader. Every Java virtual machine implementation must make sure the findSystemClass() method can invoke the bootstrap (if version 1.0 or 1.1) or system (if version 1.2 or later) class loader in this way.
>没仔细看

The resolveClass() method accepts a reference to a Class instance. This method causes the type represented by the Class instance to be linked (if it hasn't already been linked). The defineClass() method, described previous, only takes care of loading. (See the previous section, "Loading, Linking, and Initialization" for definitions of these terms.) When defineClass() returns a Class instance, the binary file for the type has definitely been located and imported into the method area, but not necessarily linked and initialized. Java virtual machine implementations make sure the resolveClass() method of class ClassLoader can cause the class loader subsystem to perform linking.
>没仔细看

The details of how a Java virtual machine performs class loading, linking, and initialization, with user- defined class loaders is given in Chapter 8, "The Linking Model."
>Java虚拟机通过用户定义的类加载器执行类加载，链接，初始化类的细节，在第八章`The Linking Model.`。

#### Name Spaces （命名空间）

As mentioned in Chapter 3, "Security," each class loader maintains its own name space populated by the types it has loaded. Because each class loader has its own name space, a single Java application can load multiple types with the same fully qualified name. A type's fully qualified name, therefore, is not always enough to uniquely identify it inside a Java virtual machine instance. If multiple types of that same name have been loaded into different name spaces, the identity of the class loader that loaded the type (the identity of the name space it is in) will also be needed to uniquely identify that type.
>没仔细看

Name spaces arise inside a Java virtual machine instance as a result of the process of resolution. As part of the data for each loaded type, the Java virtual machine keeps track of the class loader that imported the type. When the virtual machine needs to resolve a symbolic reference from one class to another, it requests the referenced class from the same class loader that loaded the referencing class. This process is described in detail in Chapter 8, "The Linking Model."
>没仔细看

---
### [The Method Area](https://www.artima.com/insidejvm/ed2/jvm5.html)

Inside a Java virtual machine instance, information about loaded types is stored in a logical area of memory called the method area. When the Java virtual machine loads a type, it uses a class loader to locate the appropriate class file. The class loader reads in the class file--a linear stream of binary data--and passes it to the virtual machine. The virtual machine extracts information about the type from the binary data and stores the information in the method area. Memory for class (static) variables declared in the class is also taken from the method area.
>在Java虚拟机实例中，有关已加载类型（类或接口）的信息存储在称为方法区域的内存的逻辑区域中。当虚拟机加载类型（类或接口）时，它使用类加载器定位相应的class文件。类加载器读取class文件的信息-一段线性的字节流数据，然后传给虚拟机。虚拟机从字节数据中提取类型（类或接口）信息，并把这些信息存在方法区。类中声明的（静态）变量的内存也在方法区中。

The manner in which a Java virtual machine implementation represents type information internally is a decision of the implementation designer. For example, multi-byte quantities in class files are stored in big- endian (most significant byte first) order. When the data is imported into the method area, however, a virtual machine can store the data in any manner. If an implementation sits on top of a little-endian processor, the designers may decide to store multi-byte values in the method area in little-endian order.
>不同的虚拟机实现，可以有不同的内部表出现类型（类或接口）信息的方式。

The virtual machine will search through and use the type information stored in the method area as it executes the application it is hosting. Designers must attempt to devise data structures that will facilitate speedy execution of the Java application, but must also think of compactness. If designing an implementation that will operate under low memory constraints, designers may decide to trade off some execution speed in favor of compactness. If designing an implementation that will run on a virtual memory system, on the other hand, designers may decide to store redundant information in the method area to facilitate execution speed. (If the underlying host doesn't offer virtual memory, but does offer a hard disk, designers could create their own virtual memory system as part of their implementation.) Designers can choose whatever data structures and organization they feel optimize their implementations performance, in the context of its requirements.

All threads share the same method area, so access to the method area's data structures must be designed to be thread-safe. If two threads are attempting to find a class named Lava, for example, and Lava has not yet been loaded, only one thread should be allowed to load it while the other one waits.
>所有线程共享同一个方法区，所以方法区数据需要被设计成线程安全的。如果两个线程都尝试去加载一个类，那么已更改只有一个线程的加载可以被允许，另一个需要等待。

The size of the method area need not be fixed. As the Java application runs, the virtual machine can expand and contract the method area to fit the application's needs. Also, the memory of the method area need not be contiguous. It could be allocated on a heap--even on the virtual machine's own heap. Implementations may allow users or programmers to specify an initial size for the method area, as well as a maximum or minimum size.
>方法区的大小不需要是固定的。当一个Java应用启动，虚拟机会扩展或收缩方法区的大小，来适应Java应用的需求。同时，方法区的内存不需要是连续的。它可以在堆上分配 - 甚至在虚拟机自己的堆上。 它允许用户或程序员指定方法区域的初始大小，以及最大或最小大小。

The method area can also be garbage collected. Because Java programs can be dynamically extended via user-defined class loaders, classes can become "unreferenced" by the application. If a class becomes unreferenced, a Java virtual machine can unload the class (garbage collect it) to keep the memory occupied by the method area at a minimum. The unloading of classes--including the conditions under which a class can become "unreferenced"--is described in Chapter 7, "The Lifetime of a Type."
>方法区也可以被垃圾回收。具体见第七章`The Lifetime of a Type.`

#### Type Information

For each type it loads, a Java virtual machine must store the following kinds of information in the method area:
>每加载一个类型（类或接口），Java虚拟机要在方法区中存储如下信息：
1. The fully qualified name of the type
2. The fully qualified name of the type's direct superclass (unless the type is an interface or class java.lang.Object, neither of which have a superclass)
3. Whether or not the type is a class or an interface
4. The type's modifiers ( some subset of` public, abstract, final)
5. An ordered list of the fully qualified names of any direct superinterfaces

Inside the Java class file and Java virtual machine, type names are always stored as fully qualified names. In Java source code, a fully qualified name is the name of a type's package, plus a dot, plus the type's simple name. For example, the fully qualified name of class Object in package java.lang is java.lang.Object. In class files, the dots are replaced by slashes, as in java/lang/Object. In the method area, fully qualified names can be represented in whatever form and data structures a designer chooses.
>在Java类文件和Java虚拟机中，类型（类或接口）名称始终存储为完全限定名称。例如java.lang中的Object，他的完全限定名称是java.lang.Object。

In addition to the basic type information listed previously, the virtual machine must also store for each loaded type:
>除了上面说的那些类型（类或接口）的基本信息，虚拟机还需要存储如下信息（在方法区）
1. The constant pool for the type（常量池）
2. Field information（字段信息）
3. Method information（方法信息）
4. All class (static) variables declared in the type, except constants（除了常量的所有（静态）变量）
5. A reference to class ClassLoader（类加载器的引用）
6. A reference to class Class（类的引用）

This data is described in the following sections.

#### The Constant Pool

For each type it loads, a Java virtual machine must store a constant pool. A constant pool is an ordered set of constants used by the type, including literals (string, integer, and floating point constants) and symbolic references to types, fields, and methods. Entries in the constant pool are referenced by index, much like the elements of an array. Because it holds symbolic references to all types, fields, and methods used by a type, the constant pool plays a central role in the dynamic linking of Java programs. The constant pool is described in more detail later in this chapter and in Chapter 6, "The Java Class File."
>常量池是一个有序的常量集，包括文字（字符串，整数，浮点常量），类型/字段/方法的符号引用。常量池中的数据跟数组很相似，是由索引来引用的。因为常量池包含所有类型/字段/方法的符号引用，所以常量池在Java程序的动态链接中起着核心作用。 常量池将在本章后面和第6章`The Java Class File.`中详细介绍。
>> 常量池主要用于存放两大类常量：字面量(Literal)和符号引用量(Symbolic References)，字面量很好理解，符号引用就是1. 类和接口的全限定名。 2. 字段名称和描述符。 3. 方法名称和描述符。

#### Field Information

For each field declared in the type, the following information must be stored in the method area. In addition to the information for each field, the order in which the fields are declared by the class or interface must also be recorded. Here's the list for fields:
>每一个声明过的字段，它的如下（1，2，3）信息都会被存储在常量池中。除此之外，他们的声明顺序也会被记录。
1. The field's name
2. The field's type
3. The field's modifiers (some subset of public, private, protected, static, final, volatile, transient)

#### Method Information

For each method declared in the type, the following information must be stored in the method area. As with fields, the order in which the methods are declared by the class or interface must be recorded as well as the data. Here's the list:
>跟字段一样，顺序也要存。
1. The method's name
2. The method's return type (or void)
3. The number and types (in order) of the method's parameters
4. The method's modifiers (some subset of public, private, protected, static, final, synchronized, native, abstract)

In addition to the items listed previously, the following information must also be stored with each method that is not abstract or native:
>除了上面说的那些，如下这些也要存（如果这个方法不是抽象/本地方法）
1. The method's byteCodes
2. The sizes of the operand stack and local variables sections of the method's stack frame (these are described in a later section of this chapter)
3. An exception table (this is described in Chapter 17, "Exceptions")
>1. 
>2. 操作数栈，栈帧中的局部变量
>3. 异常表？第十七章`Exceptions`会有

#### Class Variables

Class variables are shared among all instances of a class and can be accessed even in the absence of any instance. These variables are associated with the class--not with instances of the class--so they are logically part of the class data in the method area. Before a Java virtual machine uses a class, it must allocate memory from the method area for each non-final class variable declared in the class.
>类变量在类的所有实例之间共享，即使在没有任何实例的情况下也可以访问。这些变量与类相关联 - 而不是与类的实例相关联 - 因此它们在逻辑上是方法区域中类数据的一部分。 在Java虚拟机使用类之前，它必须为方法区域中为类中声明的每个非最终类变量分配内存。

Constants (class variables declared final) are not treated in the same way as non-final class variables. Every type that uses a final class variable gets a copy of the constant value in its own constant pool. As part of the constant pool, final class variables are stored in the method area--just like non-final class variables. But whereas non-final class variables are stored as part of the data for the type that declares them, final class variables are stored as part of the data for any type that uses them. This special treatment of constants is explained in more detail in Chapter 6, "The Java Class File."
>常量（声明为final的类变量）的处理方式与非final类变量的处理方式不同。每个使用final变量的类型（类或接口）会在自己的常量池中存放一个常量值的拷贝值。作为常量值的一部分，类中的final变量会被存在方法区，跟非final变量一样。
>>但是，他们的区别是（个人理解），非final变量作为声明它的那个类型的数据的一部分来存储，final单独存储。举个例子：
>>  
>>      public class Test{
>>      final String a;
>>      String b;
>>      }
>>
>>a跟Test一起存储，b单独存储（虽然也在常量池中）
>详细内容在第六章`The Java Class File`中有介绍

#### A Reference to Class ClassLoader

For each type it loads, a Java virtual machine must keep track of whether or not the type was loaded via the bootstrap class loader or a user-defined class loader. For those types loaded via a user-defined class loader, the virtual machine must store a reference to the user-defined class loader that loaded the type. This information is stored as part of the type's data in the method area.
>Java虚拟机要区分类型（类或接口）是由bootstrap加载的还是用户类加载器加载的，所以通过用户加载器加载的这些类型（类或接口），Java虚拟机会储存一个对用户定义类加载器的引用在方法区。

The virtual machine uses this information during dynamic linking. When one type refers to another type, the virtual machine requests the referenced type from the same class loader that loaded the referencing type. This process of dynamic linking is also central to the way the virtual machine forms separate name spaces. To be able to properly perform dynamic linking and maintain multiple name spaces, the virtual machine needs to know what class loader loaded each type in its method area. The details of dynamic linking and name spaces are given in Chapter 8, "The Linking Model."
>虚拟机在动态链接（只有在用到这个类的时候，才会去加载相关内容）的时候会用到这些信息。跟动态链接和多重命名空件有关。第八章`The Linking Model.`里面会介绍。

#### A Reference to Class Class

An instance of class java.lang.Class is created by the Java virtual machine for every type it loads. The virtual machine must in some way associate a reference to the Class instance for a type with the type's data in the method area.
>每个类型（类或接口）被Java虚拟机加载的时候，都会有一个java.lang.Class的实例被创建（比方你加载了Test.class这个类，那么堆中会有一个java.lang.Class的实例，里面的name是test.class，里面的其他信息是test这个类中的信息）。虚拟机必须以某种方式将对类型（类或接口）实例的引用与方法区域中类型（类或接口）的数据相关联。

Your Java programs can obtain and use references to Class objects. One static method in class Class, allows you to get a reference to the Class instance for any loaded class:
>您的Java程序可以获取和使用对Class对象的引用。 类Class中的一个静态方法允许您获取对任何已加载类的Class实例的引用：

    // A method declared in class java.lang.Class:
    public static Class forName(String className);

If you invoke forName("java.lang.Object"), for example, you will get a reference to the Class object that represents java.lang.Object. If you invoke forName("java.util.Enumeration"), you will get a reference to the Class object that represents the Enumeration interface from the java.util package. You can use forName() to get a Class reference for any loaded type from any package, so long as the type can be (or already has been) loaded into the current name space. If the virtual machine is unable to load the requested type into the current name space, forName() will throw ClassNotFoundException.
>如果你调用了`forName("java.lang.Object")`，您将获得对表示java.lang.Object的Class对象的引用（实际上这个对象是Class这个类的对象，但是里面的内容，都是java.lang.Object的内容，比方你调用getName()，获取到的是java.lang.Object，getMethods()，获取到的也都是java.lang.Object里面的方法）。您可以使用forName（）从任何包中获取任何已经加载的类型（类或接口）的Class引用，只要该类型可以（或已经）加载到当前名称空间中即可。

An alternative way to get a Class reference is to invoke getClass() on any object reference. This method is inherited by every object from class Object itself:
>另一种获取Class对象的引用的方法是调用Object中的getClass()方法

    // A method declared in class java.lang.Object:
    public final Class getClass();

If you have a reference to an object of class java.lang.Integer, for example, you could get the Class object for java.lang.Integer simply by invoking getClass() on your reference to the Integer object.

Given a reference to a Class object, you can find out information about the type by invoking methods declared in class Class. If you look at these methods, you will quickly realize that class Class gives the running application access to the information stored in the method area. Here are some of the methods declared in class Class:
>通过得到Class对象的引用，你能获取很多这个类型（类或接口）的信息。下面是一些Class类中的方法：

    // Some of the methods declared in class java.lang.Class:
    public String getName();
    public Class getSuperClass();
    public boolean isInterface();
    public Class[] getInterfaces();
    public ClassLoader getClassLoader();

These methods just return information about a loaded type. getName() returns the fully qualified name of the type. getSuperClass() returns the Class instance for the type's direct superclass. If the type is class java.lang.Object or an interface, none of which have a superclass, getSuperClass() returns null. isInterface() returns true if the Class object describes an interface, false if it describes a class. getInterfaces() returns an array of Class objects, one for each direct superinterface. The superinterfaces appear in the array in the order they are declared as superinterfaces by the type. If the type has no direct superinterfaces, getInterfaces() returns an array of length zero. getClassLoader() returns a reference to the ClassLoader object that loaded this type, or null if the type was loaded by the bootstrap class loader. All this information comes straight out of the method area.
>这些方法会返回已经加载的类型（类或接口）的信息
>>getName()返回完整的类名  
>>getSuperClass()返回直接父类的class实例（包含它父类信息的class对象），如果是没有父类的类（例如java.lang.Object或者接口），那就返回空  
>>isInterface()会告诉你这个类型（类或接口）是不是接口  
>>getInterfaces()返回一个数组（包括好多类），都是父类接口的信息。某些情况下数组的长度会是0，（没有深入研究）  
>>getClassLoader()返回了类加载器的class实例（包含类加载器信息的class对象），如果是bootstrap那就返回空。
>
>所有这些信息直接来自方法区域。

#### Method Tables

The type information stored in the method area must be organized to be quickly accessible. In addition to the raw type information listed previously, implementations may include other data structures that speed up access to the raw data. One example of such a data structure is a method table. For each non-abstract class a Java virtual machine loads, it could generate a method table and include it as part of the class information it stores in the method area. A method table is an array of direct references to all the instance methods that may be invoked on a class instance, including instance methods inherited from superclasses. (A method table isn't helpful in the case of abstract classes or interfaces, because the program will never instantiate these.) A method table allows a virtual machine to quickly locate an instance method invoked on an object. Method tables are described in detail in Chapter 8, "The Linking Model."
>这些存储在方法区里的类型（类或接口）信息，必须要被组织成能够被非常快地访问的结构。**方法表**是一种可行的数据结构。方法表是一个直接引用的数组，它的内容是“所有会在这个类的实例中被调用的实例方法”，包括从父类继承来的父类方法。方法表在抽象类或接口的情况下没有用，因为程序永远不会实例化这些。方法表允许虚拟机快速定位对象调用的实例方法。详细介绍在第八章`The Linking Model.`

#### An Example of Method Area Use

As an example of how the Java virtual machine uses the information it stores in the method area, consider these classes:
>下面是一个Java虚拟机使用方法区中存的信息的例子

    // On CD-ROM in file jvm/ex2/Lava.java
    class Lava {
    
        private int speed = 5; // 5 kilometers per hour
    
        void flow() {
        }
    }
    
    // On CD-ROM in file jvm/ex2/Volcano.java
    class Volcano {
    
        public static void main(String[] args) {
            Lava lava = new Lava();
            lava.flow();
        }
    }

The following paragraphs describe how an implementation might execute the first instruction in the bytecodes for the main() method of the Volcano application. Different implementations of the Java virtual machine can operate in very different ways. The following description illustrates one way--but not the only way--a Java virtual machine could execute the first instruction of Volcano's main() method.

To run the Volcano application, you give the name "Volcano" to a Java virtual machine in an implementation-dependent manner. Given the name Volcano, the virtual machine finds and reads in file Volcano.class. It extracts the definition of class Volcano from the binary data in the imported class file and places the information into the method area. The virtual machine then invokes the main() method, by interpreting the bytecodes stored in the method area. As the virtual machine executes main(), it maintains a pointer to the constant pool (a data structure in the method area) for the current class (class Volcano).
> * 首先给到类名（每种实现方式不一样，给类名的方式也不一样，不在这里讨论），Java虚拟机找到，并读取Volcano.class文件。
> * 虚拟机从导入的类文件的二进制码中提取类Volcano的定义，并将信息放入方法区。
> * 虚拟机通过解读方法区中的字节码，调用main()方法。
> * 当虚拟机执行main()方法时，它维护一个指向当前类（类Volcano）的常量池（方法区域中的数据结构）的指针。

Note that this Java virtual machine has already begun to execute the bytecodes for main() in class Volcano even though it hasn't yet loaded class Lava. Like many (probably most) implementations of the Java virtual machine, this implementation doesn't wait until all classes used by the application are loaded before it begins executing main(). It loads classes only as it needs them.
>明确一点，Java虚拟机已经开始执行Volcano的main()方法了，但是它还没有加载Lava类。绝大多数的Java虚拟机的实现，都不会等待所有（该应用）要使用的类加载完，而是会直接启动main()方法。它只有在需要（用到）这些类的时候，才会加载它们。

main()'s first instruction tells the Java virtual machine to allocate enough memory for the class listed in constant pool entry one. The virtual machine uses its pointer into Volcano's constant pool to look up entry one and finds a symbolic reference to class Lava. It checks the method area to see if Lava has already been loaded.
> * main()方法的第一条指令告诉Java虚拟机，给常量池条目1（entry one）中列出的类分配足够多的内存。
> * 虚拟机通过它的指针进入Volcano的常量池查找条目1（entry one），找到了Lava类的符号引用。
> * 虚拟机检查方法区，判断Lava类是否已经被加载。

The symbolic reference is just a string giving the class's fully qualified name: "Lava". Here you can see that the method area must be organized so a class can be located--as quickly as possible--given only the class's fully qualified name. Implementation designers can choose whatever algorithm and data structures best fit their needs--a hash table, a search tree, anything. This same mechanism can be used by the static forName() method of class Class, which returns a Class reference given a fully qualified name.
> 符号引用只是一个全量类名的字符串。（通过这个你就明白为什么方法区要被特别设计，为了让一个类能够尽快的被加载（只凭借全量类名），JVM的实现者可以使用任何算法来实现这个，static forName()也可以用这个）

When the virtual machine discovers that it hasn't yet loaded a class named "Lava," it proceeds to find and read in file Lava.class. It extracts the definition of class Lava from the imported binary data and places the information into the method area.
> * 当虚拟机发现它没有加载Lava类，虚拟机继续查找，并读取Lava.class文件
> * 虚拟机从导入的类文件的二进制码中提取类Lava的定义，并将信息放入方法区。

The Java virtual machine then replaces the symbolic reference in Volcano's constant pool entry one, which is just the string "Lava", with a pointer to the class data for Lava. If the virtual machine ever has to use Volcano's constant pool entry one again, it won't have to go through the relatively slow process of searching through the method area for class Lava given only a symbolic reference, the string "Lava". It can just use the pointer to more quickly access the class data for Lava. This process of replacing symbolic references with direct references (in this case, a native pointer) is called constant pool resolution. The symbolic reference is resolved into a direct reference by searching through the method area until the referenced entity is found, loading new classes if necessary.
> * Lava加载完之后，Java虚拟机会把Volcano的常量池条目1（entry one）中的符号引用替换成一个指针，指向Lava的类数据。
>
>如果虚拟机还要使用Volcano的常量池条目1（entry one），它不需要再经历相对比较慢的，在方法区中，根据符号引用（Lava全量类名），搜索Lava类的的过程。Java虚拟机只需要使用指针，就能够快速访问Lava的类数据。
>>这个把符号引用替换成直接引用（在这个例子中是指针）的过程，被叫做常量池解析（resolution）。通过查找方法区，找到引用的实体（必要时加载新的类），把符号引用解析成直接引用。

Finally, the virtual machine is ready to actually allocate memory for a new Lava object. Once again, the virtual machine consults the information stored in the method area. It uses the pointer (which was just put into Volcano's constant pool entry one) to the Lava data (which was just imported into the method area) to find out how much heap space is required by a Lava object.
> * 最终，虚拟机准备好给新的Lava对象分配内存了。
> * 虚拟机再次查询存储在方法区中的信息。
> * 虚拟机通过指向Lava的数据（刚刚放到方法区的）的指针（刚刚放到Volcano的常量池查找条目1（entry one）中的），找到并确定要在堆中给Lava对象分配多少内存。

A Java virtual machine can always determine the amount of memory required to represent an object by looking into the class data stored in the method area. The actual amount of heap space required by a particular object, however, is implementation-dependent. The internal representation of objects inside a Java virtual machine is another decision of implementation designers. Object representation is discussed in more detail later in this chapter.
> * 通过查看存储在方法区的类数据，Java虚拟机始终可以确定表示这个对象需要多少内存。
>
>但是，一个特定对象在堆中的实际大小，取决于不用的Java虚拟机实现。Java虚拟机对对象内部的实现也取决于Java虚拟机实现者。这一章的后面会详细讨论对象表示。

Once the Java virtual machine has determined the amount of heap space required by a Lava object, it allocates that space on the heap and initializes the instance variable speed to zero, its default initial value. If class Lava's superclass, Object, has any instance variables, those are also initialized to default initial values. (The details of initialization of both classes and objects are given in Chapter 7, "The Lifetime of a Type.")
> * 当Java虚拟机确定了要在堆中给Lava对象分配多少内存，它就在堆上分配那些空间，并初始化`Lava对象`中的变量实例`speed`为0，0是默认初始化值。如果Lava的父类（在这边是Object类），也有实例变量，那么它们也会被初始化成默认初始值。（初始化类和对象的详细信息在第七章`The Lifetime of a Type.`中）

The first instruction of main() completes by pushing a reference to the new Lava object onto the stack. A later instruction will use the reference to invoke Java code that initializes the speed variable to its proper initial value, five. Another instruction will use the reference to invoke the flow() method on the referenced Lava object.
> * Java虚拟机把新的Lava对象的引用push到栈里之后，main()方法的第一个指令完成了。
> * 下一个指令会使用这个引用来调用Java代码，来初始化`speed`变量为它正确的初始值，5。
> * 零一条指令会使用这个引用来调用被引用的Lava对象上的flow()方法。

---
### [The Heap](https://www.artima.com/insidejvm/ed2/jvm6.html)

Whenever a class instance or array is created in a running Java application, the memory for the new object is allocated from a single heap. As there is only one heap inside a Java virtual machine instance, all threads share it. Because a Java application runs inside its "own" exclusive Java virtual machine instance, there is a separate heap for every individual running application. There is no way two different Java applications could trample on each other's heap data. Two different threads of the same application, however, could trample on each other's heap data. This is why you must be concerned about proper synchronization of multi-threaded access to objects (heap data) in your Java programs.
>当一个类的实例或数组的实例在一个运行的Java应用中被创建，Java虚拟就就会在堆里面给这个新对象分配对应的内存。Java虚拟机实例中只有一个堆，所有线程共享。因为一个Java应用程序在他自己的虚拟机中运行，所以每个运行的应用程序都有一个单独的堆。两个Java应用不能互相访问对方的堆数据。但是同一个Java应用的两个不同的线程可以互相访问对方的堆数据。这就是您必须关注Java程序中对对象（堆数据）的多线程访问的正确同步的原因。

The Java virtual machine has an instruction that allocates memory on the heap for a new object, but has no instruction for freeing that memory. Just as you can't explicitly free an object in Java source code, you can't explicitly free an object in Java bytecodes. The virtual machine itself is responsible for deciding whether and when to free memory occupied by objects that are no longer referenced by the running application. Usually, a Java virtual machine implementation uses a garbage collector to manage the heap.
>Java虚拟机有一种指令，适用于在堆上面为新对象分配内存，但是没有释放该内存的指令。正如您无法在Java源代码中显式释放对象一样，您无法在Java字节码中显式释放对象。Java虚拟机自己决定`是否`以及`何时`释放正在运行的应用里面的，不再被引用的对象的，内存。通常Java虚拟机使用垃圾收集器管理堆。

#### Garbage Collection

A garbage collector's primary function is to automatically reclaim the memory used by objects that are no longer referenced by the running application. It may also move objects as the application runs to reduce heap fragmentation.
>垃圾收集器的主要任务是，自动回收正在运行的应用程序中，不再被引用的对象的内存。它也可以随着应用的运行移动对象，来见少堆中的碎片。

A garbage collector is not strictly required by the Java virtual machine specification. The specification only requires that an implementation manage its own heap in some manner. For example, an implementation could simply have a fixed amount of heap space available and throw an OutOfMemory exception when that space fills up. While this implementation may not win many prizes, it does qualify as a Java virtual machine. The Java virtual machine specification does not say how much memory an implementation must make available to running programs. It does not say how an implementation must manage its heap. It says to implementation designers only that the program will be allocating memory from the heap, but not freeing it. It is up to designers to figure out how they want to deal with that fact.
>Java虚拟机规范中并没有严格要求一个垃圾收集器。规范中只要求了需要以某种方式管理堆。blah blah blah，全凭开发者心意。

No garbage collection technique is dictated by the Java virtual machine specification. Designers can use whatever techniques seem most appropriate given their goals, constraints, and talents. Because references to objects can exist in many places--Java Stacks, the heap, the method area, native method stacks--the choice of garbage collection technique heavily influences the design of an implementation's runtime data areas. Various garbage collection techniques are described in Chapter 9, "Garbage Collection."
>Java虚拟机规范没有规定垃圾收集技术。虚拟机开发者用啥都可以，blah blah blah，第9章`Garbage Collection.`中描述了各种垃圾收集技术。

As with the method area, the memory that makes up the heap need not be contiguous, and may be expanded and contracted as the running program progresses. An implementation's method area could, in fact, be implemented on top of its heap. In other words, when a virtual machine needs memory for a freshly loaded class, it could take that memory from the same heap on which objects reside. The same garbage collector that frees memory occupied by unreferenced objects could take care of finding and freeing (unloading) unreferenced classes. Implementations may allow users or programmers to specify an initial size for the heap, as well as a maximum and minimum size.
>方法区某些规则跟堆一样，所以在某些实现中，方法区可以被放在堆的顶部。当虚拟机需要内存用于新加载的类时，它可以从对象所在的同一堆中获取该内存。 释放未引用对象占用内存的同一个垃圾收集器可以负责查找和释放（卸载）未引用的类。 用户或程序员可以指定堆的初始大小，最大最小值。

#### Object Representation

The Java virtual machine specification is silent on how objects should be represented on the heap. Object representation--an integral aspect of the overall design of the heap and garbage collector--is a decision of implementation designers

The primary data that must in some way be represented for each object is the instance variables declared in the object's class and all its superclasses. Given an object reference, the virtual machine must be able to quickly locate the instance data for the object. In addition, there must be some way to access an object's class data (stored in the method area) given a reference to the object. For this reason, the memory allocated for an object usually includes some kind of pointer into the method area.
>凭借一个对象的引用，必须能够快速定位到这个对象的实例数据，和它的类数据（方法区中的类信息）。所以堆当中这个对象的内存，通常包括一个指向方法区的指针。

One possible heap design divides the heap into two parts: a handle pool and an object pool. An object reference is a native pointer to a handle pool entry. A handle pool entry has two components: a pointer to instance data in the object pool and a pointer to class data in the method area. The advantage of this scheme is that it makes it easy for the virtual machine to combat heap fragmentation. When the virtual machine moves an object in the object pool, it need only update one pointer with the object's new address: the relevant pointer in the handle pool. The disadvantage of this approach is that every access to an object's instance data requires dereferencing two pointers. This approach to object representation is shown graphically in Figure 5-5. This kind of heap is demonstrated interactively by the HeapOfFish applet, described in Chapter 9, "Garbage Collection."
>有一种方法是把堆分成两部分：一个句柄池和一个对象池。对象引用是一个指向句柄池条目的本地指针。句柄池条目分成两个部分：一个指向实例数据（对象池中的）的指针，和一个指向方法区中的类信息的指针。
>
>优点：这种设计可以让虚拟机清理碎片变得更简单。（堆内存整理的时候，什么都不用改，只要改句柄池里面的引用就行了）当虚拟机移动对象池中的对象时，它只需要使用对象的新地址更新一个指针：句柄池中的相关指针。
>
>缺点：每次访问对象的实例数据，都得解析两个指针（对象引用指针+句柄池指针 / 还是要在句柄池两个指针间做判断？ 应该是前者）。
>
>这种对象表示方法如图5-5所示。这种堆由HeapOfFish小程序以交互方式演示，如第9章`Garbage Collection.`中所述。

![Splitting an object across a handle pool and object pool](https://www.artima.com/insidejvm/ed2/images/fig5-5.gif "Splitting an object across a handle pool and object pool")

**Figure 5-5. Splitting an object across a handle pool and object pool.**

Another design makes an object reference a native pointer to a bundle of data that contains the object's instance data and a pointer to the object's class data. This approach requires dereferencing only one pointer to access an object's instance data, but makes moving objects more complicated. When the virtual machine moves an object to combat fragmentation of this kind of heap, it must update every reference to that object anywhere in the runtime data areas. This approach to object representation is shown graphically in Figure 5-6.
>另一种设计，堆不再被分为两个部分，让对象引用直接指向一个合集，合集包括对象实例数据，和指向方法区中的类数据的指针。
>
>优点：每次访问对象的实例数据，只需要解析一个指针。
>
>缺点：移动对象更复杂。清理内存碎片移动对象的时候，对象引用要更新（如果有好几个引用引用了这个对象，那就要更新好几个）。
>
>这种对象表示方法如图5-6所示。
![Keeping object data all in one place](https://www.artima.com/insidejvm/ed2/images/fig5-6.gif "Keeping object data all in one place")

**Figure 5-6. Keeping object data all in one place.**

The virtual machine needs to get from an object reference to that object's class data for several reasons. When a running program attempts to cast an object reference to another type, the virtual machine must check to see if the type being cast to is the actual class of the referenced object or one of its supertypes. . It must perform the same kind of check when a program performs an instanceof operation. In either case, the virtual machine must look into the class data of the referenced object. When a program invokes an instance method, the virtual machine must perform dynamic binding: it must choose the method to invoke based not on the type of the reference but on the class of the object. To do this, it must once again have access to the class data given only a reference to the object.
>虚拟机经常会需要根据对象引用去获取对象的类信息（原因很多）。例如，当程序尝试把一个对象引用转换成另一个类型，虚拟机需要检查一下，转换的这个类型，是不是这个对象，或者它的父类中的一个。另外，用到instanceof方法的时候，也会要查一下。在这些例子中，虚拟机都必须调查类信息。当一个程序调用了一个实例方法，虚拟机必须执行动态绑定：动态绑定调用的方法是基于对象的类的，而不是引用的类型的。所以还要访问（对象中指向的）方法区中的类数据。
>>静态绑定，private，final，和static的方法，在编译过程中就知道是那个类的方法
>>
>>动态绑定，在程序运行过程中，根据具体的实例对象才能具体确定是哪个方法。
>
>参考资料1，[java — 静态绑定和动态绑定](https://www.cnblogs.com/Mr24/p/6767972.html)  
>参考资料2，[Java方法的静态绑定与动态绑定讲解（向上转型的运行机制详解）](https://www.cnblogs.com/ygj0930/p/6554103.html)

No matter what object representation an implementation uses, it is likely that a method table is close at hand for each object. Method tables, because they speed up the invocation of instance methods, can play an important role in achieving good overall performance for a virtual machine implementation. Method tables are not required by the Java virtual machine specification and may not exist in all implementations. Implementations that have extremely low memory requirements, for instance, may not be able to afford the extra memory space method tables occupy. If an implementation does use method tables, however, an object's method table will likely be quickly accessible given just a reference to the object.
>方法表（方法区中的）可以加速方法实例的调用。它不是虚拟机规范中要求的。但是如果有方法表，只需引用该对象，就可以快速访问对象的方法表。

One way an implementation could connect a method table to an object reference is shown graphically in Figure 5-7. This figure shows that the pointer kept with the instance data for each object points to a special structure. The special structure has two components:
>方法表和对象引用关联的一个例子在图5-7中展示。图里面展示的是，对象引用-实例数据-特殊结构，之间的指针。这个特殊的结构包含两个部分。

1. A pointer to the full the class data for the object
2. The method table for the object The method table is an array of pointers to the data for each instance method that can be invoked on objects of that class. The method data pointed to by method table includes:
    1. The sizes of the operand stack and local variables sections of the method's stack
    2. The method's bytecodes
    3. An exception table
>指向，对象的完整的类数据，的指针
>
>这个对象的方法表。方法表是一个存储指针的数组，指向（这个类的对象的）每个实例方法的数据。方法表指向的方法数据如下：
>>方法堆栈的操作数堆栈和局部变量部分的大小  
>>方法的字节码  
>>异常表

This gives the virtual machine enough information to invoke the method. The method table include pointers to data for methods declared explicitly in the object's class or inherited from superclasses. In other words, the pointers in the method table may point to methods defined in the object's class or any of its superclasses. More information on method tables is given in Chapter 8, "The Linking Model."
>方法表给虚拟机提供了足够的信息，来调用方法。方法表包含了指向，这个对象的类中明确定义的，或者，从父类中继承的，方法的数据。换句话说，方法表中的指针可能指向对象的类中的方法，也可能是父类中的方法。更多信息可以在第八章`The Linking Model.`中找到

![Keeping the method table close at hand](https://www.artima.com/insidejvm/ed2/images/fig5-7.gif "Keeping the method table close at hand")

**Figure 5-7. Keeping the method table close at hand.**

If you are familiar with the inner workings of C++, you may recognize the method table as similar to the VTBL or virtual table of C++ objects. In C++, objects are represented by their instance data plus an array of pointers to any virtual functions that can be invoked on the object. This approach could also be taken by a Java virtual machine implementation. An implementation could include a copy of the method table for a class as part of the heap image for every instance of that class. This approach would consume more heap space than the approach shown in Figure 5-7, but might yield slightly better performance on a systems that enjoy large quantities of available memory.
>方法表跟C++对象中的VTBL或虚拟表很相似。在C++中，对象被表现为“它们的实例数据”+“一个包含指针的数组，指向一些这个对象可以调用的虚拟方法”。这种方法占用更多的堆空间，但是速度快。

One other kind of data that is not shown in Figures 5-5 and 5-6, but which is logically part of an object's data on the heap, is the object's lock. Each object in a Java virtual machine is associated with a lock (or mutex) that a program can use to coordinate multi-threaded access to the object. Only one thread at a time can "own" an object's lock. While a particular thread owns a particular object's lock, only that thread can access that object's instance variables. All other threads that attempt to access the object's variables have to wait until the owning thread releases the object's lock. If a thread requests a lock that is already owned by another thread, the requesting thread has to wait until the owning thread releases the lock. Once a thread owns a lock, it can request the same lock again multiple times, but then has to release the lock the same number of times before it is made available to other threads. If a thread requests a lock three times, for example, that thread will continue to own the lock until it has released it three times.
>还有一种数据，没有在图5-5和图5-6中表现，但是它是堆上对象数据的一部分，是一个对象锁。Java虚拟机上的每一个对象都被分配了一个锁，所以一个程序可以用它来协调多个线程访问同一个对象。同一时间段，只有一个线程可以拥有一个对象锁。只有有锁的对象能访问对象的实例变量。其他尝试访问的线程都需要等待锁的释放。如果一个线程已经有锁了，它可以多次请求拥有锁，但是对应的也要多此释放。

Many objects will go through their entire lifetimes without ever being locked by a thread. The data required to implement an object's lock is not needed unless the lock is actually requested by a thread. As a result, many implementations, such as the ones shown in Figure 5-5 and 5-6, may not include a pointer to "lock data" within the object itself. Such implementations must create the necessary data to represent a lock when the lock is requested for the first time. In this scheme, the virtual machine must associate the lock with the object in some indirect way, such as by placing the lock data into a search tree based on the object's address.
>许多线程整个生命周期也不会被线程锁定。图5-5和图5-6中的实现，必须创建必要的数据，来表示锁。

Along with data that implements a lock, every Java object is logically associated with data that implements a wait set. Whereas locks help threads to work independently on shared data without interfering with one another, wait sets help threads to cooperate with one another--to work together towards a common goal.
>没看明白

Wait sets are used in conjunction with wait and notify methods. Every class inherits from Object three "wait methods" (overloaded forms of a method named wait()) and two "notify methods" (notify() and notifyAll()). When a thread invokes a wait method on an object, the Java virtual machine suspends that thread and adds it to that object's wait set. When a thread invokes a notify method on an object, the virtual machine will at some future time wake up one or more threads from that object's wait set. As with the data that implements an object's lock, the data that implements an object's wait set is not needed unless a wait or notify method is actually invoked on the object. As a result, many implementations of the Java virtual machine may keep the wait set data separate from the actual object data. Such implementations could allocate the data needed to represent an object's wait set when a wait or notify method is first invoked on that object by the running application. For more information about locks and wait sets, see Chapter 20, "Thread Synchronization."
>没怎么看，没看明白

One last example of a type of data that may be included as part of the image of an object on the heap is any data needed by the garbage collector. The garbage collector must in some way keep track of which objects are referenced by the program. This task invariably requires data to be kept for each object on the heap. The kind of data required depends upon the garbage collection technique being used. For example, if an implementation uses a mark and sweep algorithm, it must be able to mark an object as referenced or unreferenced. For each unreferenced object, it may also need to indicate whether or not the object's finalizer has been run. As with thread locks, this data may be kept separate from the object image. Some garbage collection techniques only require this extra data while the garbage collector is actually running. A mark and sweep algorithm, for instance, could potentially use a separate bitmap for marking referenced and unreferenced objects. More detail on various garbage collection techniques, and the data that is required by each of them, is given in Chapter 9, "Garbage Collection."
>没怎么看，没看明白

In addition to data that a garbage collector uses to distinguish between reference and unreferenced objects, a garbage collector needs data to keep track of which objects on which it has already executed a finalizer. Garbage collectors must run the finalizer of any object whose class declares one before it reclaims the memory occupied by that object. The Java language specification states that a garbage collector will only execute an object's finalizer once, but allows that finalizer to "resurrect" the object: to make the object referenced again. When the object becomes unreferenced for a second time, the garbage collector must not finalize it again. Because most objects will likely not have a finalizer, and very few of those will resurrect their objects, this scenario of garbage collecting the same object twice will probably be extremely rare. As a result, the data used to keep track of objects that have already been finalized, though logically part of the data associated with an object, will likely not be part of the object representation on the heap. In most cases, garbage collectors will keep this information in a separate place. Chapter 9, "Garbage Collection," gives more information about finalization.
>没怎么看，没看明白

#### Array Representation

In Java, arrays are full-fledged objects. Like objects, arrays are always stored on the heap. Also like objects, implementation designers can decide how they want to represent arrays on the heap.
>在Java中，数组是一种完整的对象。跟对象一样，数组永远存在堆中。同样，开发者也可以决定怎么在堆里表现数组。

Arrays have a Class instance associated with their class, just like any other object. All arrays of the same dimension and type have the same class. The length of an array (or the lengths of each dimension of a multidimensional array) does not play any role in establishing the array's class. For example, an array of three ints has the same class as an array of three hundred ints. The length of an array is considered part of its instance data.
>与其他对象一样，数组有一个与其类关联的Class实例。 具有相同维度和类型的所有数组具有相同的类。数组的长度（或者多维数组每个维度的长度）在建立数组的类中不起任何作用。例如，一个有3个int的数组，跟一个有300个int的数组，有同样的类。数组的长度被视为其实例数据的一部分。

The name of an array's class has one open square bracket for each dimension plus a letter or string representing the array's type. For example, the class name for an array of ints is "[I". The class name for a three-dimensional array of bytes is "[[[B". The class name for a two-dimensional array of Objects is "[[Ljava.lang.Object". The full details of this naming convention for array classes is given in Chapter 6, "The Java Class File."
>数组的类的名字，由`[`加上一个字母或者字符串。数组每多一个维度，就多一个`[`。于格利兹，int[]的类名就是"[I".byte[][][]的类名就是"[[[B"。String[][]的类名是"[[Ljava.lang.Object"。第6章`The Java Class File.`中给出了这种数组类命名约定的完整细节。

Multi-dimensional arrays are represented as arrays of arrays. A two dimensional array of ints, for example, would be represented by a one dimensional array of references to several one dimensional arrays of ints. This is shown graphically in Figure 5-8.
>多维数组可以表示成数组中的元素也是数组。举个例子，一个二维int数组，可以理解为一个一维的数组，里面的元素是很多一维的int数组。就像下面的图5-8中一样。

![One possible heap representation for arrays](https://www.artima.com/insidejvm/ed2/images/fig5-8.gif "One possible heap representation for arrays")

**Figure 5-8. One possible heap representation for arrays.**

The data that must be kept on the heap for each array is the array's length, the array data, and some kind of reference to the array's class data. Given a reference to an array, the virtual machine must be able to determine the array's length, to get and set its elements by index (checking to make sure the array bounds are not exceeded), and to invoke any methods declared by Object, the direct superclass of all arrays.
>堆上面为每个数组保存的数据包括，长度，数组数据，和引用。给定一个对数组的引用，虚拟机必须能够确定数组的长度，通过索引获取和设置其元素（检查以确保不超过数组边界），并调用Object声明的任何方法，Object是所有数组的直接超类。

---
### [The Program Counter](https://www.artima.com/insidejvm/ed2/jvm7.html)

Each thread of a running program has its own pc register, or program counter, which is created when the thread is started. The pc register is one word in size, so it can hold both a native pointer and a returnAddress. As a thread executes a Java method, the pc register contains the address of the current instruction being executed by the thread. An "address" can be a native pointer or an offset from the beginning of a method's bytecodes. If a thread is executing a native method, the value of the pc register is undefined.
>运行的程序的每一个线程，都有它自己的程序计数器（pc寄存器），计数器是伴随线程的创建而创建的。程序计数器的大小是一个`word`（前面介绍过word，可以搜一下），所以它能放下本地指针和返回地址。一个线程执行一个Java方法，程序计数器包含了线程当前操作的指令的地址。“地址”可以是本机指针，也可以是方法字节码开头的偏移量。如果一个线程在执行native方法，那么程序计数器就是未定义。

---
### [The Java Stack](https://www.artima.com/insidejvm/ed2/jvm8.html)

When a new thread is launched, the Java virtual machine creates a new Java stack for the thread. As mentioned earlier, a Java stack stores a thread's state in discrete frames. The Java virtual machine only performs two operations directly on Java Stacks: it pushes and pops frames.
>启动新线程时，Java虚拟机会为该线程创建新的Java堆栈。 如前所述，Java堆栈将线程的状态存储在离散帧中。 Java虚拟机只在Java堆栈上直接执行两个操作：压栈和出栈。

The method that is currently being executed by a thread is the thread's current method. The stack frame for the current method is the current frame. The class in which the current method is defined is called the current class, and the current class's constant pool is the current constant pool. As it executes a method, the Java virtual machine keeps track of the current class and current constant pool. When the virtual machine encounters instructions that operate on data stored in the stack frame, it performs those operations on the current frame.
>一个线程正在执行的方法被称为该线程的当前方法。当前方法的栈帧被称为当前栈帧。定义当前方法的类被称为当前类，当前类的常量池被称为当前常量池。当虚拟机执行一个方法的时候，Java虚拟机跟踪当前类和当前常量池。当虚拟机遇到对存储在堆栈帧中的数据进行操作的指令时，它会在当前帧上执行这些操作。

When a thread invokes a Java method, the virtual machine creates and pushes a new frame onto the thread's Java stack. This new frame then becomes the current frame. As the method executes, it uses the frame to store parameters, local variables, intermediate computations, and other data.
>当一个线程调用一个Java方法，Java虚拟机创建一个新的栈帧，并做压栈处理。这个新的栈帧就是当前栈帧。方法执行的时候，会用到栈帧上的参数，局部变量，中间计算，和其他数据。

A method can complete in either of two ways. If a method completes by returning, it is said to have normal completion. If it completes by throwing an exception, it is said to have abrupt completion. When a method completes, whether normally or abruptly, the Java virtual machine pops and discards the method's stack frame. The frame for the previous method then becomes the current frame.
>一个方法有两种完成（结束）方式。如果方法完成并返回值，那么就是正常完成。如果方法完成并抛出异常，那么就是异常完成。当一个方法完成，无论是普通还是异常，Java虚拟机出栈那个栈帧，并且丢弃这个栈帧。先前的方法的栈帧变成了当前栈帧。

All the data on a thread's Java stack is private to that thread. There is no way for a thread to access or alter the Java stack of another thread. Because of this, you need never worry about synchronizing multi- threaded access to local variables in your Java programs. When a thread invokes a method, the method's local variables are stored in a frame on the invoking thread's Java stack. Only one thread can ever access those local variables: the thread that invoked the method.
>Java堆栈里的所有数据，都是线程私有的。一个线程不能访问或修改其他线程的栈。正因为这个原因，你永远不用担心多线程同部访问局部变量的问题。当一个线程调用一个方法，方法的局部变量会被存在调用方法的线程的栈的栈帧中。只有调用的这个线程可以访问这些局部变量。

Like the method area and heap, the Java stack and stack frames need not be contiguous in memory. Frames could be allocated on a contiguous stack, or they could be allocated on a heap, or some combination of both. The actual data structures used to represent the Java stack and stack frames is a decision of implementation designers. Implementations may allow users or programmers to specify an initial size for Java stacks, as well as a maximum or minimum size.
>跟方法区和堆一样，Java堆栈和栈帧在内存中，不需要连续。帧可以在连续堆栈上分配，也可以在堆上分配，或者两者的某种组合。具体怎么实现取决于开发实施者。应该允许用户设置初始值，最大最小值。

---
### [The Stack Frame](https://www.artima.com/insidejvm/ed2/jvm8.html)

The stack frame has three parts: local variables, operand stack, and frame data. The sizes of the local variables and operand stack, which are measured in words, depend upon the needs of each individual method. These sizes are determined at compile time and included in the class file data for each method. The size of the frame data is implementation dependent.
>栈帧有三个组成部分：局部变量，操作数栈，栈帧数据。局部变量和操作数栈的大小（用word来做单位）取决于每个方法自己的需求。这个大小在编译的时候就确定了，被包含在类数据中。栈帧数据的大小取决于实现。

When the Java virtual machine invokes a Java method, it checks the class data to determine the number of words required by the method in the local variables and operand stack. It creates a stack frame of the proper size for the method and pushes it onto the Java stack.
>当Java虚拟机调用一个Java方法，虚拟机检查类数据，来判断这个方法的操作数栈和局部变量需要多少个单位（word）的内存。虚拟机为这个方法创建一个适当大小的站站，然后把栈帧推送到Java堆栈中。

#### Local Variables

The local variables section of the Java stack frame is organized as a zero-based array of words. Instructions that use a value from the local variables section provide an index into the zero-based array. Values of type int, float, reference, and returnAddress occupy one entry in the local variables array. Values of type byte, short, and char are converted to int before being stored into the local variables. Values of type long and double occupy two consecutive entries in the array.
>Java堆栈帧的局部变量部分被组织为从零开始的数组（内容是word）。使用了局部变量中的值的指令集，提供了一个这个（从零开始的）数组的索引。int，flout，引用，返回地址，在局部变量数组占据一个entry（单位？）。byte，short，和char会被转化成int，然后存到局部变量里。long和double则会在数组总占据两个连续的entry（单位？）。

To refer to a long or double in the local variables, instructions provide the index of the first of the two consecutive entries occupied by the value. For example, if a long occupies array entries three and four, instructions would refer to that long by index three. All values in the local variables are word-aligned. Dual-entry longs and doubles can start at any index.
>要从局部变量引用一个long或者double，指令集提供了一个索引，这个索引的值，是这个long或者double占用的两个连续的单位（entries）的两个索引中的第一个。举个例子，如果一个long，在数组的3，4两个位置，那么指令集会提供3这个索引。局部变量中的所有值都是（word/单位）字对齐的。像long和double这样的占据两个单位的，可以从任何指数开始。

The local variables section contains a method's parameters and local variables. Compilers place the parameters into the local variable array first, in the order in which they are declared. Figure 5-9 shows the local variables section for the following two methods:
>局部变量部分包含方法的参数和局部变量。编译器先把参数放到本地变量数组，按照它们的先后声明顺序。图5-9是以下两个方法的局部变量部分的例子：

    // On CD-ROM in file jvm/ex3/Example3a.java
    class Example3a {
    
        public static int runClassMethod(int i, long l, float f,
            double d, Object o, byte b) {
    
            return 0;
        }
    
        public int runInstanceMethod(char c, double d, short s,
            boolean b) {
    
            return 0;
        }
    }
    
![Method parameters on the local variables section of a Java stack](https://www.artima.com/insidejvm/ed2/images/fig5-9.gif "Method parameters on the local variables section of a Java stack")

**Figure 5-9. Method parameters on the local variables section of a Java stack.**

Note that Figure 5-9 shows that the first parameter in the local variables for runInstanceMethod() is of type reference, even though no such parameter appears in the source code. This is the hidden this reference passed to every instance method. Instance methods use this reference to access the instance data of the object upon which they were invoked. As you can see by looking at the local variables for runClassMethod() in Figure 5-9, class methods do not receive a hidden this. Class methods are not invoked on objects. You can't directly access a class's instance variables from a class method, because there is no instance associated with the method invocation.
>注意图5-9里说的，runInstanceMethod()这个方法的局部变量中的第一个参数是引用类型，但是在代码里没有这种参数。这是一个隐藏引用，每个实例方法中都有。实例方法通过这个引用来访问（调用这个方法的）对象中的实例数据。如你所见，图5-9的runClassMethod()的局部变量中没有隐藏引用。因为runClassMethod()是静态方法（也叫做类方法？），类方法不会被对象调用（而是被类调用）。通过类方法，无法直接访问一个类的实例（对象）的变量，因为没有实例与这个方法调用关联（大概理解为静态方法的调用不与类的实例相关联吧）。

Note also that types byte, short, char, and boolean in the source code become ints in the local variables. This is also true of the operand stack. As mentioned earlier, the boolean type is not supported directly by the Java virtual machine. The Java compiler always uses ints to represent boolean values in the local variables or operand stack. Data types byte, short, and char, however, are supported directly by the Java virtual machine. These can be stored on the heap as instance variables or array elements, or in the method area as class variables. When placed into local variables or the operand stack, however, values of type byte, short, and char are converted into ints. They are manipulated as ints while on the stack frame, then converted back into byte, short, or char when stored back into heap or method area.
>也要注意，源码中的byte，short，char和boolean类型，在局部变量中变成了int类型。在操作数栈中也是这样。之前已经说过，boolean类型没有被Java虚拟机直接支持，在操作数栈和局部变量中，Java编译器总是用int来表示boolean。不过byte，short和char是直接被Java虚拟机支持的。它们可以被存在堆中，作为实例变量/数组元素，或者存在方法区，作为类变量。
>
>但是，当他们被加载到局部变量或者操作数栈的时候，还是被转化成int。在栈帧中，它们被当做int来操作，一旦存回堆或者方法区，又会被转化成byte，short或者char。


Also note that Object o is passed as a reference to runClassMethod(). In Java, all objects are passed by reference. As all objects are stored on the heap, you will never find an image of an object in the local variables or operand stack, only object references.
>注意，对象o以引用的形式被传递给runClassMethod()。在Java里，所有的对象在传递的时候，都是以引用的形式存在的。而且所有对象逗存在堆里，你永远不会在局部变量和操作数栈中找到一个对象的镜像，只有对象引用。

Aside from a method's parameters, which compilers must place into the local variables array first and in order of declaration, Java compilers can arrange the local variables array as they wish. Compilers can place the method's local variables into the array in any order, and they can use the same array entry for more than one local variable. For example, if two local variables have limited scopes that don't overlap, such as the i and j local variables in Example3b, compilers are free to use the same array entry for both variables. During the first half of the method, before j comes into scope, entry zero could be used for i. During the second half of the method, after i has gone out of scope, entry zero could be used for j.
>除了方法的参数（编译器必须第一时间放到局部变量里，而且要按顺序），Java编译器可以随意安排局部变量数组。编译器可以任意安排方法局部变量在数组中的位置，它们还可以为多个局部变量用同一个数组条目（数组中的同一个元素）。举个例子，如果两个局部变量具有不重叠的有限范围（不明白，大概能猜测），例如下面的Example3b.java的代码中的i和j，编译器可以用同一个数组元素来给这两个变量。在这个方法的前半段，在j进入这个范围之前，第0个元素可以被i使用，在这个方法的后半段，i离开了这个范围，第0个元素可以被j使用。

    // On CD-ROM in file jvm/ex3/Example3b.java
    class Example3b {
    
        public static void runtwoLoops() {
    
            for (int i = 0; i < 10; ++i) {
                System.out.println(i);
            }
    
            for (int j = 9; j >= 0; --j) {
                System.out.println(j);
            }
        }
    }

As with all the other runtime memory areas, implementation designers can use whatever data structures they deem most appropriate to represent the local variables. The Java virtual machine specification does not indicate how longs and doubles should be split across the two array entries they occupy. Implementations that use a word size of 64 bits could, for example, store the entire long or double in the lower of the two consecutive entries, leaving the higher entry unused.
>跟别的运行时内存区一样，实施设计师可以用任意自己觉得合适的数据结构来表现局部变量。Java虚拟机规范没有说明long和double该怎么被放在两个条目（单元）里。举个例子，使用64位系统（这里一个word单位是64bit大小），实施者可以把long或者double放在第一个条目（单元），然后第二个空着。

#### Operand Stack

Like the local variables, the operand stack is organized as an array of words. But unlike the local variables, which are accessed via array indices, the operand stack is accessed by pushing and popping values. If an instruction pushes a value onto the operand stack, a later instruction can pop and use that value.
>像局部变量一样，操作数栈也被组织为一个包含word的数组。但是不同的是，局部变量是使用index访问元素的，操作数栈跟Java堆栈一样，是使用压栈出栈来访问数据的。如果一个指令推送了一个值到操作数栈，那么后面的操作可以弹出并使用这个值。

The virtual machine stores the same data types in the operand stack that it stores in the local variables: int, long, float, double, reference, and returnType. It converts values of type byte, short, and char to int before pushing them onto the operand stack.
>Java虚拟机存在操作数栈中的数据类型跟局部变量中的一样，int，long，float，double，reference，和返回类型。另外，byte，short和char会被转成int并压栈。

Other than the program counter, which can't be directly accessed by instructions, the Java virtual machine has no registers. The Java virtual machine is stack-based rather than register-based because its instructions take their operands from the operand stack rather than from registers. Instructions can also take operands from other places, such as immediately following the opcode (the byte representing the instruction) in the bytecode stream, or from the constant pool. The Java virtual machine instruction set's main focus of attention, however, is the operand stack.
>除了无法通过指令直接访问的程序计数器之外，Java虚拟机没有寄存器。Java虚拟机是基于栈的，而不是基于寄存器（程序计数器）的，因为它的指令从操作数堆栈而不是寄存器中获取操作数。指令也可以从其他地方获取操作数，例如紧跟字节码流中的操作码（表示指令的字节）或来自常量池。然而，Java虚拟机指令集的主要关注点是操作数堆栈。

The Java virtual machine uses the operand stack as a work space. Many instructions pop values from the operand stack, operate on them, and push the result. For example, the iadd instruction adds two integers by popping two ints off the top of the operand stack, adding them, and pushing the int result. Here is how a Java virtual machine would add two local variables that contain ints and store the int result in a third local variable:
>Java虚拟机使用操作数堆栈作为工作空间。许多指令从操作数栈弹出数据，操作这些数据，然后推送结果（到操作数栈）。举个例子，iadd指令从操作数栈中弹出两个int，然后把他们加到一起，然后推送这个int结果到操作数栈。下面这个例子描述了，Java虚拟机如何把两个包含int的局部变量相加，并存储int返回值在第三个局部变量中：

    iload_0    // push the int in local variable 0
    iload_1    // push the int in local variable 1
    iadd       // pop two ints, add them, push result
    istore_2   // pop int, store into local variable 2

In this sequence of bytecodes, the first two instructions, iload_0 and iload_1, push the ints stored in local variable positions zero and one onto the operand stack. The iadd instruction pops those two int values, adds them, and pushes the int result back onto the operand stack. The fourth instruction, istore_2, pops the result of the add off the top of the operand stack and stores it into local variable position two. In Figure 5-10, you can see a graphical depiction of the state of the local variables and operand stack while executing these instructions. In this figure, unused slots of the local variables and operand stack are left blank.
>在这个字节码序列中，前两个指令iload_0和iload_1，将存储在局部变量位置0和1的整数推入操作数堆栈。iadd指令从操作数栈中弹出两个int，然后把他们加到一起，然后推送这个int结果回到操作数栈。第四个指令，istore_2，弹出之前相加的结果，并把他们存到局部变量位置2。在图5-10中，你可以看到一个图，描述了执行这些指令的时候局部变量和操作数栈的状态。

![Adding two local variables](https://www.artima.com/insidejvm/ed2/images/fig5-10.gif "Adding two local variables")

**Figure 5-10. Adding two local variables.**

#### Frame Data

In addition to the local variables and operand stack, the Java stack frame includes data to support constant pool resolution, normal method return, and exception dispatch. This data is stored in the frame data portion of the Java stack frame.
>除了局部变量和操作数堆栈之外，Java堆栈帧还包括支持常量池解析，常规方法返回和异常分派的数据。 该数据存储在Java堆栈帧的帧数据部分中。

Many instructions in the Java virtual machine's instruction set refer to entries in the constant pool. Some instructions merely push constant values of type int, long, float, double, or String from the constant pool onto the operand stack. Some instructions use constant pool entries to refer to classes or arrays to instantiate, fields to access, or methods to invoke. Other instructions determine whether a particular object is a descendant of a particular class or interface specified by a constant pool entry.
>许多Java虚拟机指令集中的指令从常量池中引用条目（单元）。
>>一些指令只是从常量池，推送int，long，float，double或者string类型的常量，到操作数栈。  
>>一些指令使用常量池条目（单元），来引用要实例化的类或数组，要访问的字段或要调用的方法。  
>>其他指令判断一个特定的对象是不是一个由常量池指定的，特定的类或者特定的接口，的后裔。

Whenever the Java virtual machine encounters any of the instructions that refer to an entry in the constant pool, it uses the frame data's pointer to the constant pool to access that information. As mentioned earlier, references to types, fields, and methods in the constant pool are initially symbolic. When the virtual machine looks up a constant pool entry that refers to a class, interface, field, or method, that reference may still be symbolic. If so, the virtual machine must resolve the reference at that time.
>当Java虚拟机遇到任意从常量池引用条目（单元）的指令时，虚拟机使用栈帧数据的指向常量池的指针，来访问这些信息。之前提到过的，常量池中对类型（类或接口），字段，方法的引用，是initially symbolic。当Java虚拟机查询引用类，接口，字段或方法的常量池条目（单元）时，引用可能还是象征性的。如果是，则虚拟机必须在那时解析引用。

Aside from constant pool resolution, the frame data must assist the virtual machine in processing a normal or abrupt method completion. If a method completes normally (by returning), the virtual machine must restore the stack frame of the invoking method. It must set the pc register to point to the instruction in the invoking method that follows the instruction that invoked the completing method. If the completing method returns a value, the virtual machine must push that value onto the operand stack of the invoking method.
>除了常量池的解析，帧数据必须帮助虚拟机处理正常或突然的方法完成。如果一个方法正常结束（通过返回），则虚拟机必须还原调用方法的堆栈帧。它必须设置程序计数器，让它指向调用方法中的指令，这个指令紧随 调用完成方法 的那个指令。如果完成方法返回一个值，虚拟机必须推送这个值到调用方法（调用目前这个完成方法的方法）的操作数栈中。

The frame data must also contain some kind of reference to the method's exception table, which the virtual machine uses to process any exceptions thrown during the course of execution of the method. An exception table, which is described in detail in Chapter 17, "Exceptions," defines ranges within the bytecodes of a method that are protected by catch clauses. Each entry in an exception table gives a starting and ending position of the range protected by a catch clause, an index into the constant pool that gives the exception class being caught, and a starting position of the catch clause's code.
>帧数据还必须包含对方法异常表的某些引用。虚拟机使用该引用来处理在方法执行过程中抛出的任何异常。异常表，在第十七章`Exceptions`中有具体介绍，定义了受catch代码保护的方法的字节码的范围（理解下来大概意思是，try{}中间的代码的字节码的范围吧）。异常表中的每一个条目（单元），都包含了catch代码保护的代码的开始位置和结束位置，还包含了被catch的那个异常对应的常量池索引，还包含catch中的代码的开始位置。

When a method throws an exception, the Java virtual machine uses the exception table referred to by the frame data to determine how to handle the exception. If the virtual machine finds a matching catch clause in the method's exception table, it transfers control to the beginning of that catch clause. If the virtual machine doesn't find a matching catch clause, the method completes abruptly. The virtual machine uses the information in the frame data to restore the invoking method's frame. It then rethrows the same exception in the context of the invoking method.
>当一个方法抛出一个异常时，Java虚拟机用帧数据引用的异常表来判断如何处理这个异常。如果虚拟机在这个方法的异常表中找到一个匹配的catch代码，虚拟机会把控制权转移到这个catch代码的开头。如果虚拟机找不到匹配的catch代码，这个方法就会突然停止。虚拟机使用帧数据中的信息来恢复调用方法的帧，然后它在调用方法的内容中重新抛出同样的异常。

In addition to data to support constant pool resolution, normal method return, and exception dispatch, the stack frame may also include other information that is implementation dependent, such as data to support debugging.
>除了支持常量池解析的数据，普通方法返回，异常调度，栈帧还包含其他信息，例如支持debug的数据。

#### Possible Implementations of the Java Stack

Implementation designers can represent the Java stack in whatever way they wish. As mentioned earlier, one potential way to implement the stack is by allocating each frame separately from a heap. As an example of this approach, consider the following class:
>实现设计者可以使用任意它们喜欢的方式来实现Java堆栈。就像之前提到过的，一个潜在的途径实现栈，是从堆中分别分配每个帧。下面是一个例子。

    // On CD-ROM in file jvm/ex3/Example3c.java
    class Example3c {
    
        public static void addAndPrint() {
            double result = addTwoTypes(1, 88.88);
            System.out.println(result);
        }
    
        public static double addTwoTypes(int i, double d) {
            return i + d;
        }
    }
    
Figure 5-11 shows three snapshots of the Java stack for a thread that invokes the addAndPrint() method. In the implementation of the Java virtual machine represented in this figure, each frame is allocated separately from a heap. To invoke the addTwoTypes() method, the addAndPrint() method first pushes an int one and double 88.88 onto its operand stack. It then invokes the addTwoTypes() method.
>图5-11是一个调用addAndPrint()方法的线程的Java堆栈的切片。每个栈帧逗从堆中独立分配空间。为了调用addTwoTypes()方法，addAndPrint()方法先推送了一个int=1的值和一个double=88.88的值到自己的操作数栈。然后它调用addTwoTypes()方法。

![Allocating frames from a heap](https://www.artima.com/insidejvm/ed2/images/fig5-11.gif "Allocating frames from a heap")

**Figure 5-11. Allocating frames from a heap.**

The instruction to invoke addTwoTypes() refers to a constant pool entry. The Java virtual machine looks up the entry and resolves it if necessary.
>调用addTwoTypes()的指令引用了一个常量池条目（单元）。虚拟机会查找条目（单元），并在必要时解析它。

Note that the addAndPrint() method uses the constant pool to identify the addTwoTypes() method, even though it is part of the same class. Like references to fields and methods of other classes, references to the fields and methods of the same class are initially symbolic and must be resolved before they are used.
>注意，addAndPrint()方法使用常量池来识别addTwoTypes()方法，虽然它也是相同类中的一部分。就像引用其他类的字段和方法一样，引用同一个类的字段和方法，也是象征性初始化的，它们在被使用的时候需要被解析。

The resolved constant pool entry points to information in the method area about the addTwoTypes() method. The virtual machine uses this information to determine the sizes required by addTwoTypes() for the local variables and operand stack. In the class file generated by Sun's javac compiler from the JDK 1.1, addTwoTypes() requires three words in the local variables and four words in the operand stack. (As mentioned earlier, the size of the frame data portion is implementation dependent.) The virtual machine allocates enough memory for the addTwoTypes() frame from a heap. It then pops the double and int parameters (88.88 and one) from addAndPrint()'s operand stack and places them into addTwoType()'s local variable slots one and zero.
>对常量池条目（单元）的解析，指向方法区中addTwoTypes()方法的信息。虚拟机使用这些信息来判断addTwoTypes()的局部变量和操作数栈需要的内存大小。
> 1. 在Sun的JDK1.1里面，addTwoTypes()这个方法需要3个单位（word）大小的局部变量和4个单位（word）大小的操作数栈。（如前所述，帧数据部分的大小取决于实现。）
> 2. 虚拟机从堆中（`为什么是堆，不是栈吗？？？？？可能是JDK1.1里面是在堆，后面是栈的意思？`）给addTwoTypes()分配足够的内存。
> 3. 然后从addAndPrint()方法的操作数栈中弹出int=1和double=88.88这两个值，并把他们放进addTwoType()方法的局部变量的第一个格子（单位/插槽）和第二个格子。

When addTwoTypes() returns, it first pushes the double return value (in this case, 89.88) onto its operand stack. The virtual machine uses the information in the frame data to locate the stack frame of the invoking method, addAndPrint(). It pushes the double return value onto addAndPrint()'s operand stack and frees the memory occupied by addTwoType()'s frame. It makes addAndPrint()'s frame current and continues executing the addAndPrint() method at the first instruction past the addTwoType() method invocation.
> 1. 当addTwoTypes()方法返回时，它先把返回值（这个例子中是89.88）推进它自己的操作数栈。
> 2. 虚拟机用帧数据中的信息来定位调用方法（addAndPrint()方法）的栈帧。
> 3. 虚拟机推送double返回值到addAndPrint()方法的操作数栈，并且释放addTwoType()的栈帧占据的内存。
> 4. 虚拟机把addAndPrint()方法的栈帧当成当前栈帧，并在addTwoType()方法被调用的那句指令之后的位置，继续执行addAndPrint()方法

Figure 5-12 shows snapshots of the Java stack of a different virtual machine implementation executing the same methods. Instead of allocating each frame separately from a heap, this implementation allocates frames from a contiguous stack. This approach allows the implementation to overlap the frames of adjacent methods. The portion of the invoking method's operand stack that contains the parameters to the invoked method become the base of the invoked method's local variables. In this example, addAndPrint()'s entire operand stack becomes addTwoType()'s entire local variables section.
>图5-12展示了，另外一种不同的虚拟机执行同样的方法时，Java堆栈的切片。此实现不是从堆中单独分配每个帧，而是从连续的堆栈中分配帧。该方法允许实现与相邻方法的帧重叠。调用方法的操作数栈的，含有被调用方法需要的参数的，那一部分，变成了被调用方法的局部变量的基础。在这个例子中，addAndPrint()方法的整个操作数栈，就是addTwoType()的整个局部变量部分。

![Allocating frames from a contiguous stack](https://www.artima.com/insidejvm/ed2/images/fig5-12.gif "Allocating frames from a contiguous stack")

**Figure 5-12. Allocating frames from a contiguous stack.**

This approach saves memory space because the same memory is used by the calling method to store the parameters as is used by the invoked method to access the parameters. It saves time because the Java virtual machine doesn't have to spend time copying the parameter values from one frame to another.
>这种方法节省了内存空间，因为相同的内存被重复利用。这种方法还节省了时间，因为Java虚拟机不用花时间在栈帧之间复制参数了。

Note that the operand stack of the current frame is always at the "top" of the Java stack. Although this may be easier to visualize in the contiguous memory implementation of Figure 5-12, it is true no matter how the Java stack is implemented. (As mentioned earlier, in all the graphical images of the stack shown in this book, the stack grows downwards. The "top" of the stack is always shown at the bottom of the picture.) Instructions that push values onto (or pop values off of) the operand stack always operate on the current frame. Thus, pushing a value onto the operand stack can be seen as pushing a value onto the top of the entire Java stack. In the remainder of this book, "pushing a value onto the stack" refers to pushing a value onto the operand stack of the current frame.
>注意，当前栈帧的操作数栈永远在Java堆栈的最上面。（之前已经提到过，这本书里面所有的Java堆栈的图片，堆栈都是向下增长的，所以“最上面”其实就是图片的最下面）推送数值到操作数栈的指令，永远在当前栈帧操作。因此，推送一个数据到操作数栈，可以被理解为推送数据到整个Java堆栈顶部。在本书的其余部分中，“将值推入堆栈”是指将值推送到当前帧的操作数堆栈。

One other possible approach to implementing the Java stack is a hybrid of the two approaches shown in Figure 5-11 and Figure 5-12. A Java virtual machine implementation can allocate a chunk of contiguous memory from a heap when a thread starts. In this memory, the virtual machine can use the overlapping frames approach shown in Figure 5-12. If the stack outgrows the contiguous memory, the virtual machine can allocate another chunk of contiguous memory from the heap. It can use the separate frames approach shown in Figure 5-11 to connect the invoking method's frame sitting in the old chunk with the invoked method's frame sitting in the new chunk. Within the new chunk, it can once again use the contiguous memory approach.
>另一个可能的实现Java堆栈的方法是一种混合前面图5-11和图5-12两种方法的混合版。当线程启动时，Java虚拟机实现可以从堆中分配一块连续内存。在这个内存中，虚拟机可以像图5-12一样重叠栈帧的方法。如果堆栈内存超过了这个连续的内存，那么Java虚拟机另外再从堆中分配一个连续的内存。然后用图5-11中的方式连接这两个内存块。

---
### [Native Method Stacks](https://www.artima.com/insidejvm/ed2/jvm9.html)

In addition to all the runtime data areas defined by the Java virtual machine specification and described previously, a running Java application may use other data areas created by or for native methods. When a thread invokes a native method, it enters a new world in which the structures and security restrictions of the Java virtual machine no longer hamper its freedom. A native method can likely access the runtime data areas of the virtual machine (it depends upon the native method interface), but can also do anything else it wants. It may use registers inside the native processor, allocate memory on any number of native heaps, or use any kind of stack.
>除了之前描述过的Java虚拟机规范定义的Java运行时数据区，一个Java应用程序还可能需要其他为本地方法创建的数据区。当一个线程调用本地方法，它进入了一个新的世界，在这里Java虚拟机的结构和安全限制不再妨碍其自由。本机方法可能会访问虚拟机的运行时数据区域（它取决于本机方法接口），但也可以执行其他任何需要的操作。它可以使用本机处理器内的寄存器，在任意数量的本机堆上分配内存，或使用任何类型的堆栈。

Native methods are inherently implementation dependent. Implementation designers are free to decide what mechanisms they will use to enable a Java application running on their implementation to invoke native methods.
>本机方法本质上依赖于实现（本机实现？）。实现设计者可以自行决定他们要用哪种机制来让他们的虚拟机上运行的Java应用调用本地方法。

Any native method interface will use some kind of native method stack. When a thread invokes a Java method, the virtual machine creates a new frame and pushes it onto the Java stack. When a thread invokes a native method, however, that thread leaves the Java stack behind. Instead of pushing a new frame onto the thread's Java stack, the Java virtual machine will simply dynamically link to and directly invoke the native method. One way to think of it is that the Java virtual machine is dynamically extending itself with native code. It is as if the Java virtual machine implementation is just calling another (dynamically linked) method within itself, at the behest of the running Java program.
>任意本地方法接口都会使用一些本地方法栈。当一个线程调用了一个Java方法，Java虚拟机创建一个新的栈帧，并把它推送到Java堆栈中。但是，当一个线程调用一个本地方法的时候，该线程将Java堆栈留在后面（抛诸脑后？）。不再推送一个新的栈帧到该线程的Java堆栈，Java虚拟机只会简单的动态的链接，并调用本地方法。Java虚拟机使用本地代码动态扩展自己。就好像Java虚拟机实现只是在运行Java程序的命令下调用其自身内的另一个（动态链接）方法。

If an implementation's native method interface uses a C-linkage model, then the native method stacks are C stacks. When a C program invokes a C function, the stack operates in a certain way. The arguments to the function are pushed onto the stack in a certain order. The return value is passed back to the invoking function in a certain way. This would be the behavior of the of native method stacks in that implementation.

A native method interface will likely (once again, it is up to the designers to decide) be able to call back into the Java virtual machine and invoke a Java method. In this case, the thread leaves the native method stack and enters another Java stack.
>一个本地方法接口很可能（取决于虚拟机实现者的意愿）能够回调Java虚拟机，并调用一个Java方法。在这种情况下，线程离开本地方法栈，进入拎一个Java堆栈。

Figure 5-13 shows a graphical depiction of a thread that invokes a native method that calls back into the virtual machine to invoke another Java method. This figure shows the full picture of what a thread can expect inside the Java virtual machine. A thread may spend its entire lifetime executing Java methods, working with frames on its Java stack. Or, it may jump back and forth between the Java stack and native method stacks.
>图5-13是一个线程调用本地方法，并回调Java虚拟机和另一个Java方法的图例。这个图描绘了一个线程在Java虚拟机中可能会有的回路的全貌。线程可能会花费整个生命周期来执行Java方法，并在其Java堆栈上使用栈帧。或者，它可能在Java堆栈和本机方法堆栈之间来回跳转。

![The stack for a thread that invokes Java and native methods](https://www.artima.com/insidejvm/ed2/images/fig5-13.gif "The stack for a thread that invokes Java and native methods")

**Figure 5-13. The stack for a thread that invokes Java and native methods.**

As depicted in Figure 5-13, a thread first invoked two Java methods, the second of which invoked a native method. This act caused the virtual machine to use a native method stack. In this figure, the native method stack is shown as a finite amount of contiguous memory space. Assume it is a C stack. The stack area used by each C-linkage function is shown in gray and bounded by a dashed line. The first C-linkage function, which was invoked as a native method, invoked another C-linkage function. The second C-linkage function invoked a Java method through the native method interface. This Java method invoked another Java method, which is the current method shown in the figure.
>跟图5-13中描绘的一样，一个线程先调用了两个Java方法，其中第二个Java方法调用了本地方法。这种操作导致虚拟机使用了本地方法栈。（在这个图中，本地方法栈是一个无限大的连续内存空间。）假设本地方法栈是一个c堆栈。省略一大段，感觉没啥用。第二个c连锁方法通过本地方法接口调用了一个Java方法。这个Java方法调用了另一个Java方法。

As with the other runtime memory areas, the memory they occupied by native method stacks need not be of a fixed size. It can expand and contract as needed by the running application. Implementations may allow users or programmers to specify an initial size for the method area, as well as a maximum or minimum size.

---
### [Execution Engine](https://www.artima.com/insidejvm/ed2/jvm10.html)

At the core of any Java virtual machine implementation is its execution engine. In the Java virtual machine specification, the behavior of the execution engine is defined in terms of an instruction set. For each instruction, the specification describes in detail what an implementation should do when it encounters the instruction as it executes bytecodes, but says very little about how. As mentioned in previous chapters, implementation designers are free to decide how their implementations will execute bytecodes. Their implementations can interpret, just-in-time compile, execute natively in silicon, use a combination of these, or dream up some brand new technique.
>Java虚拟机的核心是执行引擎。执行引擎的行为是根据指令集定义的。规范详细描述了当指令集的指令执行字节码的时候，一个实现该做什么，但是几乎没有规定怎么实现。实施设计师可自行选择怎么实现。

Similar to the three senses of the term "Java virtual machine" described at the beginning of this chapter, the term "execution engine" can also be used in any of three senses: an abstract specification, a concrete implementation, or a runtime instance. The abstract specification defines the behavior of an execution engine in terms of the instruction set. Concrete implementations, which may use a variety of techniques, are either software, hardware, or a combination of both. A runtime instance of an execution engine is a thread.
>执行引擎可以用在以下三个场景，抽象规范，具体实现，和运行实例。抽象规范 - 指令集，具体实现 - 软/硬件和结合，运行实例 - 线程。

Each thread of a running Java application is a distinct instance of the virtual machine's execution engine. From the beginning of its lifetime to the end, a thread is either executing bytecodes or native methods. A thread may execute bytecodes directly, by interpreting or executing natively in silicon, or indirectly, by just- in-time compiling and executing the resulting native code. A Java virtual machine implementation may use other threads invisible to the running application, such as a thread that performs garbage collection. Such threads need not be "instances" of the implementation's execution engine. All threads that belong to the running application, however, are execution engines in action.
>运行的Java应用的每个线程都是虚拟机执行引擎的一个不同的实例。它的整个生命周期，线程不是在执行字节码，就是本地方法。线程可以直接执行字节码，也可以间接执行。Java虚拟机实现可以使用正在运行的应用程序的不可见的其他线程，例如执行垃圾收集的线程。

#### The Instruction Set

A method's bytecode stream is a sequence of instructions for the Java virtual machine. Each instruction consists of a one-byte opcode followed by zero or more operands. The opcode indicates the operation to be performed. Operands supply extra information needed by the Java virtual machine to perform the operation specified by the opcode. The opcode itself indicates whether or not it is followed by operands, and the form the operands (if any) take. Many Java virtual machine instructions take no operands, and therefore consist only of an opcode. Depending upon the opcode, the virtual machine may refer to data stored in other areas in addition to (or instead of) operands that trail the opcode. When it executes an instruction, the virtual machine may use entries in the current constant pool, entries in the current frame's local variables, or values sitting on the top of the current frame's operand stack.
>

The abstract execution engine runs by executing bytecodes one instruction at a time. This process takes place for each thread (execution engine instance) of the application running in the Java virtual machine. An execution engine fetches an opcode and, if that opcode has operands, fetches the operands. It executes the action requested by the opcode and its operands, then fetches another opcode. Execution of bytecodes continues until a thread completes either by returning from its starting method or by not catching a thrown exception.
>

From time to time, the execution engine may encounter an instruction that requests a native method invocation. On such occasions, the execution engine will dutifully attempt to invoke that native method. When the native method returns (if it completes normally, not by throwing an exception), the execution engine will continue executing the next instruction in the bytecode stream.
>

One way to think of native methods, therefore, is as programmer-customized extensions to the Java virtual machine's instruction set. If an instruction requests an invocation of a native method, the execution engine invokes the native method. Running the native method is how the Java virtual machine executes the instruction. When the native method returns, the virtual machine moves on to the next instruction. If the native method completes abruptly (by throwing an exception), the virtual machine follows the same steps to handle the exception as it does when any instruction throws an exception.
>

Part of the job of executing an instruction is determining the next instruction to execute. An execution engine determines the next opcode to fetch in one of three ways. For many instructions, the next opcode to execute directly follows the current opcode and its operands, if any, in the bytecode stream. For some instructions, such as goto or return, the execution engine determines the next opcode as part of its execution of the current instruction. If an instruction throws an exception, the execution engine determines the next opcode to fetch by searching for an appropriate catch clause.
>

Several instructions can throw exceptions. The athrow instruction, for example, throws an exception explicitly. This instruction is the compiled form of the throw statement in Java source code. Every time the athrow instruction is executed, it will throw an exception. Other instructions throw exceptions only when certain conditions are encountered. For example, if the Java virtual machine discovers, to its chagrin, that the program is attempting to perform an integer divide by zero, it will throw an ArithmeticException. This can occur while executing any of four instructions--idiv, ldiv, irem, and lrem--which perform divisions or calculate remainders on ints or longs.
>

Each type of opcode in the Java virtual machine's instruction set has a mnemonic. In the typical assembly language style, streams of Java bytecodes can be represented by their mnemonics followed by (optional) operand values.
>

For an example of method's bytecode stream and mnemonics, consider the doMathForever() method of this class:
>

    // On CD-ROM in file jvm/ex4/Act.java
    class Act {
    
        public static void doMathForever() {
            int i = 0;
            for (;;) {
                i += 1;
                i *= 2;
            }
        }
    }

The stream of bytecodes for doMathForever() can be disassembled into mnemonics as shown next. The Java virtual machine specification does not define any official syntax for representing the mnemonics of a method's bytecodes. The code shown next illustrates the manner in which streams of bytecode mnemonics will be represented in this book. The left hand column shows the offset in bytes from the beginning of the method's bytecodes to the start of each instruction. The center column shows the instruction and any operands. The right hand column contains comments, which are preceded with a double slash, just as in Java source code.
>

    // Bytecode stream: 03 3b 84 00 01 1a 05 68 3b a7 ff f9
    // Disassembly:
    // Method void doMathForever()
    // Left column: offset of instruction from beginning of method
    // |   Center column: instruction mnemonic and any operands
    // |   |                   Right column: comment
       0   iconst_0           // 03
       1   istore_0           // 3b
       2   iinc 0, 1          // 84 00 01
       5   iload_0            // 1a
       6   iconst_2           // 05
       7   imul               // 68
       8   istore_0           // 3b
       9   goto 2             // a7 ff f9
       
This way of representing mnemonics is very similar to the output of the javap program of Sun's Java 2 SDK. javap allows you to look at the bytecode mnemonics of the methods of any class file. Note that jump addresses are given as offsets from the beginning of the method. The goto instruction causes the virtual machine to jump to the instruction at offset two (an iinc). The actual operand in the stream is minus seven. To execute this instruction, the virtual machine adds the operand to the current contents of the pc register. The result is the address of the iinc instruction at offset two. To make the mnemonics easier to read, the operands for jump instructions are shown as if the addition has already taken place. Instead of saying "goto -7," the mnemonics say, "goto 2."
>

The central focus of the Java virtual machine's instruction set is the operand stack. Values are generally pushed onto the operand stack before they are used. Although the Java virtual machine has no registers for storing arbitrary values, each method has a set of local variables. The instruction set treats the local variables, in effect, as a set of registers that are referred to by indexes. Nevertheless, other than the iinc instruction, which increments a local variable directly, values stored in the local variables must be moved to the operand stack before being used.
>

For example, to divide one local variable by another, the virtual machine must push both onto the stack, perform the division, and then store the result back into the local variables. To move the value of an array element or object field into a local variable, the virtual machine must first push the value onto the stack, then store it into the local variable. To set an array element or object field to a value stored in a local variable, the virtual machine must follow the reverse procedure. First, it must push the value of the local variable onto the stack, then pop it off the stack and into the array element or object field on the heap.
>

Several goals--some conflicting--guided the design of the Java virtual machine's instruction set. These goals are basically the same as those described in Part I of this book as the motivation behind Java's entire architecture: platform independence, network mobility, and security.
>

The platform independence goal was a major influence in the design of the instruction set. The instruction set's stack-centered approach, described previously, was chosen over a register-centered approach to facilitate efficient implementation on architectures with few or irregular registers, such as the Intel 80X86. This feature of the instruction set--the stack-centered design--make it easier to implement the Java virtual machine on a wide variety of host architectures.
>

Another motivation for Java's stack-centered instruction set is that compilers usually use a stack-based architecture to pass an intermediate compiled form or the compiled program to a linker/optimizer. The Java class file, which is in many ways similar to the UNIX .o or Windows .obj file emitted by a C compiler, really represents an intermediate compiled form of a Java program. In the case of Java, the virtual machine serves as (dynamic) linker and may serve as optimizer. The stack-centered architecture of the Java virtual machine's instruction set facilitates the optimization that may be performed at run-time in conjunction with execution engines that perform just-in-time compiling or adaptive optimization.
>

As mentioned in Chapter 4, "Network Mobility," one major design consideration was class file compactness. Compactness is important because it facilitates speedy transmission of class files across networks. In the bytecodes stored in class files, all instructions--except two that deal with table jumping--are aligned on byte boundaries. The total number of opcodes is small enough so that opcodes occupy only one byte. This design strategy favors class file compactness possibly at the cost of some performance when the program runs. In some Java virtual machine implementations, especially those executing bytecodes in silicon, the single-byte opcode may preclude certain optimizations that could improve performance. Also, better performance may have been possible on some implementations if the bytecode streams were word-aligned instead of byte-aligned. (An implementation could always realign bytecode streams, or translate opcodes into a more efficient form as classes are loaded. Bytecodes are byte-aligned in the class file and in the specification of the abstract method area and execution engine. Concrete implementations can store the loaded bytecode streams any way they wish.)
>

Another goal that guided the design of the instruction set was the ability to do bytecode verification, especially all at once by a data flow analyzer. The verification capability is needed as part of Java's security framework. The ability to use a data flow analyzer on the bytecodes when they are loaded, rather than verifying each instruction as it is executed, facilitates execution speed. One way this design goal manifests itself in the instruction set is that most opcodes indicate the type they operate on.
>

For example, instead of simply having one instruction that pops a word from the operand stack and stores it in a local variable, the Java virtual machine's instruction set has two. One instruction, istore, pops and stores an int. The other instruction, fstore, pops and stores a float. Both of these instructions perform the exact same function when executed: they pop a word and store it. Distinguishing between popping and storing an int versus a float is important only to the verification process.
>

For many instructions, the virtual machine needs to know the types being operated on to know how to perform the operation. For example, the Java virtual machine supports two ways of adding two words together, yielding a one-word result. One addition treats the words as ints, the other as floats. The difference between these two instructions facilitates verification, but also tells the virtual machine whether it should perform integer or floating point arithmetic.
>

A few instructions operate on any type. The dup instruction, for example, duplicates the top word of a stack irrespective of its type. Some instructions, such as goto, don't operate on typed values. The majority of the instructions, however, operate on a specific type. The mnemonics for most of these "typed" instructions indicate their type by a single character prefix that starts their mnemonic. Table 5-2 shows the prefixes for the various types. A few instructions, such as arraylength or instanceof, don't include a prefix because their type is obvious. The arraylength opcode requires an array reference. The instanceof opcode requires an object reference.
>

|Type|Code|Example|Description|
| ---- | ---- | --- | --- |
|byte|b|baload|load byte from array|
|short|s|saload|load short from array|
|int|i|iaload|load int from array|
|long|l|laload|load long from array|
|char|c|caload|load char from array|
|float|f|faload|load float from array|
|double|d|daload|load double from array|
|reference|a|aaload|load reference from array|

**Table 5-2. Type prefixes of bytecode mnemonics**

Values on the operand stack must be used in a manner appropriate to their type. It is illegal, for example, to push four ints, then add them as if they were two longs. It is illegal to push a float value onto the operand stack from the local variables, then store it as an int in an array on the heap. It is illegal to push a double value from an object field on the heap, then store the topmost of its two words into the local variables as an value of type reference. The strict type rules that are enforced by Java compilers must also be enforced by Java virtual machine implementations.
>

Implementations must also observe rules when executing instructions that perform generic stack operations independent of type. As mentioned previously, the dup instruction pushes a copy of the top word of the stack, irrespective of type. This instruction can be used on any value that occupies one word: an int, float, reference, or returnAddress. It is illegal, however, to use dup when the top of the stack contains either a long or double, the data types that occupy two consecutive operand stack locations. A long or double sitting on the top of the operand stack can be duplicated in their entirety by the dup2 instruction, which pushes a copy of the top two words onto the operand stack. The generic instructions cannot be used to split up dual-word values.
>

To keep the instruction set small enough to enable each opcode to be represented by a single byte, not all operations are supported on all types. Most operations are not supported for types byte, short, and char. These types are converted to int when moved from the heap or method area to the stack frame. They are operated on as ints, then converted back to byte, short, or char before being stored back into the heap or method area.
>

Table 5-3 shows the computation types that correspond to each storage type in the Java virtual machine. As used here, a storage type is the manner in which values of the type are represented on the heap. The storage type corresponds to the type of the variable in Java source code. A computation type is the manner in which the type is represented on the Java stack frame.
>

|Storage Type|Minimum Bits in Heap or Method Area|Computation Type|Words in the Java Stack Frame|
| ---- | ---- | --- | --- |
|byte|8|int|1|
|short|16|int|1|
|int|32|int|1|
|long|64|long|2|
|char|16|int|1|
|float|32|float|1|
|double|64|double|2|
|reference|32|reference|1|

**Table 5-3. Storage and computation types inside the Java virtual machine**

Implementations of the Java virtual machine must in some way ensure that values are operated on by instructions appropriate to their type. They can verify bytecodes up front as part of the class verification process, on the fly as the program executes, or some combination of both. Bytecode verification is described in more detail in Chapter 7, "The Lifetime of a Type." The entire instruction set is covered in detail in Chapters 10 through 20.
>

#### Execution Techniques
#### Threads

---
### [Native Method Interface](https://www.artima.com/insidejvm/ed2/jvm12.html)

---
## [The Real Machine](https://www.artima.com/insidejvm/ed2/jvm13.html)

---
## [Eternal Math: A Simulation](https://www.artima.com/insidejvm/ed2/jvm13.html)

---
## [On the CD-ROM](https://www.artima.com/insidejvm/ed2/jvm13.html)

---
## [The Resources Page](https://www.artima.com/insidejvm/ed2/jvm13.html)
















