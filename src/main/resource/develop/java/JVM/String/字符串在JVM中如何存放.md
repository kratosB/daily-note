[蚂蚁面试：字符串在JVM中如何存放？](https://mp.weixin.qq.com/s/bVSMSd606UdR2aBD1YJtJw)

1. JVM中字符串通常存放在**常量池**或**堆内存**。
2. String类中提供了一个`java.lang.String.intern()`方法，可以手动将一个字符串对象的值移动到**常量池**中。
3. 1.7之前，字符串常量池在**PermGen区域**。在1.7以后，字符串常量池移到了**堆内存中**，并且可以被垃圾收集器回收。
> 因为1.7的PermGen区域是固定大小的，不能在运行时根据需求扩大，也不能回收，所以如果序中有太多的字符串调用了intern方法的话，就可能造成OutOfMemory。

## 案例分析

![图1](https://mmbiz.qpic.cn/mmbiz_png/8Jeic82Or04kYTcicWulT8o9vLjWSkUkV2qpf141t7AKQG3VFQdyicwrzWjrDGoHlkzbgu1G5eVic1aYiacicaR2IeDw/640)

验证代码：

    public class StringTest {
    
        public static void main(String[] args) {
            String s1 = "java1";
            String s2 = "java1";
            String s3 = new String("java1");
            // true
            System.out.println(s1==s2);
            // false
            System.out.println(s1==s3);
            
            String s4 = s3.intern();
            System.out.println(s1==s4);
        }
    }

## intern源码分析

ntern方法的实现底层是一个native方法，在Hotspot JVM里字符串常量池它的逻辑在注释里写得很清楚：如果常量池中有这个字符串常量，就直接返回，否则将该字符串对象的值存入常量池，再返回。

![图2](https://mmbiz.qpic.cn/mmbiz_jpg/8Jeic82Or04kYTcicWulT8o9vLjWSkUkV2jRYXeMM1bDQiaowyXg8p2VmKDNVbbNcJWIicXdKTAd9Gn0VQibs7SZWIg/640)

这里以Openjdk1.8的源码为例，跟下intern方法的底层实现，String.java文件对应的C文件是String.c：

    JNIEXPORT jobject JNICALL
    Java_java_lang_String_intern(JNIEnv *env, jobject this)
    {
        return JVM_InternString(env, this);
    }

JVM_InternString这个方法的定义在jvm.h，实现在jvm.cpp中，在JVM中，Java世界和C++世界的连接层就是jvm.h和jvm.cpp这两文件。

    JVM_ENTRY(jstring, JVM_InternString(JNIEnv *env, jstring str))
        JVMWrapper("JVM_InternString");
        JvmtiVMObjectAllocEventCollector oam;
        if (str == NULL) return NULL;
        oop string = JNIHandles::resolve_non_null(str);
        oop result = StringTable::intern(string, CHECK_NULL);
        return (jstring) JNIHandles::make_local(env, result);
    JVM_END

可以看出，字符串常量池在JVM内部就是一个**HashTable**，也就是上面代码中的StringTable。

从`StringTable::intern`方法跟下去，就可以发现：如果找到了这次操作的字符串，就直接返回found_string；如果没有找到，就将当前的字符串加入到HashTable中，然后再返回。

## 总结

在Java应用恰当得使用String.intern()方法有助于节省内存空间，但是在使用的时候，也需要注意，因为StringTable的大小是固定的，如果常量池中的字符串过多，会影响程序运行效率。













未完待续，可以结合**参考资料**里的东西继续写。







## 参考资料

> [java中String常量的存储原理](https://www.cnblogs.com/hewenwu/p/3665632.html)