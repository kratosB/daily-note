# ioc原理

1. 用了工厂模式
2. BeanFactory接口是spring核心的bean工厂定义，它是IOC容器的顶层接口，spring中所有bean工厂都直接或间接的继承或实现了这个接口。平时使用的最多的ApplicationContext接口也继承了BeanFactory接口。
   1. 持有各种bean的定义。（只有拿到了bean的定义信息，才能根据这些信息进行实例化）
   2. 持有各种bean之间的依赖关系。（如果一个类中持有对另一个类的引用，那么在对该类进行实例化时，必须根据类之间的依赖关系对相关类也进行实例化）
   3. 工厂需要一个工具来读取配置文件的信息。（以上两种信息都依赖于我们的配置信息定义，比如xml配置文件）
3. BeanDefinition，是spring中的bean定义接口，spring的工厂里持有的就是此接口定义的内容。
   1. 这个接口继承了两个另外两个接口：
      1. AttributeAccessor接口：继承这个接口就意味着BeanDefinition接口拥有了处理属性的能力
      2. BeanMetedataElement接口：它可以获得bean的配置定义的元素，对于xml文件来说就是会持有bean的标签。
   2. BeanDefinition存在于BeanFactory的实现类（例如DefaultListableBeanFactory）中，以`Map<String, BeanDefinition> beanDefinitionMap`的形式，key是bean名，value是bean信息。
4. 将xml文件配置的bean注册到这个beanDefinitionMap对象里的逻辑。
   1. 需要一个工具来找到xml配置文件，可以称之为资源定位。
   2. 需要一个Reader来读取xml配置信息，即DOM解析。
   3. 将读取出来的信息注册到map对象里。（判断是不是单例啦，什么的，实现挺复杂的）
5. 从工厂中获取Bean的时候，先根据getBean(name)中的name从beanDefinitionMap获取bean的信息，然后用类加载器（或者反射）完成加载。注入属性。

## 引用

>1. [深度解析spring源码IOC原理](https://mp.weixin.qq.com/s/CSYHUBdIP0D3dt5dOkaxew)
>2. [看了绝对不会后悔之：spring Ioc原理](https://mp.weixin.qq.com/s/c_F1pva08rALLE0oqDz8YQ)