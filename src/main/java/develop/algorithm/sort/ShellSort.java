package develop.algorithm.sort;

public class ShellSort {

    public static void main(String[] args) {
        int[] a = new int[]{
                5, 1, 6, 9, 3, 2, 6, 8, 3, 5
        };
        ShellSort shellSort = new ShellSort();
        shellSort.shellSort(a);
        for (int v : a) {
            System.out.print(v + " ");
        }
    }

    private void shellSort(int[] a) {
        for (int shell = a.length / 2; shell >= 1; shell = shell / 2) {
            for (int i = shell; i < a.length; i++) {
                int index = i;
                for (int j = i - shell; j >= 0; j = j - shell) {
                    if (a[i] < a[j]) {
                        index = j;
                    } else {
                        break;
                    }
                }
                int value = a[i];
                for (int j = i - shell; j >= index; j = j - shell) {
                    a[j + shell] = a[j];
                }
                a[index] = value;
            }
            for (int i = 0; i < a.length; i++) {
                System.out.print(a[i] + " ");
            }
            System.out.println();
            if (shell == 1) {
                break;
            }
        }
    }

}