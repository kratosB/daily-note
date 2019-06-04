## 第七章 虚拟机类加载机制

1. 虚拟机把描述类的数据从Class文件**加载**到内存，并对数据进行**校验**，**转换解析**和**初始化**，最终形成可以被虚拟机直接使用的Java类型，这就是虚拟机的类加载机制。
2. Java语言里，类型的**加载**和**连接**过程都是在程序**运行期间**完成的。

### 7.1 类加载的时机

![类的生命周期](https://img-blog.csdn.net/20180725191039947?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2dhbjc4NTE2MDYyNw==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

1. 类从被加载到虚拟机内存种开始，到卸载出内存为止，它的整个生命周期包括了：
    1. **加载（Loading）**
    2. **连接（Linking）**
        1. **验证（Verification）**
        2. **准备（Preparation）**
        3. **解析（Resolution）**
    3. **初始化（Initialization）**
    4. **使用（Using）**
    5. **卸载（Unloading）**
2. 虚拟机对**初始化**阶段有严格规定，只有四种情况必须立即对类进行“初始化”：
    1. 遇到**new**，**getstatic**，**putstatic**或**invokestatic**这四条字节码之类时，如果类没有进行过初始化，则需要先触发初始化。
        > 最常见的场景是:  
        > 1. 使用new关键字实例化对象的时候。
        > 2. 读取或设置一个类的静态字段的时候。
        > 3. 调用一个类的静态方法的时候。
    2. 使用**java.lang.reflect**包的方法对类进行反射调用的到时候，如果类没有进行过初始化，则需要先触发其初始化。
    3. 当初始化一个类的时候，如果它的**父类**没有进行过初始化，则需要先触发父类的初始化。
    4. 当虚拟机启动时，用户需要指定一个要执行的主类（包含main方法的那个类），虚拟机会先初始化这个主类。
    
    对于这四种会触发类进行初始化的场景，虚拟机规范中使用了一个很强烈的限定：**有且只有**。这四种场景中的行为称为对一个类进行的**主动引用**。除此之外的所有引用类的方式，都**不会触发初始化**，称为**被动引用**。

下面三个例子说明**被动引用**：

    /**
     * 被动使用类字段演示一：
     * 通过子类引用父类的静态字段，不会导致子类初始化
     */
    public class SuperClass {
    
        static {
            System.out.println("SuperClass init!");
        }
    
        public static int value = 123;
    }
    
    public class SubClass extends SuperClass {
    
        static {
            System.out.println("SubClass init!");
        }
    }
    
    /**
     * 非主动使用类字段演示
     */
    public class NotInitialization {
    
        public static void main(String[] args) {
            System.out.println(SubClass.value);
        }
    }

上面的代码运行之后，只会输出`SuperClass init!`，而不会输出`SubClass init!`。对于**静态字段**，只有**直接定义**这个字段的类才会被初始化。因此通过子类来引用父类中的静态字段，只会触发父类的初始化而不会触发子类的初始化。

    package org.fenixsoft.classloading;
    /**
     * 被动使用类字段演示二：
     * 通过数组定义来引用类，不会触发此类的初始化
     */
    public class SuperClass {
    
        static {
            System.out.println("SuperClass init!");
        }
    
        public static int value = 123;
    }
    
    /**
     * 被动使用类字段演示二：
     * 通过数组定义来引用类，不会触发此类的初始化
     */
    public class NotInitialization {
    
        public static void main(String[] args) {
            SuperClass[] sca = new SuperClass[10];
        }
    }
    
这段代码运行只会没有输出`SuperClass init!`，说明**没有触发**SuperClass类的初始化。但是这段代码触发了[Lorg.fenixsoft.classloading.SuperClass类的初始化阶段。这个类是**虚拟机自动生成**的，直接继承于Object的子类，创建动作由newarray触发。

这个类，代表了一个元素类型为org.fenixsoft.classloading.SuperClass的**一维数组**。

    /**
     * 被动使用类字段演示三： 
     * 常量在编译阶段会存入调用类的常量池，
     * 本质上没有直接引用到定义常量的类，因此不会触发定义常量的类的初始化
     */
    public class ConstClass {
    
        static {
            System.out.println("ConstClass init!");
        }
    
        public static final String HELLOWORLD = "hello world";
    }
    
    /**
     * 非主动使用类字段演示
     */
    public class NotInitialization {
    
        public static void main(String[] args) {
            System.out.println(ConstClass.HELLOWORLD);;
        }
    }
    
上面的代码运行之后，也没有输出`ConstClass init!`。这是因为虽然Java源码中引用了ConstClass类中的HELLOWORLD**常量**，但是编译阶段，这个常量的值存储到了**NotInitialization类的常量池中**，对常量`ConstClass.HELLOWORLD`的引用实际都**转化**为NotInitialization类**对自身常量池的引用**了。所以实际上NotInitialization类的Class文件中没有ConstClass类的符号引用入口，所以编译只会就不存在联系了。

接口的加载过程与类稍有不同。主要在于（上面的四种**有且仅有**的第三种），当一个接口初始化时，并**不要求父接口全部完成初始化**，只有在真正用到父接口的时候才会初始化。

### 7.2 类加载的过程

#### 7.2.1 加载

1. 加载（Loading）阶段是类加载（Class Loading）过程的一个阶段，主要有三件事情：
    1. 通过类的**全限定名**来获取定义此类的二进制字节流。
    2. 将这个字节流代表的**静态存储结构**转化为方法区的**运行时数据结构**。
    3. 在Java**堆中**生成一个代表这个类的java.lang.Class对象实例，作为方法区这些数据的访问入口。
2. 相对于类加载的其他阶段，加载阶段是开发期**可控性最强**的阶段，因为加载阶段既可以使用**系统的**类加载器，也可以使用**用户自定义的**类加载器。
3. 数组比较特殊，这边书里没讲。[参考资料](https://blog.csdn.net/zhangwei408089826/article/details/81667803)里面有提到。

#### 7.2.2 验证

1. 验证的目的是，确保Class文件的字节流中包含的信息**附合当前虚拟机的要求**，并且**不会危害**虚拟机自身的安全。
    > Java本身是安全的，但是Class文件并不一定用Java源码编译而来，一些其他途径（网络或其他途径，甚至使用16进制直接编辑）生成的Class文件需要验证，否则可能会载入会导致徐通崩溃的代码。
2. 不同虚拟机实现不同，但大致都会完成以下四个阶段的验证过程：
    1. **文件格式**验证：
        1. 验证字节流是否附合Class文件格式规范，并能被当前版本的虚拟机处理。
        2. 保证输入的字节流能正确地解析并**存储于方法区**。
        3. 验证的这个阶段是**基于字节流**进行的，这个阶段之后，字节流才会进入内存中的方法区，验证的后面三个阶段都是**基于方法区的存储结构**的。
    2. **元数据**验证：
        1. 对字节码描述的信息进行语义分析，以保证其描述的信息附合Java语言规范。
    3. **字节码**验证：
        1. 进行数据流和控制流分析。对类的方法体进行校验分析。保证被校验类的方法在运行时不会做出**危害虚拟机安全**的行为。
        2. 这个阶段是验证过程中**最复杂**的阶段。
    4. **符号引用**验证：
        1. 发生在虚拟机将**符号引用**转化为**直接引用**的时候。
        2. 这个转化动作将在连接的**第三个阶段**（**解析阶段**）中发生。
        3. 这个验证的目的是，确保**解析动作能正常执行**。
        
    验证阶段对虚拟机的类加载机制来说，是一个**非常重要，但不必要**的阶段。如果需要，可以使用-Xverify:none来关闭大部分类验证措施。

#### 7.2.3 准备

1. 准备阶段是正式为类变量**分配内存**并**设置类变量初始值**的阶段，这些内存都将在**方法区**中进行分配。
2. 这个阶段进行内存分配的**仅仅包括类变量（被static修饰的变量）**，**不包括实例变量**，实例变量将在对象实例化的时候，随着对象一起分配在Java堆中。
3. 这里的初始值，通常情况下，是数字类型**0**。
    1. 假设一个类变量是`public static int value = 123;`，那么`value`在准备阶段之后的初始值是`0`而不是`123`。
        > 因为这时候尚未开始执行任何Java方法，把`value`赋值为`123`的**putstatic指令**是程序被**编译之后**，存放于**类构造器`<clinit>()`方法**之中的，所以把`value`赋值成`123`的动作将在**初始化阶段**才会被执行。
    2. 在特殊情况下，类似`public static final int value = 123;`，在准备阶段，虚拟机直接会把`value`设置成`123`。
        > 编译时，Javac会为`value`生成**ConstantValue属性**，如果类字段的属性表中存在ConstantValue属性，那么**准备阶段**，虚拟机会根据ConstantValue的设置将`value`赋值为`123`。

#### 7.2.4 解析

1. 解析阶段，是虚拟机将**常量池内**的**符号引用**替换成**直接引用**的过程。
2. **符号引用（Symbolic References）**：符号引用以一组符号来描述所引用的目标。
    1. 符号可以是任何形式的**字面量**，只要能**无歧义**地定位到目标即可。
    2. 符号引用与虚拟机实现的**内存布局无关**。
    3. 引用的目标**不一定已经加载**到内存中。
3. **直接引用（Direct References）**：直接引用可以试直接指向目标的指针，相对偏移量，或是一个能间接定位到目标的句柄。
    1. 直接引用是与虚拟机实现的**内容布局相关**的，同一个符号引用在不同虚拟机实例上编译出来的直接引用一般不会相同。
    2. 如果有直接引用，引用目标肯定已经**在内存中存在**。
4. 虚拟机规范没有规定解析发生的具体时间，只要求在13个**用于操作符号引用的字节码指令**之前，先对他们所使用的符号引用进行解析。
    > anewarray,checkcast,getfield,getstatic,instanceof,invokeinterface,invokespecial,invokestatic,invokevirtual,multianewarray,new,putfield和putstatic
5. 对同一个符号引用**解析多次**是很常见的，所以虚拟机会对解析结果**进行缓存**。
6. 解析动作主要针对四类符号引用，分别对应常量池的四种常量类型。
    1. 类或接口：CONSTANT_Class_info
    2. 字段:CONSTANT_Fieldref_info
    3. 类方法:CONSTANT_Methodref_info
    4. 接口方法:CONSTANT_InterfaceMethodref_info
    
##### 7.2.4.1 类或接口的解析

假设当前代码所处的是**类D**，如果要把一个未解析过的**符号引用N**解析为**类或接口C的直接引用**，那虚拟机完成解析需要包括以下三个步骤：
1. 如果C**不是一个数组类型**，虚拟机会把代表N的全限定名**传递给D的类加载器**去加载类C。
2. 如果C**是一个数组类型**，并且数组的**元素类型**为对象（N的描述符是类似“[Ljava.lang.Integer”的形式），会按照第1点的规则**加载数组元素类型**。假设是上面说的`Integer[]`的形式，需要加载的**元素类型**是“java.lang.Integer”，接着，由虚拟机生成一个**代表此数组维度和元素的数组对象**。
    > 应该是会加载java.lang.Integer，但是没有触发java.lang.Integer的初始化阶段，触发了[Ljava.lang.Integer的类的初始化阶段，这个创建动作，由newarray触发。
3. 如果以上步骤没有任何异常，那么C在虚拟机中实际上**已经成为一个有效的类或接口**了，但在解析完成之前还要进行**符号引用验证**，确认C是否具备对D的**访问权限**。如果没有访问权限，则抛出**java.lang.IllegalAccessError异常**。

##### 7.2.4.2 字段解析

1. 要解析一个**未被解析过**的字段的符号引用，首先将会对**字段所属的类或接口**的符号引用进行解析。如果解析成功，那将这个字段所属的类或接口用C表示，虚拟机要求按如下步骤对C进行后续字段搜索：
    1. 如果C本身就包含了字段B，B的**简单名称和字段描述都与目标相匹配**，则返回字段B的直接引用，查找结束。
    2. 如果C本身不包含字段B，C中**实现了接口**，将会按照**继承关系从上往下递归搜索**各个接口和它的父类，如果找到了匹配的字段B，则返回这个字段的直接引用，查找结束。
    3. 如果C本身不包含字段B，C没实现接口，并且C不是java.lang.Object，将会按照**继承关系从上往下递归搜索**它的父类，如果找到了匹配的字段B，则返回这个字段的直接引用，查找结束。
    4. 如果以上123都不满足，查找失败，抛出java.lang.NoSuchFieldError异常。
2. 如果成功返回引用，还会对字段进行权限验证，如果没有访问权限，则抛出**java.lang.IllegalAccessError异常**。
3. 实际应用中可能会更加严格，如果一个字段名同时出现在C的接口和父类中，或者同时在自己或父类的多个接口中，编译器可能会**拒绝编译**。

##### 7.2.4.3 类方法解析

1. 类方法解析的第一步与字段解析一样，也是需要先对**方法所属的类或接口**的符号引用进行解析。如果解析成功，我们还用C表示这个类，虚拟机会按照如下步骤进行后续类方法搜索：
    1. **类方法**和**接口方法**的符号引用的常量类型定义是分开的。
    2. 在C中查找是否有方法B，B的**简单名称和描述符都与目标相匹配**，如果有则返回这个方法的直接引用，查找结束。
    3. 如果C中没有方法B，则在C的**父类中递归查找**是否有方法B，如果有则返回这个方法的直接引用，查找结束。
    4. 如果C中没有方法B，C的父类中也没有方法B，则在**C实现的接口以及它们的父类接口中递归查找**是否有方法B，如果有，说明类C是一个抽象类，抛出java.lang.AbstractMethodError，查找结束。
    5. 如果以上234都不满足，查找失败，抛出java.lang.NoSuchMethodError。
2. 同样也有权限验证，如果没有访问权限，则抛出**java.lang.IllegalAccessError异常**。
    
##### 7.2.4.4 接口方法解析

基本跟“**7.2.4.3类方法解析**”差不多。

#### 7.2.5 初始化

1. 类初始化阶段是类加载过程的最后一步。
2. 在**准备阶段**，变量已经赋过一次系统要求的**初始值**，在**初始化阶段**，则是根据**用户的要求**初始化变量和其他资源的值。
    > 初始化阶段是执行类构造器`<clinit>()`方法的过程。
    1. `<clinit>()`方法是由编译器**自动收集**类中的所有**类变量（static变量，其他的是实例变量）的赋值动作**和**静态语句块（static{}）中的语句**合并产生的。编译器收集的顺序是语句在源文件中出现的顺序。
    2. `<clinit>()`方法与类的构造函数（实例构造器`<init>()`）不同，它不需要显式调用父类构造器，虚拟机会保证在`<clinit>()`执行之前，父类的`<clinit>()`已经执行完毕，所以虚拟机中**第一个被执行的`<clinit>()`方法的类肯定是java.lang.Object**。
    3. 由于父类的`<clinit>()`方法先执行，父类中定义的**静态语句块**要**优先于子类变量赋值操作**。下面的代码中，字段B的值会是2，而不是1.
        ```
        static class Parent {
            public static int A = 1;
            static {
                A = 2;
            }
        }
        
        static class Sub extends Parent {
            public static int B = A;
        }
        
        public static void main(String[] args) {
            System.out.println(Sub.B);
        }
        ```
    4. `<clinit>()`方法对于类或接口来说，**不是必须的**。如果一个接口中**没有静态语句块**，也**没有对变量的赋值操作**，那么编译器不会为这个类生成一个`<clinit>()`方法。
    5. 接口中不能使用静态语句块，但是可以有变量初始化的赋值操作，所以也可以生成`<clinit>()`方法。接口与类不同的是，**不需要先执行父接口的`<clinit>()`方法**，只有父接口中的变量被使用时，父接口才会被初始化。
    6. 虚拟机会保证一个类的`<clinit>()`方法在**多个线程**中被正确地加锁和同步。如果多个线程同时初始化一个类，那么只有一个线程会执行`<clinit>()`方法，其他全部阻塞等待，直到`<clinit>()`方法结束。
        ```
        static class DeadLoopClass {
            static {
                // 如果不加上这个if，编译器会提示“Initializer does not complete normally”并拒绝编译
                if (true) {
                    System.out.println(Thread.currentThread() + "init DeadLoopClass");
                    while (true) {
                    }
                }
            }
        }
        
        public static void main(String[] args) {
            Runnable script = new Runnable() {
                public void run() {
                    System.out.println(Thread.currentThread() + "start");
                    DeadLoopClass dlc = new DeadLoopClass();
                    System.out.println(Thread.currentThread() + " run over");
                }
            };
            
            Thread thread1 = new Thread(script);
            Thread thread2 = new Thread(script);
            thread1.start();
            thread2.start();
        }
        ```
        运行结果如下，一条线程在长时间操作（死循环模拟），另一条现场阻塞等待：
        ```
        Thread[Thread-0,5,main]start
        Thread[Thread-1,5,main]start
        Thread[Thread-0,5,main]init DeadLoopClass
        ```

### 7.3 类加载器

1. 类加载器是一个**Java虚拟机外部**的代码模块，主要功能是，通过一个**类的全限定名**来获取**描述此类的二进制字节流**。
2. 类加载器在**类层次划分**，**OSGi**，**热部署**，**代码加密**等领域使用。

#### 7.3.1 类与类加载器

1. 类加载器虽然**只用于实现类的加载动作**，但是它在Java程序中起到的作用**不限于类加载阶段**。
2. 对于任意一个类，都需要由**它的类加载器**和**这个类本身**一同确立其**在Java虚拟机中的唯一性**。
    > 换句话说，比较两个类是否“相等”，只有在这两个类是**由同一个类加载器加载**的前提之下才有意义，否则，即使两个类源自于同一个Class文件，加载器不同，两个类也不相等。
    >
    > 这里的“相等”，包括代表类的Class对象的`equals()`方法，`isAssignableFrom()`方法，`isInstance()`方法的返回结果，也包括instanceof判断的情况。

类加载器与`instanceof`关键字演示
    
    /**
     * 类加载器与instanceof关键字演示
     */
    package org.fenixsoft.classloading;
     
    public class ClassLoaderTest {
    
        public static void main(String[] args) throws Exception {
            ClassLoader myLoader = new ClassLoader() {
    
                @Override
                public Class<?> loadClass(String name) throws ClassNotFoundException {
                    try {
                        String fileName = name.substring(name.lastIndexOf(".") + 1) + ".class";
                        InputStream is = getClass().getResourceAsStream(fileName);
                        if (is == null) {
                            return super.loadClass(name);
                        }
                        byte[] b = new byte[is.available()];
                        is.read(b);
                        return defineClass(name, b, 0, b.length);
                    } catch (Exception e) {
                        throw new ClassNotFoundException(name);
                    }
                };
            };
    
            Object obj = myLoader.loadClass("org.fenixsoft.classloading.ClassLoaderTest").newInstance();
    
            System.out.println(obj.getClass());
            System.out.println(obj instanceof ClassLoaderTest);
        }
    }
    
运行结果：

    class org.fenixsoft.classloading.ClassLoaderTest
    false
    
从上面的输出结果可以看出，这个对象确实是类`org.fenixsoft.classloading.ClassLoaderTest`实例化出的对象，但是用instanceof做所属类型检查返回了false，这是因为虚拟机中**存在了两个ClassLoaderTest类**，一个是系统应用程序加载器加载的，一个是diy类加载器加载的。
    
#### 7.3.2 双亲委派模型

1. 从Java虚拟机地角度看，只有两种不同的类加载器：
    1. **启动类加载器（Bootstrap ClassLoader）**：使用C++实现，是虚拟机的一部分。
    2. **其他所有类加载器**，由Java语言实现，独立于虚拟机，而且全部继承自抽象类java.lang.ClassLoader。
2. 从开发人员角度来看，类加载器可以分为三种：
    1. **启动类加载器（Bootstrap ClassLoader）**
        1. 负责将**存放在<JAVA_HOME>\lib目录**中的，或者**被-Xbootclasspath参数指定的路径**中的类库，加载到虚拟机内存中。
        2. 无法被Java程序直接引用。
    2. **扩展类加载器（Extension ClassLoader）**
        1. 负责加载**存放在<JAVA_HOME>\lib\ext目录**中的，或者**被java.ext.dirs系统变量所制定的路径**中的所有类库。
        2. 开发者可以直接使用扩展类加载器。
    3. **应用程序类加载器（Application ClassLoader）**
        1. 负责加载**用户类路径（ClassPath）上**所指定的类库。
        2. 开发者可以直接使用应用程序类加载器。

![](https://ws3.sinaimg.cn/large/006tNc79ly1fmjwua3iv4j30ic0f0mxq.jpg)

上图展示的类加载器之间的层次关系，被称为类加载器的**双亲委派模型（Parents Delegation Model）**。

双亲委派模型要求：
1. 除了顶层的启动类加载器外，其余的类加载器都**应该有自己的父类加载器**。

双亲委派模型的工作过程是：
1. 如果一个类加载器收到了类加载请求，它首先**不会自己去尝试加载**这个类，而是把这个请求**委派给父类加载器**去完成。
2. 依此类推直到**传送到顶层**的启动类加载器。
3. 只有当父加载器反馈自己**无法完成**这个加载请求时，子加载器才会尝试自己去加载。

使用双亲委派模型的好处是：
1. Java类随着它的类加载器一起具备了一种带有优先级的层次关系。
    > 例如java.lang.Object，它存放在rt.jar中，无论那个类加载器要加载这个类，最终都会**委派给启动类加载器**，所以Object类在程序的各种类加载器环境中都是同一个类。
    >> 相反，如果没有使用双亲委派模型，由各个类加载器自行加载，如果用户自己写了一个java.lang.Object类，并放在程序的ClassPath中，那系统中会有多个不同的Object类
    >>
    >> 如果自己写一个与rt.jar类库中已有类重名的Java类，放在ClassPath下，你会发现可以正常编译，但是永远无法被加载运行（应该是，层层向上之后，启动类加载器在自己的目录底下找到了rt.jar类库，并在其中找到了相同名称的类，并完成了加载。）
```
    protected synchronized Class<?> loadClass(String name, boolean resolve)
            throws ClassNotFoundException {
        // First, check if the class has already been loaded
        // 首先，检查请求的类是否已经被加载过了
        Class<?> c = findLoadedClass(name);
        if (c == null) {
            try {
                if (parent != null) {
                    c = parent.loadClass(name, false);
                } else {
                    c = findBootstrapClassOrNull(name);
                }
            } catch (ClassNotFoundException e) {
                // ClassNotFoundException thrown if class not found
                // from the non-null parent class loader
                // 如果父类加载器抛出ClassNotFoundException
                // 则说明父类加载器无法完成加载请求
            }
            if (c == null) {
                // If still not found, then invoke findClass in order
                // to find the class.
                // 在父类加载器无法加载的时候
                // 再调用本身的findClass方法来进行加载
                long t1 = System.nanoTime();
                c = findClass(name);
            }
        }
        if (resolve) {
            resolveClass(c);
        }
        return c;
    }
```
上面的代码是**双亲委派模型的实现**，存在于java.lang.ClassLoader的`loadClass()`方法中：
1. 先检查是否已经**被加载过**。
2. 没有加载则**调用父类加载器**的`loadClass(`)方法。
3. 若父加载器为空，则默认启动启动类加载器作为父加载器。
4. 父加载器加载失败，抛出ClassNotFoundException异常，并调用自己的`findClass()`方法进行加载。

#### 7.3.3 破坏双亲委派模型

没看，大概是对老代码的妥协，设计漏洞，为了热部署等功能扩展。

























