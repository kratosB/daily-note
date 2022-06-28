# AQS

## AQS独占锁

### ReentrantLock的lock

1. ReentrantLock内部有一个sync，sync继承了AQS，加解锁都是sync实现的。
2. ReentrantLock的lock方法，会调用sync的lock方法。sync的lock方法，会调用AQS的acquire方法。
   1. 公平锁直接调用AQS的acquire。
   2. 非公平会直接先cas试一下能不能抢到锁，抢不到再调AQS的acquire。
3. AQS的acquire的主要功能是：
   1. 调用tryAcquire():
      1. 这个方法是尝试获取锁。
      2. AQS中没有对应的实现，留给sync实现。如果sync没实现，那就会调用AQS中的方法，抛出UnsupportedOperationException。
      3. **不同的工具的主要差距就在tryAcquire()方法。**
   2. addWaiter()
      1. 当前线程入队。
   3. acquireQueued()
      1. 尝试获取锁或入队挂起。
   4. selfInterrupt()
4. ReentrantLock的tryAcquire()方法。
   1. 获取当前state（表示当前是否加锁，0表示没有，等于1表示已经加锁，大于1表示已经加锁并重入）。
   2. 如果state==0，判断一下（1. 队列是否是空，2. 队列里第一个是不是当前线程）大概就是轮到你没有。
      1. 如果state==0 && !hasQueuedPredecessors() == true（队列是空，或者队列第一个是你），尝试cas获取锁。
      2. 如果获取到了，设置独占锁当前所有人是当前线程。
      3. 如果没获取到，那就说明被同期其他线程获取了。
   3. 如果state!=0，说明已经加锁，判断当前线程是不是锁的持有人，是的话重入。
   4. 其他情况，返回尝试获取锁失败。
   5. 非公平版本的区别：在下一章
5. AQS中的addWaiter()
   1. 先把当前线程包装成node。然后看看队列是不是空。不是空的话，用cas尝试入队。
   2. 如果队列是空/cas尝试入队失败，调用enq()方法。
   3. enq方法
      1. cas初始化队列。
      2. 自旋+cas尝试入队（队尾）。
6. AQS中的acquireQueued()
   1. 判断当前是否在队首，如果是，尝试获取锁（1. 挂起很耗资源，所以再试试。2. 这段代码是公用的，所以此时可能是非公平锁，要跟人家抢）。
      1. 如果获取锁成功，就把当前设置成head。
      2. 如果获取锁失败，1. shouldParkAfterFailedAcquire()方法判断是否需要挂起，2. 挂起线程。
7. AQS中的shouldParkAfterFailedAcquire()
   1. 判断前节点状态
      1. -1，正常，返回true，进入挂起方法。
      2. 1，取消，那就找前前节点，前前前节点，直到找到/找不到。返回false，不挂起，进入acquireQueued()方法中的循环。
      3. 其他（0，-2，-3），把前节点状态设置成-1。一个node刚进来的时候，waitStatus就是0，需要等后续节点帮他设置。返回false，不挂起，进入acquireQueued()方法中的循环。
   2. 既然有前节点，为什么要返回false，步挂起，重新走循环了。是为了避免挂起的时候，前节点已经没了，那么就白挂了。

### ReentrantLock的unlock

1. ReentrantLock的unlock方法，会调用sync的release方法。sync的release方法继承自AQS的release方法。（sync没有先自己写release再调AQS）
   1. lock包装了两层，是因为分成公平锁非公平锁。而unlock只有一种，所以就不用再包装了，有需要的话可以包装。
2. AQS的release的主要功能是：
   1. 调用tryRelease():
      1. 这个方法是尝试解锁。
      2. AQS中没有对应的实现，留给sync实现。如果sync没实现，那就会调用AQS中的方法，抛出UnsupportedOperationException。
      3. **不同的工具的主要差距就在tryRelease()方法。**
   2. 如果解锁成功，判断队列中有东西。
   3. unparkSuccessor()
3. ReentrantLock的tryRelease()方法。
   1. 判断当前线程是不是持有锁的线程，不是的话抛出异常。
   2. 判断解锁后state是不是0。
      1. =0，说明完全解锁，当前持有锁的线程设置成null（setExclusiveOwnerThread(null);），返回true（成功）。
      2. !=0，没解锁完（说明重入了），设置新的state（剩余次数），返回false（失败）。
4. AQS中的unparkSuccessor()
   1. 判断当前的节点的waitStatus，改为0（应该是结束的意思，可以理解为非等待非取消非条件等等）
   2. 判断后一个节点是不是null，状态是不是cancel。
      1. 不是的话直接返回。
      2. 是的话从tail开始往前找，找到最早的那个不是null且状态不是cancel的节点。
         >往前找是有原因的，因为cas之前会先设置node.prev=tail，所以往前找肯定能找到。往后找的话，能cas之后挂起了，tail.next=node这一行还没走到。
   3. unpark这个线程。
5. 这里唤醒的线程，会回到AQS的acquire()的acquireQueued()的parkAndCheckInterrupt()的LockSupport.park(this);这一行。
6. 根据Doug lea的注释，waitStatus=-1代表的是下个节点需要被唤醒。那么我猜测0就是没有后续节点的意思。-2，-3才是自己节点的状态。

### ReentrantLock的公平非公平

1. 非公平锁在调用lock后，首先就会调用CAS进行一次抢锁，如果这个时候恰巧锁没有被占用，那么直接就获取到锁返回了。
2. 非公平锁在CAS失败后，和公平锁一样都会进入到tryAcquire方法，在tryAcquire方法中，如果发现锁这个时候被释放了（state == 0），非公平锁会直接CAS抢锁，但是公平锁会判断等待队列是否有线程处于等待状态，如果有则不去抢锁，乖乖排到后面。

### Condition

1. condition主要维护了一个条件队列
   1. 调用await的线程被添加到条件队列并park。
   2. 被其他线程signal之后会进入同步队列（然后等待唤醒）。
   3. 被interrupt之后会被唤醒，然后进入同步队列。
2. Node类当中的nextWaiter，就是条件队列的实现。Condition类中会存firstWaiter和lastWaiter。
3. await
   1. 加入条件队列。
   2. 完全释放锁。
   3. park（并循环）。
   4. 被唤醒之后再获取锁。
4. signal
   1. 将条件队列中的第一个（非cancel和interrupt的）node转移到同步队列。
   2. 如果同步队列没有东西，直接唤醒这个node。
5. await的线程，会调用LockSupport.park挂起。只有三种办法可以unpark。
   1. signal之后，node被移动到同步队列，然后被unlock中的release unpark。（没有signal直接unlock的话，会因为这个线程在条件队列（所以同步队列没东西），触发`if (h != null && h.waitStatus != 0)`的第一或者第二个条件，导致直接跳过`unparkSuccessor(h);`这个方法，直接返回true，所以之前park的方法就没有被unpark。这个地方大概也说明了，unlock（之中的release）只能unpark同步队列里等待的线程。）
   2. signal中的transferForSignal，会在前驱节点取消，或者前驱节点的waitStatus不能被cas成-1（可能代表变成condition或者已经没有了）的时候，唤醒当前node（此时node已经在同步队列了）。
   3. 线程被interrupt了，会自动唤醒，然后根据中断情况跳出循环。 
   所以除非是中断，不然必须得signal。
6. 实际上AQS里面也就只有两个`LockSupport.unpark()`。所以await里面的park应该只有signal和interrupt能唤醒。除此之外，parkNano，parkUntil之类的，自己会醒。

## AQS共享锁

### CountDownLatch

1. 主要应用：
   1. 辅线程await，主线程countDown，让多个辅线程同时开始工作。
   2. 主线程await，辅线程countDown，主线程等待多个辅线程工作完，再继续往下执行。
2. 有一个区别：reentrantLock中的acquire是申请执行，countDownLatch中的acquire是申请排队。
3. AQS的`doReleaseShared`方法中，unparkSuccessor会唤醒后继节点。后继节点被唤醒之后，会执行`setHeadAndPropagate`方法，还会唤醒后继的后继节点，此时会有多个`doReleaseShared`方法并行在工作，所以cas有可能会失败。

### CyclicBarrier

1. CyclicBarrier就是ReentrantLock和Condition的一个实践，大多操作都是lock之后的操作，不考虑ReentrantLock和Condition的代码，比较简单。

## 引用
1. [由浅入深逐步讲解Java并发的半壁江山AQS](https://mp.weixin.qq.com/s/bxWgo9IuggDpE1l37JqEhQ)
2. [Java Concurrency代码实例之四-锁](https://zhuanlan.zhihu.com/p/27546231)
3. [AQS-为什么队列头节点的Thread是null](https://blog.csdn.net/weixin_38106322/article/details/107141976)
4. [一行一行源码分析清楚AbstractQueuedSynchronizer](https://www.javadoop.com/post/AbstractQueuedSynchronizer)
5. [AQS唤醒线程的时候为什么从后向前遍历，我懂了](https://blog.csdn.net/qq_37699336/article/details/124294697)
   1. 主要就是说，同步队列设置队尾，然后`t.next=node`的时候，cas是有保障的，但是if里面的内容（`t.next=node`）不是线程安全的，可能在这里上下文切换了，那么t.next=null。从前往后就会出错，但是从后往前不会，因为之前设置过`node.prev=t`。
   2. 这个问题同时解释了，为什么要先`node.prev=t`，然后再`compareAndSetTail`。在极端情况下可能会发生。