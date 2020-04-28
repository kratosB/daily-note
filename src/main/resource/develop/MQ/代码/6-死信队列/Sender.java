import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 * Created on 2019/8/1.
 *
 * @author zhiqiang bao
 */
public class Sender {

    public static void main(String[] args) throws Exception {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("192.168.99.100");
        Connection connection = connectionFactory.newConnection();
        Channel channel = connection.createChannel();

        AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder().deliveryMode(2).contentEncoding("UTF-8")
                .expiration("10000").build();
        String message = "Hello RabbitMQ DLX Message";
        channel.basicPublish("test_dlx_exchange", "dlx.save", true, properties, message.getBytes());
    }
}
