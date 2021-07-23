package develop.algorithm.sword.linkedlist;

/**
 * Created on 2021/7/23.
 * <p>
 * 两个链表的第一个公共结点
 *
 * @author zhiqiang bao
 */
public class JZ36 {

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
        listNode05.setNext(listNode5);
        JZ36 test = new JZ36();
        ListNode node1 = test.findFirstCommonNode1(null, null);
        ListNode node2 = test.findFirstCommonNode2(null, null);
    }

    /**
     * 自己写的跳过差值，对比差值之后部分的方法。
     * 
     * @param pHead1
     *            pHead1
     * @param pHead2
     *            pHead2
     * @return return
     */
    public ListNode findFirstCommonNode1(ListNode pHead1, ListNode pHead2) {
        ListNode pHead11 = pHead1;
        ListNode pHead21 = pHead2;
        int count1 = 0, count2 = 0;
        while (pHead11 != null) {
            pHead11 = pHead11.next;
            count1++;
        }
        while (pHead21 != null) {
            pHead21 = pHead21.next;
            count2++;
        }
        if (count1 > count2) {
            int i = count1 - count2;
            for (int j = 0; j < i; j++) {
                pHead1 = pHead1.next;
            }
        } else {
            int i = count2 - count1;
            for (int j = 0; j < i; j++) {
                pHead2 = pHead2.next;
            }
        }
        while (pHead1 != null) {
            if (pHead1 == pHead2) {
                return pHead1;
            } else {
                pHead1 = pHead1.next;
                pHead2 = pHead2.next;
            }
        }
        return null;
    }

    /**
     * 网上的例子，把两个链表拼到一起，一起走，公共节点肯定会在相同的index上（不论链表长短一样不一样）。感觉还可以。
     *
     * @param pHead1
     *            pHead1
     * @param pHead2
     *            pHead2
     * @return return
     */
    public ListNode findFirstCommonNode2(ListNode pHead1, ListNode pHead2) {
        ListNode p1 = pHead1, p2 = pHead2;
        if (p1 == null || p2 == null) {
            return null;
        }
        while (p1 != p2) {
            p1 = p1 == null ? pHead2 : p1.next;
            p2 = p2 == null ? pHead1 : p2.next;
        }
        return p1;
    }

}
