package develop.algorithm.sword.linkedlist;

import java.util.ArrayList;
import java.util.Stack;

/**
 * Created on 2021/7/20.
 * <p>
 * 从尾到头打印链表
 * <p>
 * https://blog.nowcoder.net/n/b30ce013bd294a1681711e8a6f0a4231
 * 
 * @author zhiqiang bao
 */
public class JZ3 {

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
        JZ3 solution = new JZ3();
        ArrayList<Integer> integers1 = solution.printListFromTailToHead1(listNode1);
        for (Integer integer : integers1) {
            System.out.print(integer + " ");
        }
        System.out.println();
        ArrayList<Integer> integers2 = solution.printListFromTailToHead2(listNode1);
        for (Integer integer : integers2) {
            System.out.print(integer + " ");
        }
        System.out.println();
        ArrayList<Integer> integers3 = solution.printListFromTailToHead3(listNode1);
        for (Integer integer : integers3) {
            System.out.print(integer + " ");
        }
        System.out.println();
        ArrayList<Integer> integers4 = solution.printListFromTailToHead4(listNode1);
        for (Integer integer : integers4) {
            System.out.print(integer + " ");
        }
    }

    /**
     * 我自己的做法，一开始我判断用node.next != null，后来看了答案之后，发现node != null更好。
     * 
     * @param listNode
     *            node
     * @return list
     */
    public ArrayList<Integer> printListFromTailToHead1(ListNode listNode) {
        ArrayList<Integer> list = new ArrayList<>();
        getNext(listNode, list);
        return list;
    }

    private void getNext(ListNode node, ArrayList<Integer> list) {
        if (node != null) {
            getNext(node.next, list);
            list.add(node.val);
        }
    }

    ArrayList<Integer> list2 = new ArrayList<>();

    /**
     * 网上的递归做法，原里跟我的一样，这个比较精简
     * 
     * @param listNode
     *            node
     * @return list
     */
    public ArrayList<Integer> printListFromTailToHead2(ListNode listNode) {
        if (listNode != null) {
            printListFromTailToHead2(listNode.next);
            list2.add(listNode.val);
        }
        return list2;
    }

    /**
     * <p>
     * 网上的非递归做法，相当简单，用了list的add（index，value的特性），很聪明。
     * <p>
     * 但是，这个add(index,value)方法，内部有一个arraycopy的操作，复杂度比较高。
     * <p>
     * 一般来说，可以用stack。
     * 
     * @param listNode
     *            node
     * @return list
     */
    public ArrayList<Integer> printListFromTailToHead3(ListNode listNode) {
        ArrayList<Integer> list = new ArrayList<>();
        ListNode tmp = listNode;
        while (tmp != null) {
            list.add(0, tmp.val);
            tmp = tmp.next;
        }
        return list;
    }

    public ArrayList<Integer> printListFromTailToHead4(ListNode listNode) {
        Stack<Integer> stack = new Stack<Integer>();
        while (listNode!=null) {
            stack.push(listNode.val);
            listNode = listNode.next;
        }
        ArrayList<Integer> list = new ArrayList<>();
        while (!stack.empty()) {
            list.add(stack.pop());
        }
        return list;
    }

}
