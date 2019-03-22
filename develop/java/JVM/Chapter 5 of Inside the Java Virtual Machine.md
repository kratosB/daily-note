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
>当虚拟机加载一个class文件时，它会解析包含在这个class文件中的二进制数据的类型（类或接口）信息。它将此类型（类或接口）信息放入方法区域。 当程序运行时，虚拟机将程序实例化的所有对象放置到堆上。

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
>类加载器子系统涉及Java虚拟机的许多其他部分以及java.lang库中的几个类。例如，用户定义地类加载器是普通Java对象，是java.lang.ClassLoader地后代。ClassLoader的方法允许Java应用访问虚拟机的类加载机制。此外，对于Java虚拟机加载的每种类型（类或接口），它都会创建类java.lang.Class的实例来表示该类型（类或接口）。与所有对象一样，用户定义的类加载器和类Class的实例放在堆上。加载类型（类或接口）的代码数据放在方法区域中。

#### Loading, Linking and Initialization

The class loader subsystem is responsible for more than just locating and importing the binary data for classes. It must also verify the correctness of imported classes, allocate and initialize memory for class variables, and assist in the resolution of symbolic references. These activities are performed in a strict order:
>这个类加载子系统不但负责定位并输入类的二进制数据，而且验证输入的类的正确性，为类的变量初始化并分配内存，并在解决方案上协助符号引用。这些活动按严格的顺序执行：

1. Loading: finding and importing the binary data for a type
>加载：查找并导入类型（类或接口）的二进制数据
2. Linking: performing verification, preparation, and (optionally) resolution
    1. Verification: ensuring the correctness of the imported type
    2. Preparation: allocating memory for class variables and initializing the memory to default values
    3. Resolution: transforming symbolic references from the type into direct references.
>链接：执行验证，准备，和（可选）解决方案
>
>>验证：确认导入的类型的正确性
>>
>>准备：为类的变量分配内存，并且初始化内存为默认值
>>
>>解决方案：将`符号引用`从类型转换为`直接引用`。
3. Initialization: invoking Java code that initializes class variables to their proper starting values.
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
>操作栈，栈帧中的本地变量
3. An exception table (this is described in Chapter 17, "Exceptions")
>异常表？第十七章`Exceptions`会有

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
>每个类型（类或接口）被Java虚拟机加载的时候，都会有一个java.lang.Class的实例被创建。虚拟机必须以某种方式将对类型（类或接口）实例的引用与方法区域中类型（类或接口）的数据相关联。

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
> * Lava加载完之后，Java虚拟机会把Volcano的线程池条目1（entry one）中的符号引用替换成一个指针，指向Lava的类数据。
>
>如果虚拟机还要使用Volcano的常量池条目1（entry one），它不需要再经历相对比较慢的，在方法区中，根据符号引用（Lava全量类名），搜索Lava类的的过程。Java虚拟机只需要使用指针，就能够怪苏访问Lava的类数据。
>>这个把符号引用替换成直接引用（在这个例子中是指针）的过程，被叫做常量池分解（resolution）。通过查找方法区，找到引用的实体（必要时加载新的类），把符号引用解析成直接引用。

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

One possible heap design divides the heap into two parts: a handle pool and an object pool. An object reference is a native pointer to a handle pool entry. A handle pool entry has two components: a pointer to instance data in the object pool and a pointer to class data in the method area. The advantage of this scheme is that it makes it easy for the virtual machine to combat heap fragmentation. When the virtual machine moves an object in the object pool, it need only update one pointer with the object's new address: the relevant pointer in the handle pool. The disadvantage of this approach is that every access to an object's instance data requires dereferencing two pointers. This approach to object representation is shown graphically in Figure 5-5. This kind of heap is demonstrated interactively by the HeapOfFish applet, described in Chapter 9, "Garbage Collection."

![Splitting an object across a handle pool and object pool](https://www.artima.com/insidejvm/ed2/images/fig5-5.gif "Splitting an object across a handle pool and object pool")

**Figure 5-5. Splitting an object across a handle pool and object pool.**

Another design makes an object reference a native pointer to a bundle of data that contains the object's instance data and a pointer to the object's class data. This approach requires dereferencing only one pointer to access an object's instance data, but makes moving objects more complicated. When the virtual machine moves an object to combat fragmentation of this kind of heap, it must update every reference to that object anywhere in the runtime data areas. This approach to object representation is shown graphically in Figure 5-6.

![Keeping object data all in one place](https://www.artima.com/insidejvm/ed2/images/fig5-6.gif "Keeping object data all in one place")

**Figure 5-6. Keeping object data all in one place.**

The virtual machine needs to get from an object reference to that object's class data for several reasons. When a running program attempts to cast an object reference to another type, the virtual machine must check to see if the type being cast to is the actual class of the referenced object or one of its supertypes. . It must perform the same kind of check when a program performs an instanceof operation. In either case, the virtual machine must look into the class data of the referenced object. When a program invokes an instance method, the virtual machine must perform dynamic binding: it must choose the method to invoke based not on the type of the reference but on the class of the object. To do this, it must once again have access to the class data given only a reference to the object.

No matter what object representation an implementation uses, it is likely that a method table is close at hand for each object. Method tables, because they speed up the invocation of instance methods, can play an important role in achieving good overall performance for a virtual machine implementation. Method tables are not required by the Java virtual machine specification and may not exist in all implementations. Implementations that have extremely low memory requirements, for instance, may not be able to afford the extra memory space method tables occupy. If an implementation does use method tables, however, an object's method table will likely be quickly accessible given just a reference to the object.

One way an implementation could connect a method table to an object reference is shown graphically in Figure 5-7. This figure shows that the pointer kept with the instance data for each object points to a special structure. The special structure has two components:

1. A pointer to the full the class data for the object
2. The method table for the object The method table is an array of pointers to the data for each instance method that can be invoked on objects of that class. The method data pointed to by method table includes:
3. The sizes of the operand stack and local variables sections of the method's stack
4. The method's bytecodes
5. An exception table

This gives the virtual machine enough information to invoke the method. The method table include pointers to data for methods declared explicitly in the object's class or inherited from superclasses. In other words, the pointers in the method table may point to methods defined in the object's class or any of its superclasses. More information on method tables is given in Chapter 8, "The Linking Model."

![Keeping the method table close at hand](https://www.artima.com/insidejvm/ed2/images/fig5-7.gif "Keeping the method table close at hand")

**Figure 5-7. Keeping the method table close at hand.**

If you are familiar with the inner workings of C++, you may recognize the method table as similar to the VTBL or virtual table of C++ objects. In C++, objects are represented by their instance data plus an array of pointers to any virtual functions that can be invoked on the object. This approach could also be taken by a Java virtual machine implementation. An implementation could include a copy of the method table for a class as part of the heap image for every instance of that class. This approach would consume more heap space than the approach shown in Figure 5-7, but might yield slightly better performance on a systems that enjoy large quantities of available memory.

One other kind of data that is not shown in Figures 5-5 and 5-6, but which is logically part of an object's data on the heap, is the object's lock. Each object in a Java virtual machine is associated with a lock (or mutex) that a program can use to coordinate multi-threaded access to the object. Only one thread at a time can "own" an object's lock. While a particular thread owns a particular object's lock, only that thread can access that object's instance variables. All other threads that attempt to access the object's variables have to wait until the owning thread releases the object's lock. If a thread requests a lock that is already owned by another thread, the requesting thread has to wait until the owning thread releases the lock. Once a thread owns a lock, it can request the same lock again multiple times, but then has to release the lock the same number of times before it is made available to other threads. If a thread requests a lock three times, for example, that thread will continue to own the lock until it has released it three times.

Many objects will go through their entire lifetimes without ever being locked by a thread. The data required to implement an object's lock is not needed unless the lock is actually requested by a thread. As a result, many implementations, such as the ones shown in Figure 5-5 and 5-6, may not include a pointer to "lock data" within the object itself. Such implementations must create the necessary data to represent a lock when the lock is requested for the first time. In this scheme, the virtual machine must associate the lock with the object in some indirect way, such as by placing the lock data into a search tree based on the object's address.

Along with data that implements a lock, every Java object is logically associated with data that implements a wait set. Whereas locks help threads to work independently on shared data without interfering with one another, wait sets help threads to cooperate with one another--to work together towards a common goal.

Wait sets are used in conjunction with wait and notify methods. Every class inherits from Object three "wait methods" (overloaded forms of a method named wait()) and two "notify methods" (notify() and notifyAll()). When a thread invokes a wait method on an object, the Java virtual machine suspends that thread and adds it to that object's wait set. When a thread invokes a notify method on an object, the virtual machine will at some future time wake up one or more threads from that object's wait set. As with the data that implements an object's lock, the data that implements an object's wait set is not needed unless a wait or notify method is actually invoked on the object. As a result, many implementations of the Java virtual machine may keep the wait set data separate from the actual object data. Such implementations could allocate the data needed to represent an object's wait set when a wait or notify method is first invoked on that object by the running application. For more information about locks and wait sets, see Chapter 20, "Thread Synchronization."

One last example of a type of data that may be included as part of the image of an object on the heap is any data needed by the garbage collector. The garbage collector must in some way keep track of which objects are referenced by the program. This task invariably requires data to be kept for each object on the heap. The kind of data required depends upon the garbage collection technique being used. For example, if an implementation uses a mark and sweep algorithm, it must be able to mark an object as referenced or unreferenced. For each unreferenced object, it may also need to indicate whether or not the object's finalizer has been run. As with thread locks, this data may be kept separate from the object image. Some garbage collection techniques only require this extra data while the garbage collector is actually running. A mark and sweep algorithm, for instance, could potentially use a separate bitmap for marking referenced and unreferenced objects. More detail on various garbage collection techniques, and the data that is required by each of them, is given in Chapter 9, "Garbage Collection."

In addition to data that a garbage collector uses to distinguish between reference and unreferenced objects, a garbage collector needs data to keep track of which objects on which it has already executed a finalizer. Garbage collectors must run the finalizer of any object whose class declares one before it reclaims the memory occupied by that object. The Java language specification states that a garbage collector will only execute an object's finalizer once, but allows that finalizer to "resurrect" the object: to make the object referenced again. When the object becomes unreferenced for a second time, the garbage collector must not finalize it again. Because most objects will likely not have a finalizer, and very few of those will resurrect their objects, this scenario of garbage collecting the same object twice will probably be extremely rare. As a result, the data used to keep track of objects that have already been finalized, though logically part of the data associated with an object, will likely not be part of the object representation on the heap. In most cases, garbage collectors will keep this information in a separate place. Chapter 9, "Garbage Collection," gives more information about finalization.

#### Array Representation

In Java, arrays are full-fledged objects. Like objects, arrays are always stored on the heap. Also like objects, implementation designers can decide how they want to represent arrays on the heap.

Arrays have a Class instance associated with their class, just like any other object. All arrays of the same dimension and type have the same class. The length of an array (or the lengths of each dimension of a multidimensional array) does not play any role in establishing the array's class. For example, an array of three ints has the same class as an array of three hundred ints. The length of an array is considered part of its instance data.

The name of an array's class has one open square bracket for each dimension plus a letter or string representing the array's type. For example, the class name for an array of ints is "[I". The class name for a three-dimensional array of bytes is "[[[B". The class name for a two-dimensional array of Objects is "[[Ljava.lang.Object". The full details of this naming convention for array classes is given in Chapter 6, "The Java Class File."

Multi-dimensional arrays are represented as arrays of arrays. A two dimensional array of ints, for example, would be represented by a one dimensional array of references to several one dimensional arrays of ints. This is shown graphically in Figure 5-8.

![One possible heap representation for arrays](https://www.artima.com/insidejvm/ed2/images/fig5-8.gif "One possible heap representation for arrays")

**Figure 5-8. One possible heap representation for arrays.**

The data that must be kept on the heap for each array is the array's length, the array data, and some kind of reference to the array's class data. Given a reference to an array, the virtual machine must be able to determine the array's length, to get and set its elements by index (checking to make sure the array bounds are not exceeded), and to invoke any methods declared by Object, the direct superclass of all arrays.

---
### [The Method Area](https://www.artima.com/insidejvm/ed2/jvm5.html)


---
### [The Method Area](https://www.artima.com/insidejvm/ed2/jvm5.html)


---
### [The Method Area](https://www.artima.com/insidejvm/ed2/jvm5.html)
















