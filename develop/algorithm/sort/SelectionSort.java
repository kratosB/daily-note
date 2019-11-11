class SelectionSort {

    public static void main(String[] args) {
        int[] a = new int[] {
                5, 1, 6, 9, 3, 2, 6, 8, 3, 5
        };
        SelectionSort selectionSort = new SelectionSort();
        selectionSort.selectionSort(a);
        for (int v : a) {
            System.out.print(v + " ");
        }
    }

    private void selectionSort(int[] a) {
        for (int i = 0; i < a.length; i++) {
            int index = i;
            for (int j = i + 1; j < a.length; j++) {
                if (a[index] > a[j]) {
                    index = j;
                }
            }
            int value = a[i];
            a[i] = a[index];
            a[index] = value;
        }
    }

}
