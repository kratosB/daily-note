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
            a[k] = b;
        }
    }

    /**
     * 我按照自己的逻辑写的，对比教程之后发现几个问题
     * 1. 我的方法，第一个获取index的循环的时候，缺少break，
     * 当我的a[i]>=a[j]的时候，没有break，还会继续循环对比，
     * 实际上后面是有序的，不可能再发生a[i]<a[j]了，所以要加上else-break;
     * 这么一来就跟教程是一样的了，就多了一个赋值的操作。
     * 2. 我的index，就等于教程里的j+1，优点是便于理解，缺点是多用空间
     */
    private void insertSort1(int[] a) {
        for (int i = 1; i < a.length; i++) {
            int index = i;
            for (int j = i - 1; j >= 0; j--) {
                if (a[i] < a[j]) {
                    index = j;
                } else {
                    break;
                }
            }
            int value = a[i];
            for (int j = i - 1; j >= index; j--) {
                a[j + 1] = a[j];
            }
            a[index] = value;
        }
    }

    /**
     * 自己又写了一遍，跟自己之前写的逻辑基本一致，
     * 跟教程里比还是差了一点
     * @param a
     */
    private void insertSort2(int[] a) {
        for (int i = 1; i < a.length; i++) {
            int temp = a[i];
            int tempIndex = i;
            for (int j = i - 1; j >= 0; j--) {
                if (temp < a[j]) {
                    a[j + 1] = a[j];
                    tempIndex = j;
                } else {
                    break;
                }
            }
            a[tempIndex] = temp;
        }
    }
}
