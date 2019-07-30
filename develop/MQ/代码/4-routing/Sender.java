import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 * Created on 2019/7/30.
 *
 * @author zhiqiang bao
 */
public class Sender {

    private static final String EXCHANGE_NAME = "direct_logs";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("192.168.99.100");
        try (Connection connection = connectionFactory.newConnection(); Channel channel = connection.createChannel()) {
            channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.DIRECT);
            while (true) {
                System.out.println(" Please input something: ");
                BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                String string = br.readLine();
                String[] split = string.split(",");
                String severity = getSeverity(split);
                String message = getMessage(split);
                channel.basicPublish(EXCHANGE_NAME, severity, null, message.getBytes(StandardCharsets.UTF_8));
                System.out.println(" [x] Sent '" + severity + "':'" + message + "'");
            }

        }
    }

    private static String getSeverity(String[] strings) {
        if (strings.length < 1)
            return "info";
        return strings[0];
    }

    private static String getMessage(String[] strings) {
        if (strings.length < 2)
            return "Hello World!";
        return joinStrings(strings);
    }

    private static String joinStrings(String[] strings) {
        int length = strings.length;
        if (length == 0)
            return "";
        if (length <= 1)
            return "";
        StringBuilder words = new StringBuilder(strings[1]);
        for (int i = 1 + 1; i < length; i++) {
            words.append(" ").append(strings[i]);
        }
        return words.toString();
    }
}
