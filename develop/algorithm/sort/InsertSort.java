/**
 * Created on 2019/2/14.
 *
 * @author zhiqiang bao
 */
public class InsertSort {

    public static void main(String[] args) {
        int[] a = new int[]{5, 1, 6, 9, 3, 2, 6, 8, 3, 5};
        InsertSort insertSort = new InsertSort();
        insertSort.insertSort(a);
        for (int v : a) {
            System.out.println(v);
        }
    }

    private void insertSort(int[] a) {
        int j, k;
        for (int i = 1; i < a.length; i++) {
            for (j = i - 1; j >= 0; j--) {
                if (a[j] <= a[i]) {
                    break;
                }
            }
            int b = a[i];
            for (k = i; k > j + 1; k--) {
                a[k] = a[k - 1];
            }
            a[k ] = b;
        }
    }
}
