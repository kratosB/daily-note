### Chapter 5 of [Inside the Java Virtual Machine](https://www.artima.com/insidejvm/ed2/index.html)
## [The Java Virtual Machine](https://www.artima.com/insidejvm/ed2/jvm.html)

### [What is a Java Virtual Machine?](https://www.artima.com/insidejvm/ed2/jvm.html)

To understand the Java virtual machine you must first be aware that you may be talking about any of three different things when you say "Java virtual machine." You may be speaking of:

* the abstract specification,
* a concrete implementation, or
* a runtime instance.

The abstract specification is a concept, described in detail in the book: _The Java Virtual Machine Specification_, by Tim Lindholm and Frank Yellin. Concrete implementations, which exist on many platforms and come from many vendors, are either all software or a combination of hardware and software. A runtime instance hosts a single running Java application.

Each Java application runs inside a runtime instance of some concrete implementation of the abstract specification of the Java virtual machine. In this book, the term "Java virtual machine" is used in all three of these senses. Where the intended sense is not clear from the context, one of the terms "specification," "implementation," or "instance" is added to the term "Java virtual machine".

---
### [The Lifetime of a Java Virtual Machine](https://www.artima.com/insidejvm/ed2/jvm.html)

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
### [The Architecture of the Java Virtual Machine](https://www.artima.com/insidejvm/ed2/jvm2.html)

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
>当虚拟机加载一个class文件时，它会解析包含在这个class文件中的二进制数据的类型信息。它将此类型信息放入方法区域。 当程序运行时，虚拟机将程序实例化的所有对象放置到堆上。

![Runtime data areas shared among all threads](https://www.artima.com/insidejvm/ed2/images/fig5-2.gif "Runtime data areas shared among all threads")

**Figure 5-2. Runtime data areas shared among all threads.**

As each new thread comes into existence, it gets its own pc register (program counter) and Java stack. If the thread is executing a Java method (not a native method), the value of the pc register indicates the next instruction to execute. A thread's Java stack stores the state of Java (not native) method invocations for the thread. The state of a Java method invocation includes its local variables, the parameters with which it was invoked, its return value (if any), and intermediate calculations. The state of native method invocations is stored in an implementation-dependent way in native method stacks, as well as possibly in registers or other implementation-dependent memory areas.
>伴随着每个新线程的出现，它会有一个他自己的pc寄存器（程序计数器）和Java栈。如果线程在执行一个Java方法（不是本地方法），那么pc寄存器（程序计数器）的值表示下一条要执行的指令。该线程的Java栈存储了方法调用（非本地方法）的状态。方法调用的状态包括局部变量，调用的参数，返回值（如果有），以及中间计算。本机方法调用的状态以依赖于实现的方式存储在本机方法堆栈中，也可能存储在寄存器或其他依赖于实现的存储区域中。

The Java stack is composed of stack frames (or frames). A stack frame contains the state of one Java method invocation. When a thread invokes a method, the Java virtual machine pushes a new frame onto that thread's Java stack. When the method completes, the virtual machine pops and discards the frame for that method.
>Java栈由栈帧组成。一个栈帧包含一个Java方法调用的状态。当一个线程调用一个方法，虚拟机会push一个新的栈帧到该线程的Java栈中，当方法结束的时候，虚拟机会pop并丢弃这个方法对应的栈帧。

The Java virtual machine has no registers to hold intermediate data values. The instruction set uses the Java stack for storage of intermediate data values.This approach was taken by Java's designers to keep the Java virtual machine's instruction set compact and to facilitate implementation on architectures with few or irregular general purpose registers. In addition, the stack-based architecture of the Java virtual machine's instruction set facilitates the code optimization work done by just-in-time and dynamic compilers that operate at run-time in some virtual machine implementations.
>Java虚拟机没有用于保存中间数据值的寄存器。 指令集使用Java栈存储中间数据值。这个方法被Java开发者用来保持Java虚拟机的指令集更紧凑，并便于在具有少量或不规则通用寄存器的体系结构上实现。除此之外，Java虚拟机指令集的这种基于栈的体系结构，有助于在有些虚拟机实现中的实时和动态编译的代码精简工作的完成。

See Figure 5-3 for a graphical depiction of the memory areas the Java virtual machine creates for each thread. These areas are private to the owning thread. No thread can access the pc register or Java stack of another thread.
>图5-3是Java虚拟机为每个线程创建内存区域的图。这些内存区市每个线程私有的，没有线程能访问其他线程的程序计数器和Java栈。

![Runtime data areas exclusive to each thread](https://www.artima.com/insidejvm/ed2/images/fig5-3.gif "Runtime data areas exclusive to each thread")

**Figure 5-3. Runtime data areas exclusive to each thread.**

Figure 5-3 shows a snapshot of a virtual machine instance in which three threads are executing. At the instant of the snapshot, threads one and two are executing Java methods. Thread three is executing a native method.
>图5-3展示了一个“Java虚拟机实例执行3个线程”的切片。在这个切片中，线程1和线程2在执行Java方法，线程3在执行本地方法。

In Figure 5-3, as in all graphical depictions of the Java stack in this book, the stacks are shown growing downwards. The "top" of each stack is shown at the bottom of the figure. Stack frames for currently executing methods are shown in a lighter shade. For threads that are currently executing a Java method, the pc register indicates the next instruction to execute. In Figure 5-3, such pc registers (the ones for threads one and two) are shown in a lighter shade. Because thread three is currently executing a native method, the contents of its pc register--the one shown in dark gray--is undefined.
>在图5-3中，与本书中Java栈的所有图形描述一样，栈显示为向下增长。 每个栈的“顶部”显示在图的底部。 当前执行方法的栈帧以较浅的阴影显示。 对于当前正在执行Java方法的线程，pc寄存器指示要执行的下一条指令。 在图5-3中，这些pc寄存器（线程1和2的寄存器）以较浅的阴影显示。 由于线程3当前正在执行本机方法，因此其pc寄存器的内容（以深灰色显示的内容）未定义。

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
>类加载器子系统涉及Java虚拟机的许多其他部分以及java.lang库中的几个类。例如，用户定义地类加载器是普通Java对象，是java.lang.ClassLoader地后代。ClassLoader的方法允许Java应用访问虚拟机的类加载机制。此外，对于Java虚拟机加载的每种类型，它都会创建类java.lang.Class的实例来表示该类型。与所有对象一样，用户定义的类加载器和类Class的实例放在堆上。加载类型的代码数据放在方法区域中。

#### Loading, Linking and Initialization

The class loader subsystem is responsible for more than just locating and importing the binary data for classes. It must also verify the correctness of imported classes, allocate and initialize memory for class variables, and assist in the resolution of symbolic references. These activities are performed in a strict order:

1. Loading: finding and importing the binary data for a type
2. Linking: performing verification, preparation, and (optionally) resolution
    1. Verification: ensuring the correctness of the imported type
    2. Preparation: allocating memory for class variables and initializing the memory to default values
    3. Resolution: transforming symbolic references from the type into direct references.
3. Initialization: invoking Java code that initializes class variables to their proper starting values.

The details of these processes are given Chapter 7, "The Lifetime of a Type."

#### The Bootstrap Class Loader
#### User-Defined Class Loaders
#### Name Spaces






---
### [The Lifetime of a Java Virtual Machine](https://www.artima.com/insidejvm/ed2/jvm.html)

