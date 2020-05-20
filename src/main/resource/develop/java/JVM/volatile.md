# [一个volatile跟面试官扯了1个小时](https://mp.weixin.qq.com/s/R1D5tfuMVL-v8qQlZvPhmA)

## 1. 作用

1. 可见性
2. 有序性

## 2. 原理

1. 强制刷新缓存/内存。
2. 内存屏障。

## 3. 实例

1. 状态标志，true/false，来停止循环啥的。
    >volatile 很适合只有一个线程修改，其他线程读取的情况。volatile 变量被修改之后，对其他线程立即可见。
2. double-check的单例模式。
    >可见性+有序性。

# 引用

>[一个volatile跟面试官扯了1个小时](https://mp.weixin.qq.com/s/R1D5tfuMVL-v8qQlZvPhmA)