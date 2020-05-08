package develop.java.concurrency.tool;

import java.util.Random;
import java.util.concurrent.Phaser;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created on 2020/5/6.
 *
 * @author zhiqiang bao
 */
public class PhaserDemo {
    static class PreTaskThread implements Runnable {

        private String task;
        private Phaser phaser;

        public PreTaskThread(String task, Phaser phaser) {
            this.task = task;
            this.phaser = phaser;
        }

        @Override
        public void run() {
            for (int i = 1; i < 4; i++) {
                try {
                    // 第二次关卡起不加载NPC，跳过
                    Random random = new Random();
                    Thread.sleep(random.nextInt(1000));
                    System.out.println(String.format("关卡%d，需要加载%d个模块，当前模块【%s】",
                            i, phaser.getRegisteredParties(), task));

                    // 从第二个关卡起，不加载NPC
                    if (i == 1 && "加载新手教程".equals(task)) {
                        System.out.println("下次关卡移除加载【新手教程】模块");
                        phaser.arriveAndDeregister(); // 移除一个模块
                        break;
                    } else {
                        phaser.arriveAndAwaitAdvance();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) {
        Phaser phaser = new Phaser(4) {
            @Override
            protected boolean onAdvance(int phase, int registeredParties) {
                System.out.println(String.format("第%d次关卡准备完成", phase + 1));
                return phase == 3 || registeredParties == 0;
            }
        };

        ThreadPoolExecutor gamePoolExecutor = new ThreadPoolExecutor(10, 10,
                60L, TimeUnit.SECONDS,
                new SynchronousQueue<>());
        gamePoolExecutor.submit(new PreTaskThread("加载地图数据", phaser));
        gamePoolExecutor.submit(new PreTaskThread("加载人物模型", phaser));
        gamePoolExecutor.submit(new PreTaskThread("加载背景音乐", phaser));
        gamePoolExecutor.submit(new PreTaskThread("加载新手教程", phaser));
        gamePoolExecutor.shutdown();
    }
}
