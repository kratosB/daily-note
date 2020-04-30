package develop.java.jvm;

/**
 * 这个类主要演示了类初始化加载的顺序
 * <p>
 * Created on 2020/4/30.
 *
 * @author zhiqiang bao
 */
public class InitOrderTest {

    //    static InitOrderTest st = new InitOrderTest();

    static {
        System.out.println("1");
    }

    static int b = 112;

    InitOrderTest() {
        System.out.println("3");
        System.out.println("a=" + a + ",b=" + b);
    }

    public static void staticFunction() {
        System.out.println("4");
        System.out.println("b = " + b);
    }

    int a = 110;

    {
        System.out.println("2");
    }

    // static int b = 112;

    public static void main(String[] args) {
        staticFunction();
    }
}
