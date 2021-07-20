package develop.algorithm.sword;

/**
 * Created on 2021/7/20.
 *
 * @author zhiqiang bao
 */
public class ListNode {

    int val;

    ListNode next = null;

    ListNode(int val) {
        this.val = val;
    }

    void setNext(ListNode next1) {
        next = next1;
    }
}