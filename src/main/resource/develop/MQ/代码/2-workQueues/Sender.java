import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 * Created on 2019/7/30.
 *
 * @author zhiqiang bao
 */
public class Sender {

    private static final String TASK_QUEUE_NAME = "task_queue";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("192.168.99.100");
        try (Connection connection = connectionFactory.newConnection(); Channel channel = connection.createChannel()) {
            channel.queueDeclare(TASK_QUEUE_NAME, true, false, false, null);
            while (true) {
                System.out.println(" Please input something: ");
                BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                String s = br.readLine();
                String message = String.join(" ", s);
                channel.basicPublish("", TASK_QUEUE_NAME, null, message.getBytes(StandardCharsets.UTF_8));
                System.out.println(" [x] Sent '" + message + "'");
            }
        }
    }
}
