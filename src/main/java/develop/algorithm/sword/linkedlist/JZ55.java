package develop.algorithm.sword.linkedlist;

import java.util.HashSet;

/**
 * Created on 2021/7/23.
 * <p>
 * 链表中环的入口结点
 *
 * @author zhiqiang bao
 */
public class JZ55 {

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
        listNode5.setNext(listNode2);

        JZ55 test = new JZ55();
        ListNode node1 = test.entryNodeOfLoop1(listNode1);
        ListNode node2 = test.entryNodeOfLoop2(listNode1);
    }

    /**
     * 自己写的hashSet
     * 
     * @param pHead
     *            pHead
     * @return return
     */
    public ListNode entryNodeOfLoop1(ListNode pHead) {
        HashSet<ListNode> listNodes = new HashSet<>();
        while (pHead != null) {
            if (listNodes.contains(pHead)) {
                return pHead;
            }
            listNodes.add(pHead);
            pHead = pHead.next;
        }
        return null;
    }

    /**
     * 看了网上的双指针（快慢指针）思路，自己写的（基本一样）。
     * <p>
     * 先是一快一慢找相同，总能找到的。这时候慢指针走了x，快指针走了2x。
     * <p>
     * 然后慢的那个点继续走，快的那个点从头开始走，再走x就能碰到了，这时候慢指针走了2x。
     *
     * @param pHead
     *            pHead
     * @return return
     */
    public ListNode entryNodeOfLoop2(ListNode pHead) {
        ListNode one = pHead;
        ListNode two = pHead;
        while (one != null && one.next != null) {
            one = one.next;
            two = two.next.next;
            if (one == two) {
                break;
            }
        }
        if (one == null || one.next == null) {
            return null;
        } else {
            two = pHead;
            while (one != two) {
                one = one.next;
                two = two.next;
            }
            return one;
        }
    }

}
