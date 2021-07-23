package develop.algorithm.sword.linkedlist;

import java.util.HashMap;

/**
 * Created on 2021/7/23.
 * <p>
 * 复杂链表的复制，这个题个人觉得没啥意思，但是确实有点恶心
 * 
 * @author zhiqiang bao
 */
public class JZ25 {

    public static void main(String[] args) throws Exception {
        RandomListNode listNode5 = new RandomListNode(5);
        RandomListNode listNode4 = new RandomListNode(4);
        RandomListNode listNode3 = new RandomListNode(3);
        RandomListNode listNode2 = new RandomListNode(2);
        RandomListNode listNode1 = new RandomListNode(1);
        listNode1.next = listNode2;
        listNode2.next = listNode3;
        listNode3.next = listNode4;
        listNode4.next = listNode5;

        listNode1.random = listNode3;
        listNode2.random = listNode5;
        listNode3.random = null;
        listNode4.random = listNode2;
        listNode5.random = null;

        JZ25 test = new JZ25();
        RandomListNode select1 = test.clone(listNode1);
        RandomListNode select2 = test.clone1(listNode1);
        System.out.println();
    }

    /**
     * 自己写的指针的方法，很繁琐，尤其是random部分，还要重复比对。网上也没看到非常亮眼的。
     * @param pHead
     * @return
     */
    public RandomListNode clone(RandomListNode pHead) {
        if (pHead == null) {
            return null;
        }
        RandomListNode first = new RandomListNode(0);
        RandomListNode pHead1 = pHead;
        RandomListNode current = new RandomListNode(pHead1.label);
        first.next = current;
        while (pHead1.next != null) {
            RandomListNode temp = new RandomListNode(pHead1.next.label);
            current.next = temp;
            current = temp;
            pHead1 = pHead1.next;
        }
        pHead1 = pHead;
        current = first.next;
        while (current != null) {
            RandomListNode random = pHead1.random;
            if (random == null) {
                current.random = null;
            } else {
                RandomListNode currentRandom = first.next;
                RandomListNode pHead2 = pHead;
                while (pHead2 != null) {
                    if (pHead2 == random) {
                        current.random = currentRandom;
                        break;
                    } else {
                        pHead2 = pHead2.next;
                        currentRandom = currentRandom.next;
                    }
                }
            }
            current = current.next;
            pHead1 = pHead1.next;
        }
        return first.next;
    }

    /**
     * 借助map的方法，网上差不多也是这样的，只能说还行吧。
     * @param pHead
     * @return
     */
    public RandomListNode clone1(RandomListNode pHead) {
        if (pHead == null) {
            return null;
        }
        HashMap<RandomListNode, RandomListNode> map = new HashMap<>();
        map.put(null, null);
        RandomListNode pHead1 = pHead;
        while (pHead1 != null) {
            RandomListNode randomListNode = new RandomListNode(pHead1.label);
            map.put(pHead1, randomListNode);
            pHead1 = pHead1.next;
        }
        pHead1 = pHead;
        while (pHead1 != null) {
            RandomListNode next = pHead1.next;
            RandomListNode random = pHead1.random;
            RandomListNode randomListNode = map.get(pHead1);
            randomListNode.next = map.get(next);
            randomListNode.random = map.get(random);
            pHead1 = pHead1.next;
        }
        return map.get(pHead);
    }

}
