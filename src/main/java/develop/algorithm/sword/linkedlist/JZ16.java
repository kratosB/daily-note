package develop.algorithm.sword.linkedlist;

/**
 * Created on 2021/7/23.
 * <p>
 * 合并两个排序的链表
 *
 * @author zhiqiang bao
 */
public class JZ16 {

    public static void main(String[] args) throws Exception {
        ListNode listNode5 = new ListNode(5);
        ListNode listNode4 = new ListNode(4);
        ListNode listNode3 = new ListNode(3);
        ListNode listNode2 = new ListNode(2);
        ListNode listNode1 = new ListNode(1);
        listNode1.setNext(listNode2);
        listNode2.setNext(listNode3);
        listNode3.setNext(listNode4);
        listNode4.setNext(listNode5);

        ListNode listNode05 = new ListNode(5);
        ListNode listNode04 = new ListNode(4);
        ListNode listNode03 = new ListNode(3);
        ListNode listNode02 = new ListNode(2);
        ListNode listNode01 = new ListNode(1);
        listNode01.setNext(listNode02);
        listNode02.setNext(listNode03);
        listNode03.setNext(listNode04);
        listNode04.setNext(listNode05);

        JZ16 test = new JZ16();
        ListNode merge = test.merge(listNode1, listNode01);
    }

    /**
     * 我自己写的递归方法，跟人家的方法几乎一样，多了第一个if
     * 
     * @param list1
     *            list1
     * @param list2
     *            list2
     * @return ListNode
     */
    public ListNode merge(ListNode list1, ListNode list2) {
        // 这个if可以删掉，没有意义
        if (list1 == null && list2 == null) {
            return null;
        }
        if (list1 == null) {
            return list2;
        }
        if (list2 == null) {
            return list1;
        }
        if (list1.val > list2.val) {
            list2.next = merge(list1, list2.next);
            return list2;
        } else {
            list1.next = merge(list1.next, list2);
            return list1;
        }
    }

    /**
     * 我自己写的非递归方法，跟网上解法基本一样，网上的解法也是用这个first的虚拟节点
     * 
     * @param list1
     *            list1
     * @param list2
     *            list2
     * @return ListNode
     */
    public ListNode merge1(ListNode list1, ListNode list2) {
        ListNode first = new ListNode(0);
        ListNode current = first;
        while (list1 != null && list2 != null) {
            if (list1.val > list2.val) {
                current.next = list2;
                list2 = list2.next;
                current = current.next;
            } else {
                current.next = list1;
                list1 = list1.next;
                current = current.next;
            }
        }
        if (list1 == null) {
            current.next = list2;
        }
        if (list2 == null) {
            current.next = list1;
        }
        return first.next;
    }

}
