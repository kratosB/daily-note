package develop.java.concurrency.threadpool;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 定时任务打印消息。Created on 2020/5/8.
 *
 * @author zhiqiang bao
 */
public class ScheduledThreadPoolExecutorDemo {

    public static void main(String[] args) {
        ScheduledExecutorService executor = new ScheduledThreadPoolExecutor(1, Executors.defaultThreadFactory());
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        // 新建一个固定延迟时间的计划任务
        executor.scheduleWithFixedDelay(() -> {
            System.out.println(df.format(new Date()));
            System.out.println("大家注意了，我要发消息了");
        }, 1, 1, TimeUnit.SECONDS);
    }

}
