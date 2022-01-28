# aop原里

1. 为什么用代理：增强类/方法的功能，便于扩展。
2. 动态代理：通用的代理，可以避免反复创建多个代理类。
3. jdk的Proxy：
   1. 只能为接口生成代理类。
   2. 主要通过getProxyClass和newProxyInstance来实现代理。
   3. 因为代理类默认继承Proxy类，所以只能实现接口。
4. cglib：
   1. 可以为普通类（非接口）生成代理类。
   2. 本质上它是通过动态的生成一个子类（继承和重写父类方法）去覆盖所要代理的类（非final修饰的类和方法）
   3. cglib的工作流程：
      1. 根据父类，Callback，Filter及一些相关信息生成key。
      2. 根据key生成对应的子类的二进制表现形式。
      3. 使用ClassLoader装载对应的二进制，生成Class对象，并缓存。
      4. 实例化Class对象。
   4. lazyLoader功能
      1. 延迟加载功能。
      2. cglib用于实现懒加载的callback。当被增强bean的方法初次被调用时，会触发回调，之后每次再进行方法调用调的都是缓存里的这个bean。
   5. Dispatcher功能：
      1. 跟lazyLoader类似，但是每次对被代理bean进行方法调用，都会触发回调（感觉跟MethodInterceptor的intercept也类似）。
      2. 可以对被代理类进行增强，使它有一些其他接口中的功能（有点复杂，看引用2，案例6）。
5. jdk动态代理和cglib动态代理的区别。

   |类型|jdk|cglib|
   |---|---|---|
   |原理|利用反射生成一个实现代理接口的匿名类，再调用方法前调用InvokeHandler|通过asm，动态生成一个（代理类）的子类来实现增强|
   |核心类|Proxy，创建代理，实例化代理类|Enhancer，主要增强类，通过字节码技术动态创建（代理类的）子类实例|
   |核心类|InvocationHandler，拦截器接口，需要实现invoke方法|MethodInterceptor，拦截器接口，需要实现intercept方法|
   |局限性|只能代理接口|可以代理类，但是不能代理final|
6. aop概念
   1. target：需要被增强的类（对象）。
   2. joinPint：需要被增强的类中的方法（方法前/后/环绕/抛出等等）。
   3. proxy：aop通过代理，为目标对象生成一个代理对象，来实现增强功能。
   4. advice：具体的增强操作（比如说打日志，鉴权等）。
   5. pointCut：一个筛选规则，可以从代码中筛选出joinPoint。
   6. aspect：切面，advice+pointCut。（跟advisor配置方式不太一样，另外，advisor大多用于管理事务，aspect用于日志缓存）
   7. advisor：Pointcut与Advice的组合。
7. 面试题
   1. 对AOP的理解。
   2. AOP中常见的概念。
   3. Spring中的通知有哪一些类型。
   4. Spring中事务管理的方式有哪些。
   5. AOP有哪些实现方式。
   6. Spring事务中的隔离级别。
   7. Spring事务中的传播行为。
8. Spring如何选择创建代理的方式：
   1. 如果设置proxyTargetClass=true，用cglib代理。
   2. 如果proxyTargetClass=false，目标实现了接口，用jdk代理。
   3. 如果proxyTargetClass=false，没实现接口，用cglib代理。
9. 

## 引用
>1. [Spring系列第15篇：代理详解（java动态代理&CGLIB代理)](https://mp.weixin.qq.com/s?__biz=MzA5MTkxMDQ4MQ==&mid=2648934082&idx=1&sn=c919886400135a0152da23eaa1f276c7&chksm=88621efcbf1597eab943b064147b8fb8fd3dfbac0dc03f41d15d477ef94b60d4e8f78c66b262&token=1042984313&lang=zh_CN&scene=21#wechat_redirect)
>2. [Spring系列第30篇：jdk动态代理和cglib代理](https://mp.weixin.qq.com/s?__biz=MzA5MTkxMDQ4MQ==&mid=2648934783&idx=1&sn=5531f14475a4addc6d4d47f0948b3208&chksm=88621141bf159857bc19d7bb545ed3ddc4152dcda9e126f27b83afc2e975dee1682de2d98ad6&token=1672930952&lang=zh_CN&scene=21#wechat_redirect)
>3. [Spring系列第31篇：Aop概念详解](https://mp.weixin.qq.com/s?__biz=MzA5MTkxMDQ4MQ==&mid=2648934876&idx=1&sn=7794b50e658e0ec3e0aff6cf5ed4aa2e&chksm=886211e2bf1598f4e0e636170a4b36a5a5edd8811c8b7c30d61135cb114b0ce506a6fa84df0b&token=1672930952&lang=zh_CN&scene=21#wechat_redirect)
>4. [Aop总结](https://mp.weixin.qq.com/s/8B0cgwIREzhFWo7QNBLGug `AOP，确实难，会让很多人懵逼，那是因为你没有看这篇文章！`)
>5. [Spring之AOP面试题](https://mp.weixin.qq.com/s/rpKmCsqg_Ry7rcOTMF_-7A)
>6. [Spring 如何实现 AOP](https://mp.weixin.qq.com/s/XMCnBcmTlrU9AidRbm2V3A)
>7. [JDK动态代理[4]----ProxyGenerator生成代理类的字节码文件解析](https://www.cnblogs.com/liuyun1995/p/8144706.html `介绍了为什么jdk只能代理接口，这篇是4，还有123可以看看`)