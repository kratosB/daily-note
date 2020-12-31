# Redis实例

## 1. [公司有十万员工，分成500个部门，公司为员工制定了每日9点前和18点后网上签到的制度，签到之后可以及时查看自身签到状态，主管可以及时收到下属员工的签到状态，一整天未签到的员工自动补充旷工](https://blog.csdn.net/wdcl2468/article/details/93360546)



## 2 [Spring之缓存注解@Cacheable](https://www.cnblogs.com/yuluoxingkong/p/10143810.html)

1. @Cacheable: 使用@Cacheable标记的方法在执行后Spring Cache将缓存其返回结果。
2. @CachePut: 与@Cacheable不同的是使用@CachePut标注的方法在执行前不会去检查缓存中是否存在之前执行过的结果，而是每次都会执行该方法，并将执行结果以键值对的形式存入指定的缓存中。
3. @CacheEvict: 使用@CacheEvict标记的方法会在方法执行前或者执行后移除Spring Cache中的某些元素。
4. @Caching：@Caching注解可以让我们在一个方法或者类上同时指定多个Spring Cache相关的注解。其拥有三个属性：cacheable、put和evict，分别用于指定@Cacheable、@CachePut和@CacheEvict。
5. CacheConfig：

[代码gti地址](http://git.oschina.net/gpy1994/redisdemo)

## 引用
>1. [Redis场景应用实例](https://blog.csdn.net/wdcl2468/article/details/93360546)
>2. [redis缓存实践](https://www.jianshu.com/p/8abbcf51a2ba)
>3. [Spring之缓存注解@Cacheable](https://www.cnblogs.com/yuluoxingkong/p/10143810.html)