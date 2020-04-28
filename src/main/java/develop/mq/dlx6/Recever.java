package develop.mq.dlx6;

import java.util.HashMap;
import java.util.Map;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

/**
 * Created on 2019/8/1.
 *
 * @author zhiqiang bao
 */
public class Recever {

    public static void main(String[] args) throws Exception {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("192.168.99.100");
        Connection connection = connectionFactory.newConnection();
        Channel channel = connection.createChannel();

        String exchangeName = "test_dlx_exchange";
        String queueName = "test_dlx_queue";
        String bindingKey = "dlx.#";
        boolean durable = true;
        boolean autoDelete = true;
        boolean exclusive = false;
        Map<String, Object> arguments = new HashMap<>();
        String key = "x-dead-letter-exchange";
        String dlxExchange = "dlx.exchange";
        arguments.put(key, dlxExchange);

        // 普通队列，死信会被转到arguments里面配置的exchange中
        channel.exchangeDeclare(exchangeName, BuiltinExchangeType.TOPIC, durable, autoDelete, null);
        channel.queueDeclare(queueName, durable, exclusive, autoDelete, arguments);
        channel.queueBind(queueName, exchangeName, bindingKey);

        String dlxExchangeName = "dlx.exchange";
        String dlxQueueName = "dlx.queue";
        String dlxBindingKey = "#";

        // 死信的exchange和队列
        channel.exchangeDeclare(dlxExchangeName, BuiltinExchangeType.TOPIC, durable, autoDelete, null);
        channel.queueDeclare(dlxQueueName, durable, exclusive, autoDelete, null);
        channel.queueBind(dlxQueueName, dlxExchangeName, dlxBindingKey);

        channel.basicConsume(dlxQueueName, true, new MyConsumer(channel));
    }
}

class MyConsumer extends DefaultConsumer {

    public MyConsumer(Channel channel) {
        super(channel);
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) {
        System.err.println("-----------consume message----------");
        System.err.println("consumerTag: " + consumerTag);
        System.err.println("envelope: " + envelope);
        System.err.println("properties: " + properties);
        System.err.println("body: " + new String(body));
    }
}
