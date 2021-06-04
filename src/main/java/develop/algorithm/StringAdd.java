package develop.algorithm;

/**
 * Created on 2021/6/1.
 *
 * @author zhiqiang bao
 */
public class StringAdd {

    public static void main(String[] args) {
        add("9999100010009805", "199990000545");
        add("987654321", "123456789");
    }

    private static void add(String s1, String s2) {
        StringBuilder sb = new StringBuilder();
        char[] chars1 = s1.toCharArray();
        char[] chars2 = s2.toCharArray();
        int size;
        int length1 = chars1.length;
        int length2 = chars2.length;
        size = Math.min(length1, length2);
        int addOne = 0;
        for (int i = 0; i < size; i++) {
            int c = chars1[length1 - i - 1] - '0';
            int c1 = chars2[length2 - i - 1] - '0';
            int result = c + c1 + addOne;
            if (result >= 10) {
                sb.insert(0, (result - 10));
                addOne = 1;
            } else {
                sb.insert(0, result);
                addOne = 0;
            }
        }
        if (length1 <= length2) {
            for (int i = size; i < length2; i++) {
                int c = chars2[length2 - i - 1] - '0';
                int result = c + addOne;
                if (result >= 10) {
                    sb.insert(0, (result - 10));
                    addOne = 1;
                } else {
                    sb.insert(0, result);
                    addOne = 0;
                }
            }
        } else {
            for (int i = size; i < length1; i++) {
                int c = chars1[length1 - i - 1] - '0';
                int result = c + addOne;
                if (result >= 10) {
                    sb.insert(0, (result - 10));
                    addOne = 1;
                } else {
                    sb.insert(0, result);
                    addOne = 0;
                }
            }
        }
        if (addOne == 1) {
            sb.insert(0, 1);
        }
        System.out.println(sb);
    }
}
