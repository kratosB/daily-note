# [RabbitMQ的死信队列详解](https://www.jianshu.com/p/986ee5eb78bc)

## 1. 死信队列介绍

1. 死信队列：DLX，`dead-letter-exchange`。
2. 利用DLX，当消息在一个队列中变成死信 `dead message`之后，它能被重新publish到另一个Exchange，这个Exchange就是DLX。

## 2. 消息变成死信有以下几种情况

1. 消息被拒绝(basic.reject / basic.nack)，并且requeue = false。
2. 消息TTL过期。
3. 队列达到最大长度。

## 3. 死信处理过程

1. DLX也是一个正常的Exchange，和一般的Exchange没有区别，它能在任何的队列上被指定，实际上就是设置某个队列的属性。
2. 当这个队列中有死信时，RabbitMQ就会自动的将这个消息重新发布到设置的Exchange上去，进而被路由到另一个队列。
3. 可以监听这个队列（另一个队列）中的消息做相应的处理。

## 4. 死信队列设置

1. 首先需要设置死信队列的exchange和queue，然后进行绑定：
    ```
    Exchange: dlx.exchange
    Queue: dlx.queue
    RoutingKey: #
    #号表示只要有消息到达了Exchange，那么都会路由到这个queue上
    ```
2. 然后需要有一个监听，去监听这个队列（dlx.queue）进行处理。
3. 然后我们进行正常声明交换机、队列、绑定，只不过我们需要在队列加上一个参数`arguments.put(" x-dead-letter-exchange"，"dlx.exchange");`，这样消息在过期、requeue、 队列在达到最大长度时，消息就可以直接路由到死信队列。

## 5. 死信队列演示（代码在MQ的代码目录下的6-死信队列中）

1. 生产者发送消息时，设置一个超时时间。
2. 声明正常处理消息的交换机`test_dlx_exchange`、队列`test_dlx_queue`及绑定规则`dlx.#`。
3. 在正常队列`test_dlx_queue`上指定死信的处理规则`arguments.put("x-dead-letter-exchange", "dlx.exchange")`。
4. 声明死信交换机`dlx.exchange`、队列`dlx.queue`及绑定规则`#`。
5. 监听死信队列，进行后续处理`channel.basicConsume("dlx.queue", true, new MyConsumer(channel));`。

## 6. 运行说明

1. 启动消费端，此时查看管控台，新增了两个Exchange，两个Queue。在`test_dlx_queue`上我们设置了DLX，也就代表死信消息会发送到指定的Exchange`dlx.exchange`上，最终其实会路由到`dlx.queue`上。
    ![1](https://upload-images.jianshu.io/upload_images/14795543-76d69a56ef1ad6a7.png)
2. 启动生产端，查看管控台队列的消息情况，`test_dlx_queue`的值为1，而`dlx_queue`的值为0。
3. 10s后的队列结果如图，由于生产端发送消息时指定了消息的过期时间为10s，而此时没有消费端进行消费，消息便被路由到死信队列中。
    ![2](https://upload-images.jianshu.io/upload_images/14795543-2c020cefbd1820ce.png)
4. 然后被监听死信队列的消费者处理，消费者输出消息详细信息。

## 引用
>1. [RabbitMQ的死信队列详解](https://www.jianshu.com/p/986ee5eb78bc)
>