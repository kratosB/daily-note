package develop.algorithm.sword.linkedlist;

import org.springframework.util.Assert;

/**
 * Created on 2021/7/20.
 * <p>
 * 反转链表
 * <p>
 * https://blog.nowcoder.net/n/f1550b6a02fc40e6b4ab41a7734f68ea
 *
 * @author zhiqiang bao
 */
public class JZ15 {

    public static void main(String[] args) {
        ListNode listNode5 = new ListNode(5);
        ListNode listNode4 = new ListNode(4);
        ListNode listNode3 = new ListNode(3);
        ListNode listNode2 = new ListNode(2);
        ListNode listNode1 = new ListNode(1);
        listNode1.setNext(listNode2);
        listNode2.setNext(listNode3);
        listNode3.setNext(listNode4);
        listNode4.setNext(listNode5);
        JZ15 solution = new JZ15();
        ListNode node = solution.reverseList1(listNode1);
        ListNode node2 = solution.reverseList2(node);
        // ListNode node3 = solution.reverseList3(node2);
        Assert.isTrue(node.val == listNode5.val, "faaaaaaaaaaaaaaaaaaaalse");
    }

    public ListNode reverseList1(ListNode head) {
        if (head != null) {
            ListNode node = reverseList1(head.next);
            if (node != null) {
                ListNode next = node;
                while (next.next != null) {
                    next = next.next;
                }
                head.next = null;
                next.next = head;
                return node;
            } else {
                return head;
            }
        } else {
            return null;
        }
    }

    public ListNode reverseList3(ListNode head) {
        if (head != null) {
            ListNode node = reverseList3(head.next);
            if (node != null) {
                head.next.next = head;
                head.next = null;
                return node;
            } else {
                return head;
            }
        } else {
            return null;
        }
    }

    public ListNode reverseList2(ListNode head) {
        if (head == null || head.next == null) {
            return head;
        }
        ListNode reverse = reverseList2(head.next);
        head.next.next = head;
        head.next = null;
        return reverse;
    }

}
