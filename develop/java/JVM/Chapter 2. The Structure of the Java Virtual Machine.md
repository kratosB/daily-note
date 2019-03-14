## [Chapter 2. The Structure of the Java Virtual Machine](https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-2.html "第二章，Java虚拟机的结构")

This document specifies an abstract machine. It does not describe any particular implementation of the Java Virtual Machine.

这篇文章介绍了一种抽象的机器，它并没有描述任何JVM（Java虚拟机）的详细实现。

To implement the Java Virtual Machine correctly, you need only be able to read the `class` file format and correctly perform the operations specified therein. Implementation details that are not part of the Java Virtual Machine's specification would unnecessarily constrain the creativity of implementors. For example, the memory layout of run-time data areas, the garbage-collection algorithm used, and any internal optimization of the Java Virtual Machine instructions (for example, translating them into machine code) are left to the discretion of the implementor.

为了能正确地实现JVM（Java虚拟机），你需要能够读懂`class`格式的文件，并且能够正确地执行其中所描述的操作。那些不属于JVM规范的实现细节，将会不必要地限制实施者的创造力。例如，运行时数据区的内存分布，（JVM）使用的垃圾回收方法，以及JVM内部指令的优化(比如，将它们转换为机器代码)留给实现者自己去判断。

All references to Unicode in this specification are given with respect to The Unicode Standard, Version 6.0.0, available at http://www.unicode.org/.

本文中所有对Unicode的引用，都是基于标准的Unicode Version 6.0.0的，具体信息可以在http://www.unicode.org/找到。

### [2.1 The `class` File Format](https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-2.html#jvms-2.1 "`class`文件的格式")

Compiled code to be executed by the Java Virtual Machine is represented using a hardware- and operating system-independent binary format, typically (but not necessarily) stored in a file, known as the class file format. The class file format precisely defines the representation of a class or interface, including details such as byte ordering that might be taken for granted in a platform-specific object file format.

这种编译之后的，被JVM执行的代码，是一种独立于硬件和操作系统的二进制格式。它们（编译后的代码）通常会（但不是必须的）被存放在文件里，这类文件的是格式是`class`格式。class文件精确描述了一个类或接口的实现，包括详例如字节顺序可能会被视为一个特定于平台的对象文件格的式细信息。

Chapter 4, "The class File Format", covers the class file format in detail.





























#### 2.5. Run-Time Data Areas

The Java Virtual Machine defines various run-time data areas that are used during execution of a program. Some of these data areas are created on Java Virtual Machine start-up and are destroyed only when the Java Virtual Machine exits. Other data areas are per thread. Per-thread data areas are created when a thread is created and destroyed when the thread exits.

java虚拟机定义了许多不同的运行时数据区。其中一部分数据区是伴随着JVM的启动创建的，它们只会在JVM退出的时候被销毁。其他的数据区是属于线程的数据区，线程数据区的创建和销毁，紧随着线程的创建和结束。

下面有一个图片，可能可以帮助理解

![Java 运行时的内存划分](https://camo.githubusercontent.com/2cb4513f3e90831871d92a43678032b31283029e/68747470733a2f2f7773312e73696e61696d672e636e2f6c617267652f303036744e6337396c7931666d6b35763139636d766a3330673230616e7133792e6a7067)


##### 2.5.1. The pc Register

The Java Virtual Machine can support many threads of execution at once (JLS §17). Each Java Virtual Machine thread has its own pc (program counter) register. At any point, each Java Virtual Machine thread is executing the code of a single method, namely the current method (§2.6) for that thread. If that method is not native, the pc register contains the address of the Java Virtual Machine instruction currently being executed. If the method currently being executed by the thread is native, the value of the Java Virtual Machine's pc register is undefined. The Java Virtual Machine's pc register is wide enough to hold a returnAddress or a native pointer on the specific platform.

java虚拟机可以同时支持多个线程，每个JVM线程都有他们自己的程序计数器。在任意时间点，每个JVM线程执行它们自己单独的方法，也就是当前线程的当前方法。如果这个方法不是native的，程序计数器会包含当前执行的JVM指令的地址。如果当前正在被执行的方法是native的，那么JVM程序计数器的值就是未定义的。

##### 2.5.2. Java Virtual Machine Stacks

Each Java Virtual Machine thread has a private Java Virtual Machine stack, created at the same time as the thread. A Java Virtual Machine stack stores frames (§2.6). A Java Virtual Machine stack is analogous to the stack of a conventional language such as C: it holds local variables and partial results, and plays a part in method invocation and return. Because the Java Virtual Machine stack is never manipulated directly except to push and pop frames, frames may be heap allocated. The memory for a Java Virtual Machine stack does not need to be contiguous.

每一个JVM线程，都有一个私有的JVM栈，伴随着线程的创建同时被创建。JVM栈是用来存放帧（为了防止混淆，这里的帧被称为栈帧）的。JVM的栈跟传统的编程语言中（例如c语言）的栈是类似的：它保存局部变量和中间结果，并作用于方法调用和返回。由于JVM栈除了push栈帧和pop栈帧，没有其他直接的操作，所以帧是可以在堆上分配的。栈在内存中不需要是连续的。

In the First Edition of The Java® Virtual Machine Specification, the Java Virtual Machine stack was known as the Java stack.

在Java虚拟机规范第一版中，JVM栈称为Java栈。

This specification permits Java Virtual Machine stacks either to be of a fixed size or to dynamically expand and contract as required by the computation. If the Java Virtual Machine stacks are of a fixed size, the size of each Java Virtual Machine stack may be chosen independently when that stack is created.

此规范允许Java虚拟机堆栈具有固定大小或根据计算的需要动态扩展和收缩。 如果JVM栈具有固定大小，则可以在创建该栈时独立选择每个JVM栈的大小。

A Java Virtual Machine implementation may provide the programmer or the user control over the initial size of Java Virtual Machine stacks, as well as, in the case of dynamically expanding or contracting Java Virtual Machine stacks, control over the maximum and minimum sizes.

Java虚拟机实现可以为程序员或用户提供对JVM栈的初始大小的控制。同时，在可以动态扩展或收缩JVM栈的情况下，Java虚拟机也提供了最大值和最小值的控制。

The following exceptional conditions are associated with Java Virtual Machine stacks:

以下异常状况与JVM栈有关：

* If the computation in a thread requires a larger Java Virtual Machine stack than is permitted, the Java Virtual Machine throws a StackOverflowError.  
若线程执行过程中栈帧大小超出虚拟机栈限制，则会抛出 StackOverflowError。
* If Java Virtual Machine stacks can be dynamically expanded, and expansion is attempted but insufficient memory can be made available to effect the expansion, or if insufficient memory can be made available to create the initial Java Virtual Machine stack for a new thread, the Java Virtual Machine throws an OutOfMemoryError.  
若虚拟机栈允许动态扩展，但在尝试扩展时内存不足，或者在为一个新线程初始化新的虚拟机栈时申请不到足够的内存，则会抛出 OutOfMemoryError。

##### 2.5.3. Heap

The Java Virtual Machine has a heap that is shared among all Java Virtual Machine threads. The heap is the run-time data area from which memory for all class instances and arrays is allocated.

Java虚拟机中有一个在所有Java虚拟机线程之间共享的堆。堆是运行时数据区，所有类实例和数组所需要的内存都会从中分配。

The heap is created on virtual machine start-up. Heap storage for objects is reclaimed by an automatic storage management system (known as a garbage collector); objects are never explicitly deallocated. The Java Virtual Machine assumes no particular type of automatic storage management system, and the storage management technique may be chosen according to the implementor's system requirements. The heap may be of a fixed size or may be expanded as required by the computation and may be contracted if a larger heap becomes unnecessary. The memory for the heap does not need to be contiguous.

堆在虚拟机启动的时候就被创建了。堆之中存储的对象，会被一个自动内存管理系统（垃圾回收器）回收；对象永远不会被显式回收。Java虚拟机假设没有特定类型的自动存储管理系统，可以根据实现者的系统要求选择存储管理技术。堆的大小可以被设置为固定的，也可以根据机算的要求扩展或收缩（当一个过大的堆是不必要的的时候），堆在内存中也不需要是连续的。

A Java Virtual Machine implementation may provide the programmer or the user control over the initial size of the heap, as well as, if the heap can be dynamically expanded or contracted, control over the maximum and minimum heap size.

Java虚拟机为程序员或用户提供了对堆的初始大小的控制。同时，如果堆可以动态扩展或收缩，Java虚拟机也提供了最大值和最小值的控制。

The following exceptional condition is associated with the heap:

以下异常状况与堆有关：

* If a computation requires more heap than can be made available by the automatic storage management system, the Java Virtual Machine throws an OutOfMemoryError.  
如果一个机算需要的堆容量超过自动存储管理系统所能分配的最大值，则Java虚拟机会抛出OutOfMemoryError。

##### 2.5.4. Method Area

The Java Virtual Machine has a method area that is shared among all Java Virtual Machine threads. The method area is analogous to the storage area for compiled code of a conventional language or analogous to the "text" segment in an operating system process. It stores per-class structures such as the run-time constant pool, field and method data, and the code for methods and constructors, including the special methods (§2.9) used in class and instance initialization and interface initialization.

Java虚拟机中有一个再所有线程之间共享的方法区。方法区类似于传统语言的存储编译过的编译代码的存储区域，或类似于操作系统进程中的“文本”段。它存储每个类的结构，比如运行时常量池，字段，方法数据以及构造函数和方法函数的代码，包括类和实例初始化以及接口初始化中使用的特殊方法。

The method area is created on virtual machine start-up. Although the method area is logically part of the heap, simple implementations may choose not to either garbage collect or compact it. This specification does not mandate the location of the method area or the policies used to manage compiled code. The method area may be of a fixed size or may be expanded as required by the computation and may be contracted if a larger method area becomes unnecessary. The memory for the method area does not need to be contiguous.

方法区在虚拟机启动的时候就被创建了。虽然从逻辑上来说，方法区是堆的一部分，但是简单的实现并不会收集或压缩它。这篇规范并未规定方法区的位置，或用于管理编译代码的策略。方法区的大小可以被设置为固定的，也可以根据机算的要求扩展或收缩（当一个过大的堆是不必要的的时候），方法区在内存中也不需要是连续的。

A Java Virtual Machine implementation may provide the programmer or the user control over the initial size of the method area, as well as, in the case of a varying-size method area, control over the maximum and minimum method area size.

Java虚拟机为程序员或用户提供了对方法区的初始大小的控制。同时，针对不同尺寸的方法区，Java虚拟机也提供了最大值和最小值的控制。

The following exceptional condition is associated with the method area:

以下异常状况与方法区有关：

* If memory in the method area cannot be made available to satisfy an allocation request, the Java Virtual Machine throws an OutOfMemoryError.
如果方法区内存大小无法满足一个被分配来的请求，则Java虚拟机会抛出OutOfMemoryError。

##### 2.5.5. Run-Time Constant Pool

A run-time constant pool is a per-class or per-interface run-time representation of the constant_pool table in a class file (§4.4). It contains several kinds of constants, ranging from numeric literals known at compile-time to method and field references that must be resolved at run-time. The run-time constant pool serves a function similar to that of a symbol table for a conventional programming language, although it contains a wider range of data than a typical symbol table.

运行时常量池是一种每个类，每个接口的运行

Each run-time constant pool is allocated from the Java Virtual Machine's method area (§2.5.4). The run-time constant pool for a class or interface is constructed when the class or interface is created (§5.3) by the Java Virtual Machine.

The following exceptional condition is associated with the construction of the run-time constant pool for a class or interface:

* When creating a class or interface, if the construction of the run-time constant pool requires more memory than can be made available in the method area of the Java Virtual Machine, the Java Virtual Machine throws an OutOfMemoryError.

See §5 (Loading, Linking, and Initializing) for information about the construction of the run-time constant pool.

##### 2.5.6. Native Method Stacks

An implementation of the Java Virtual Machine may use conventional stacks, colloquially called "C stacks," to support native methods (methods written in a language other than the Java programming language). Native method stacks may also be used by the implementation of an interpreter for the Java Virtual Machine's instruction set in a language such as C. Java Virtual Machine implementations that cannot load native methods and that do not themselves rely on conventional stacks need not supply native method stacks. If supplied, native method stacks are typically allocated per thread when each thread is created.

This specification permits native method stacks either to be of a fixed size or to dynamically expand and contract as required by the computation. If the native method stacks are of a fixed size, the size of each native method stack may be chosen independently when that stack is created.

A Java Virtual Machine implementation may provide the programmer or the user control over the initial size of the native method stacks, as well as, in the case of varying-size native method stacks, control over the maximum and minimum method stack sizes.

The following exceptional conditions are associated with native method stacks:

* If the computation in a thread requires a larger native method stack than is permitted, the Java Virtual Machine throws a StackOverflowError.
* If native method stacks can be dynamically expanded and native method stack expansion is attempted but insufficient memory can be made available, or if insufficient memory can be made available to create the initial native method stack for a new thread, the Java Virtual Machine throws an OutOfMemoryError.





> [参考翻译资料1](https://blog.csdn.net/cdl2008sky/article/details/8128208)
>
> [参考翻译资料2](https://book.douban.com/annotation/31406463/)