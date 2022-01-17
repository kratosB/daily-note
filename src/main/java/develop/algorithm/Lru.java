package develop.algorithm;

import java.util.HashMap;
import java.util.Map;

/**
 * Created on 2022/1/12. [昨天面试被问到的
 * 缓存淘汰算法FIFO、LRU、LFU及Java实现](https://mp.weixin.qq.com/s/x1-5Ib1lgTvY6YcFfdRg6A)
 * 
 * @author zhiqiang bao
 */
public class Lru<K, V> {

    class LruNode {

        K key;

        V value;

        LruNode pre;

        LruNode next;

        public LruNode(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }

    LruNode head;

    LruNode tail;

    Map<K, LruNode> nodeMap;

    int capacity;

    public Lru(int capacity) {
        this.capacity = capacity;
        nodeMap = new HashMap<>(capacity);
    }

    public V get(K key) {
        LruNode lruNode = nodeMap.get(key);
        if (lruNode == null) {
            return null;
        }
        moveToTail(lruNode);
        return lruNode.value;
    }

    public void set(K key, V value) {
        LruNode lruNode = nodeMap.get(key);
        if (lruNode == null) {
            lruNode = new LruNode(key, value);
            if (nodeMap.size() == capacity) {
                removeHead();
            }
            addToTail(lruNode);
        } else {
            lruNode.value = value;
            moveToTail(lruNode);
        }
    }

    public void moveToTail(LruNode node) {
        if (node == tail) {
            return;
        }
        if (node == head) {
            head = head.next;
            head.pre = null;
        } else {
            LruNode pre = node.pre;
            LruNode next = node.next;
            pre.next = next;
            next.pre = pre;
        }
        node.next = null;
        tail.next = node;
        node.pre = tail;
        tail = node;
    }

    public void removeHead() {
        nodeMap.remove(head.key);
        if (head == tail) {
            head = null;
            tail = null;
        } else {
            LruNode node = head;
            head = node.next;
            head.pre = null;
            node.next = null;
        }
    }

    public void addToTail(LruNode node) {
        nodeMap.put(node.key, node);
        if (head == null) {
            head = node;
            tail = node;
        } else {
            tail.next = node;
            node.pre = tail;
            tail = node;
        }
    }

    public static void main(String[] args) {
        Lru<String, String> test = new Lru<>(5);
        test.set("1", "1");
        test.set("2", "1");
        test.set("3", "1");
        test.set("4", "1");
        test.set("5", "1");
        test.print();
        test.set("6", "1");
        test.print();
        test.set("3", "1");
        test.print();
        test.set("2", "1");
        test.print();
        test.set("2", "1");
        test.print();
    }

    public void print() {
        StringBuilder f2t = new StringBuilder();
        StringBuilder t2f = new StringBuilder();
        LruNode pHead = head;
        while (pHead != null) {
            f2t.append(pHead.key).append(",");
            pHead = pHead.next;
        }
        System.out.println(f2t);
        LruNode pTail = tail;
        while (pTail != null) {
            t2f.append(pTail.key).append(",");
            pTail = pTail.pre;
        }
        System.out.println(t2f);
        System.out.println("----------------------");
    }
}
