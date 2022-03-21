## ReentrantLock的lock

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
5. AQS中的addWaiter()
   1. 先把当前线程包装成node。然后看看队列是不是空。不是空的话，用cas尝试入队。
   2. 如果队列是空/cas尝试入队失败，调用enq()方法。
   3. enq方法会1. cas初始化队列，2. 自选+cas尝试入队（队尾）。
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

## ReentrantLock的unlock

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
    3. unpark这个线程。
5. 这里唤醒的线程，会回到AQS的acquire()的acquireQueued()的parkAndCheckInterrupt()的LockSupport.park(this);这一行。
6. 根据Doug lea的注释，waitStatus=-1代表的是下个节点需要被唤醒。那么我猜测0就是没有后续节点的意思。-2，-3才是自己节点的状态。

## ReentrantLock的公平非公平

1. 非公平锁在调用 lock 后，首先就会调用 CAS 进行一次抢锁，如果这个时候恰巧锁没有被占用，那么直接就获取到锁返回了。
2. 非公平锁在 CAS 失败后，和公平锁一样都会进入到 tryAcquire 方法，在 tryAcquire 方法中，如果发现锁这个时候被释放了（state == 0），非公平锁会直接 CAS 抢锁，但是公平锁会判断等待队列是否有线程处于等待状态，如果有则不去抢锁，乖乖排到后面。

## Condition


## ReentrantLock的公平非公平
## ReentrantLock的公平非公平