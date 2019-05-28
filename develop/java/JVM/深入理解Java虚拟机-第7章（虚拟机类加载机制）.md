## 第七章 虚拟机类加载机制

1. 虚拟机把描述类的数据从Class文件**加载**到内存，并对数据进行**校验**，**转换解析**和**初始化**，最终形成可以被虚拟机直接使用的Java类型，这就是虚拟机的类加载机制。
2. Java语言里，类型的**加载**和**连接**过程都是在程序**运行期间**完成的。

### 7.1 类加载的时机

![类的生命周期](https://img-blog.csdn.net/20180725191039947?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2dhbjc4NTE2MDYyNw==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

1. 类从被加载到虚拟机内存种开始，到卸载出内存为止，它的整个生命周期包括了：
    1. 加载（Loading）
    2. 连接（Linking）
        1. 验证（Verification）
        2. 准备（Preparation）
        3. 解析（Resolution）
    3. 初始化（Initialization）
    4. 使用（Using）
    5. 卸载（Unloading）
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
    
这段代码运行只会没有输出`SuperClass init!`，说明**没有触发**SuperClass类的初始化。但是这段代码触发了Lorg.fenixsoft.classloading.SuperClass类的初始化阶段。这个类是**虚拟机自动生成**的，直接继承于Object的子类，创建动作由newarray触发。

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



#### 7.2.3 准备



#### 7.2.4 解析



#### 7.2.5 初始化



### 7.3 类加载器

























