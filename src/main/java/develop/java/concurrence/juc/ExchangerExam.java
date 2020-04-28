package develop.java.concurrence.juc;

import java.util.Random;
import java.util.concurrent.Exchanger;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author admin
 */
public class ExchangerExam {

    public static void main(String[] args) {
        Exchanger<String> exchanger = new Exchanger<>();
        ExecutorService service = Executors.newCachedThreadPool();
        service.submit(new StringHolder("LeftHand", "LeftValue", exchanger));
        service.submit(new StringHolder("RightHand", "RightValue", exchanger));
        service.shutdown();
    }

    private static class StringHolder implements Runnable {

        private final String name;

        private final String val;

        private final Exchanger<String> exchanger;

        private static Random rand = new Random(System.currentTimeMillis());

        StringHolder(String name, String val, Exchanger<String> exchanger) {
            this.name = name;
            this.val = val;
            this.exchanger = exchanger;
        }

        @Override
        public void run() {
            try {
                System.out.println(name + " hold the val:" + val);
                TimeUnit.SECONDS.sleep(rand.nextInt(5));
                String str = exchanger.exchange(val);
                System.out.println(name + " get the val:" + str);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
