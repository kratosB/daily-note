package develop.java.jvm;

/**
 * 这个类主要演示了类初始化加载的顺序
 * <p>
 * Created on 2020/4/30. https://mp.weixin.qq.com/s/jsZv2ZJKbOEtPjlLNgs0bQ
 *
 * @author zhiqiang bao
 */
public class InitOrderTest {

    public static void main(String[] args) {
        staticFunction();
    }

    static InitOrderTest st = new InitOrderTest();

    static {
        System.out.println("1");
    }

    {
        System.out.println("2");
    }

    InitOrderTest() {
        System.out.println("3");
        System.out.println("a=" + a + ",b=" + b);
    }

    public static void staticFunction() {
        System.out.println("4");
        System.out.println("b = " + b);
    }

    int a = 110;

    static int b = 112;
}
