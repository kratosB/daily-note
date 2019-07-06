class QuickSort {

    public static void main(String[] args) {
        int[] a = new int[]{
                5, 1, 6, 9, 3, 2, 6, 8, 3, 5
        };
        QuickSort quickSort = new QuickSort();
        quickSort.quickSort(a, 0, a.length - 1);
        for (int v : a) {
            System.out.print(v + " ");
        }
    }

    /**
     * 我写的，开始差一点，对照例子改了一下，比例子多了一个tempIndex，仔细看了例子，发现确实没必要
     */
    private void quickSort(int[] a, int first, int last) {
        if (first < last) {
            int tempIndex = last;
            int right = last;
            int left = first;
            int temp = a[tempIndex];
            while (left < right) {
                while (left < right && a[left] <= temp) {
                    left++;
                }
                if (left < right) {
                    a[tempIndex] = a[left];
                    tempIndex = left;
                    right--;
                }
                while (left < right && a[right] >= temp) {
                    right--;
                }
                if (left < right) {
                    a[tempIndex] = a[right];
                    tempIndex = right;
                    left++;
                }
            }
            a[tempIndex] = temp;
            quickSort(a, first, left - 1);
            quickSort(a, right + 1, last);
        }
    }

    /**
     * 网上的例子
     */
    private void quickSort2(int[] a, int first, int last) {
        if (first < last) {
            int left = first;
            int right = last;
            int temp = a[last];
            while (left < right) {
                while (left < right && a[left] <= temp) {
                    left++;
                }
                if (left < right) {
                    a[right] = a[left];
                    right--;
                }
                while (left < right && a[right] >= temp) {
                    right--;
                }
                if (left < right) {
                    a[left] = a[right];
                    left++;
                }
            }
            a[left] = temp;
            quickSort(a, first, left - 1);
            quickSort(a, right + 1, last);
        }
    }

}
