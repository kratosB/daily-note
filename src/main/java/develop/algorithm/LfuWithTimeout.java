package develop.algorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class LfuWithTimeout<K, V> {

    class Node {

        K key;

        V value;

        int count;

        long time;

        Node pre;

        Node next;

        public Node(K key, V value, long time, int count) {
            this.key = key;
            this.value = value;
            this.time = time;
            this.count = count;
        }
    }

    int capacity;

    int minCount;

    Map<K, Node> nodeMap;

    Map<Integer, List<Node>> levelMap;

    Node head;

    Node tail;

    long timeout;

    public LfuWithTimeout(int capacity, long timeout) {
        this.capacity = capacity;
        this.minCount = 0;
        this.nodeMap = new HashMap<>();
        this.levelMap = new HashMap<>();
        this.timeout = timeout;
    }

    public V get(K key) {
        Node node = nodeMap.get(key);
        if (node == null) {
            return null;
        } else {
            node.time = System.currentTimeMillis();
            moveToNextLevel(node);
            moveToTail(node);
            return node.value;
        }
    }

    public void set(K key, V value) {
        Node node = nodeMap.get(key);
        if (node == null) {
            node = new Node(key, value, System.currentTimeMillis(), 1);
            if (nodeMap.size() == capacity) {
                boolean removed = removeTimeoutNode();
                if (!removed) {
                    removeMinNode();
                }
            }
            addNewNode(node);
            addToTail(node);
        } else {
            node.value = value;
            node.time = System.currentTimeMillis();
            moveToNextLevel(node);
            moveToTail(node);
        }
    }

    private void moveToNextLevel(Node node) {
        int count = node.count;
        List<Node> preLevel = levelMap.get(count);
        preLevel.remove(node);
        if (preLevel.size() == 0 && minCount == count) {
            minCount++;
        }
        int newCount = ++node.count;
        List<Node> nextLevel = levelMap.get(newCount);
        if (nextLevel == null) {
            nextLevel = new ArrayList<>();
            nextLevel.add(node);
            levelMap.put(newCount, nextLevel);
        } else {
            nextLevel.add(node);
        }
    }

    private void addNewNode(Node node) {
        nodeMap.put(node.key, node);
        minCount = 1;
        List<Node> oneLevel = levelMap.get(minCount);
        if (oneLevel == null) {
            oneLevel = new ArrayList<>();
            oneLevel.add(node);
            levelMap.put(minCount, oneLevel);
        } else {
            oneLevel.add(node);
        }
    }

    private void addToTail(Node node) {
        if (head == null) {
            head = node;
            tail = node;
        } else {
            tail.next = node;
            node.pre = tail;
            tail = node;
        }
    }

    private boolean removeTimeoutNode() {
        if (System.currentTimeMillis() - head.time > timeout) {
            Node removed = nodeMap.remove(head.key);
            if (head == tail) {
                head = null;
                tail = null;
            } else {
                head = removed.next;
                head.pre = null;
                removed.next = null;
            }
            List<Node> level = levelMap.get(removed.count);
            level.remove(removed);
            return true;
        } else {
            return false;
        }
    }

    private void moveToTail(Node node) {
        if (node == tail) {
            return;
        } else if (node == head) {
            head = head.next;
            head.pre = null;
        } else {
            Node pre = node.pre;
            Node next = node.next;
            pre.next = next;
            next.pre = pre;
        }
        node.next = null;
        tail.next = node;
        node.pre = tail;
        tail = node;
    }

    private void removeMinNode() {
        List<Node> minLevel = levelMap.get(minCount);
        Node removed = minLevel.remove(0);
        nodeMap.remove(removed.key);
        if (removed == head) {
            if (head == tail) {
                head = null;
                tail = null;
            } else {
                head = head.next;
                head.pre = null;
            }
        } else if (removed == tail) {
            tail = tail.pre;
            tail.next = null;
        } else {
            Node pre = removed.pre;
            Node next = removed.next;
            pre.next = next;
            next.pre = pre;
        }
        removed.pre = null;
        removed.next = null;
    }

    public void print() {
        int count = nodeMap.size();
        int length = 0;
        while (count > 0) {
            List<Node> nodes = levelMap.get(length);
            if (nodes != null && nodes.size() > 0) {
                for (Node node : nodes) {
                    System.out.print(node.key + "|" + node.value + "|" + node.count + " , ");
                    count--;
                }
                System.out.println();
            }
            length++;
        }
    }

    public static void main(String[] args) throws Exception {
        LfuWithTimeout<String, String> lfuTest = new LfuWithTimeout<>(3, 2000);
        lfuTest.set("1", "1");
        lfuTest.set("2", "2");
        lfuTest.set("3", "3");
        lfuTest.print();
        System.out.println("3-------");
        lfuTest.set("4", "4");
        lfuTest.print();
        System.out.println("4-------");
        lfuTest.set("3", "3");
        lfuTest.set("5", "5");
        lfuTest.print();
        System.out.println("35-------");
        lfuTest.set("6", "6");
        lfuTest.print();
        System.out.println("6-------");
        TimeUnit.SECONDS.sleep(3);
        lfuTest.set("7", "7");
        lfuTest.print();
        System.out.println("7-------");
    }
}
