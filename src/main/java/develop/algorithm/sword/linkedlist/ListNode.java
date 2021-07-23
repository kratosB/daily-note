package develop.algorithm.sword.linkedlist;

/**
 * Created on 2021/7/20.
 *
 * @author zhiqiang bao
 */
public class ListNode {

    public int val;

    public ListNode next = null;

    public ListNode(int val) {
        this.val = val;
    }

    public void setNext(ListNode next1) {
        next = next1;
    }
}