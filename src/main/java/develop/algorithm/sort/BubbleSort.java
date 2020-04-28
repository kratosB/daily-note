package develop.algorithm.sort;

/**
 * 冒泡排序的时间复杂度是O(N2)。
 * 假设被排序的数列中有N个数。遍历一趟的时间复杂度是O(N)，需要遍历多少次呢？N-1次！因此，冒泡排序的时间复杂度是O(N2)。
 * <p>
 * 冒泡排序是稳定的算法，它满足稳定算法的定义。
 * 算法稳定性 -- 假设在数列中存在a[i]=a[j]，若在排序之前，a[i]在a[j]前面；并且排序之后，a[i]仍然在a[j]前面。则这个排序算法是稳定的！
 * <p>
 * Created on 2019/2/14.
 *
 * @author zhiqiang bao
 */
public class BubbleSort {

    public static void main(String[] args) {
        int[] a = new int[]{5, 1, 6, 9, 3, 2, 6, 8, 3, 5};
        BubbleSort bubbleSort = new BubbleSort();
        bubbleSort.bubbleSort(a);
        for (int v : a) {
            System.out.println(v);
        }
    }

    private void bubbleSort(int[] a) {
        for (int i = a.length - 1; i > 0; i--) {
            // 改进，加入这个之后，如果提早完成（数组原来部分有序），就不需要再把剩下不必要的循环也做完了
            boolean finished = true;
            for (int j = 0; j < i; j++) {
                if (a[j] < a[j + 1]) {
                    int b = a[j];
                    a[j] = a[j + 1];
                    a[j + 1] = b;
                    // 改进，加入这个之后，如果提早完成（数组原来部分有序），就不需要再把剩下不必要的循环也做完了
                    finished = false;
                }
            }
            if (finished) {
                break;
            }
        }
    }

}