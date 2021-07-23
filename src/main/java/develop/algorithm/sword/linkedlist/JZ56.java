package develop.algorithm.sword.linkedlist;

import java.util.HashSet;

/**
 * Created on 2021/7/23.
 * <p>
 * 删除链表中重复的结点
 *
 * @author zhiqiang bao
 */
public class JZ56 {

    public static void main(String[] args) throws Exception {
        ListNode listNode55 = new ListNode(5);
        ListNode listNode5 = new ListNode(5);
        ListNode listNode44 = new ListNode(4);
        ListNode listNode4 = new ListNode(3);
        ListNode listNode33 = new ListNode(3);
        ListNode listNode3 = new ListNode(2);
        ListNode listNode2 = new ListNode(1);
        ListNode listNode1 = new ListNode(1);
        listNode1.setNext(listNode2);
        listNode2.setNext(listNode3);
        listNode3.setNext(listNode33);
        listNode33.setNext(listNode4);
        listNode4.setNext(listNode44);
        listNode44.setNext(listNode5);
        listNode5.setNext(listNode55);

        JZ56 test = new JZ56();
        ListNode node1 = test.deleteDuplication(listNode1);
    }

    /**
     * 自己写的用Set的办法，写的时候有两点没考虑到。
     * <p>
     * 1. 头节点也可能会重复的，一开始没考虑到，所以还是得引入一个虚拟节点first
     * <p>
     * 2. 整个算法结束之后，最后一个current的next要设置成null，不然返回结果中会带上后面的节点
     * <p>
     * 3. 这个题目中的是排序后的链表，所以相同的值都是连续的，不会是间隔的，我写的时候考虑的是会间隔。
     * 
     * @param pHead
     *            pHead
     * @return return
     */
    public ListNode deleteDuplication(ListNode pHead) {
        if (pHead == null) {
            return null;
        }
        HashSet<Integer> set = new HashSet<>();
        HashSet<Integer> dup = new HashSet<>();
        ListNode pHead1 = pHead;
        while (pHead1 != null) {
            if (set.contains(pHead1.val)) {
                dup.add(pHead1.val);
            } else {
                set.add(pHead1.val);
            }
            pHead1 = pHead1.next;
        }
        pHead1 = pHead;
        ListNode first = new ListNode(0);
        ListNode current = first;
        while (pHead1 != null) {
            if (!dup.contains(pHead1.val)) {
                current.next = pHead1;
                current = current.next;
            }
            pHead1 = pHead1.next;
        }
        current.next = null;
        return first.next;
    }

}
