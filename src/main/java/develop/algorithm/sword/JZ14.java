package develop.algorithm.sword;

import java.util.ArrayList;

/**
 * Created on 2021/7/20.
 * <p>
 * 链表中倒数最后k个结点
 * <p>
 * https://blog.nowcoder.net/n/6daf9fd217824042991bd9364e765ed1
 * 
 * @author zhiqiang bao
 */
public class JZ14 {

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
        JZ14 solution = new JZ14();
        ListNode node1 = solution.findKthToTail1(listNode1, 1);
        while (node1 != null) {
            System.out.println(node1.val);
            node1 = node1.next;
        }
        ListNode node2 = solution.findKthToTail2(listNode1, 2);
        while (node2 != null) {
            System.out.println(node2.val);
            node2 = node2.next;
        }
        ListNode node3 = solution.findKthToTail3(listNode1, 3);
        while (node3 != null) {
            System.out.println(node3.val);
            node3 = node3.next;
        }
        ListNode node4 = solution.findKthToTail4(listNode1, 4);
        while (node4 != null) {
            System.out.println(node4.val);
            node4 = node4.next;
        }
    }

    /**
     * 我自己的做法，网上有一个用stack的做法（出stack之后还要重新排成链表）。
     * <p>
     * 我感觉还是我这个简单一点（我这边链表还是原本的链表，只是记录了一下index）
     * 
     * @param pHead
     *            head
     * @param k
     *            index
     * @return node
     */
    public ListNode findKthToTail1(ListNode pHead, int k) {
        ArrayList<ListNode> list = new ArrayList<>();
        while (pHead != null) {
            list.add(pHead);
            pHead = pHead.next;
        }
        int index = list.size() - k;
        if (index >= 0 && k > 0) {
            return list.get(index);
        } else {
            return pHead;
        }
    }

    /**
     * 双指针的做法，很精妙。
     * <p>
     * 先用第一个指针走k格。
     * <p>
     * 然后第二个指针和第一个指针保持k个间隔，一起往后走。
     * <p>
     * 第一个指针到终点的时候，第二个指针刚好在倒数第k个。
     * 
     * @param pHead
     *            head
     * @param k
     *            index
     * @return node
     */
    public ListNode findKthToTail2(ListNode pHead, int k) {
        if (pHead == null) {
            return null;
        }
        ListNode first = pHead;
        ListNode second = pHead;
        // 第一个指针先走k步
        while (k-- > 0) {
            if (first == null) {
                return null;
            }
            first = first.next;
        }
        // 然后两个指针在同时前进
        while (first != null) {
            first = first.next;
            second = second.next;
        }
        return second;
    }

    int size3;

    /**
     * 递归做法，大概就是从后往前记录index，当index==k的时候返回，然后一路递归返回。
     *
     * @param pHead
     *            head
     * @param k
     *            index
     * @return node
     */
    public ListNode findKthToTail3(ListNode pHead, int k) {
        // 边界条件判断
        if (pHead == null) {
            return null;
        }
        ListNode node = findKthToTail3(pHead.next, k);
        ++size3;
        // 从后面数结点数小于k，返回空
        if (size3 < k) {
            return null;
        } else if (size3 == k) {
            // 从后面数访问结点等于k，直接返回传递的结点k即可
            return pHead;
        } else {
            // 从后面数访问的结点大于k，说明我们已经找到了，
            // 直接返回node即可
            return node;
        }
    }

    int size4;

    /**
     * 递归做法的精简版。
     *
     * @param pHead
     *            head
     * @param k
     *            index
     * @return node
     */
    public ListNode findKthToTail4(ListNode pHead, int k) {
        if (pHead == null) {
            return null;
        }
        ListNode node = findKthToTail4(pHead.next, k);
        if (++size4 == k) {
            return pHead;
        }
        return node;
    }
}
