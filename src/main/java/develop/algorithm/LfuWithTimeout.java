package develop.algorithm;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class LfuWithTimeout {

    static class Node {

        String key;

        String value;

        long time;

        int count;

        Node pre;

        Node next;

        public Node(String key, String value, long time, int count) {
            this.key = key;
            this.value = value;
            this.time = time;
            this.count = count;
        }
    }

    int capacity;

    long timeoutPeriod;

    Map<String, Node> nodeMap;

    Map<Integer, LinkedList<Node>> countMap;

    int minCount;

    Node head;

    Node tail;

    public LfuWithTimeout(int size, long timeoutPeriod) {
        this.capacity = size;
        this.timeoutPeriod = timeoutPeriod;
        this.minCount = 0;
        nodeMap = new HashMap<>(size);
        countMap = new HashMap<>(size);
    }

    public String get(String key) {
        Node node = nodeMap.get(key);
        if (node == null) {
            return null;
        } else {
            node.count++;
            node.time = System.currentTimeMillis();
            countMapRefresh(node);
            nodesRefresh(node);
            return node.value;
        }
    }

    public void put(String key, String value) {
        Node node = nodeMap.get(key);
        if (node == null) {
            node = new Node(key, value, System.currentTimeMillis(), 1);
            if (nodeMap.size() == capacity) {
                boolean removed = timeoutNodeRemove();
                if (!removed) {
                    minNodeRemove();
                }
            }
            nodeMap.put(key, node);
            countMapAdd(node);
            nodeListAdd(node);
        } else {
            node.value = value;
            node.count++;
            node.time = System.currentTimeMillis();
            countMapRefresh(node);
            nodesRefresh(node);
        }
    }

    public void countMapAdd(Node node) {
        LinkedList<Node> nodeList = countMap.get(1);
        if (nodeList == null) {
            nodeList = new LinkedList<>();
            nodeList.add(node);
            countMap.put(1, nodeList);
        } else {
            nodeList.add(node);
        }
        minCount = 1;
    }

    public void countMapRefresh(Node node) {
        int newCount = node.count;
        int oldCount = newCount - 1;
        LinkedList<Node> oldNodeList = countMap.get(oldCount);
        oldNodeList.remove(node);
        if (oldNodeList.size() == 0) {
            if (minCount == oldCount) {
                minCount++;
            }
        }
        LinkedList<Node> newNodeList = countMap.get(newCount);
        if (newNodeList == null) {
            newNodeList = new LinkedList<>();
            newNodeList.addLast(node);
            countMap.put(newCount, newNodeList);
        } else {
            newNodeList.addLast(node);
        }
    }

    public void nodeListAdd(Node node) {
        if (head == null) {
            head = node;
        } else {
            tail.next = node;
            node.pre = tail;
        }
        tail = node;
    }

    public void nodesRefresh(Node node) {
        if (tail == node) {
            return;
        }
        if (head == node) {
            head = node.next;
            head.pre = null;
        } else {
            Node next = node.next;
            Node pre = node.pre;
            pre.next = next;
            next.pre = pre;
        }
        tail.next = node;
        node.pre = tail;
        node.next = null;
        tail = node;
    }

    private boolean timeoutNodeRemove() {
        Node headNode = head;
        long headTime = headNode.time;
        System.out.println(System.currentTimeMillis());
        System.out.println(headTime);
        if (System.currentTimeMillis() - headTime > timeoutPeriod) {
            if (head == tail) {
                head = null;
                tail = null;
            } else {
                head = headNode.next;
                head.pre = null;
                headNode.next = null;
            }
            int count = headNode.count;
            LinkedList<Node> nodeList = countMap.get(count);
            nodeList.remove(headNode);
            nodeMap.remove(headNode.key);
            return true;
        } else {
            return false;
        }
    }

    public void minNodeRemove() {
        LinkedList<Node> nodeList = countMap.get(minCount);
        Node first = nodeList.removeFirst();
        nodeMap.remove(first.key);
        if (head == first) {
            head = first.next;
            first.next = null;
            if (head != null) {
                head.pre = null;
            } else {
                tail = null;
            }
        } else if (tail == first) {
            tail = first.pre;
            tail.next = null;
            first.pre = null;
        } else {
            Node next = first.next;
            Node pre = first.pre;
            pre.next = next;
            next.pre = pre;
            first.pre = null;
            first.next = null;
        }
    }

    public void print() {
        int count = nodeMap.size();
        int length = 0;
        while (count > 0) {
            LinkedList<Node> nodes = countMap.get(length);
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
        LfuWithTimeout lfuTest = new LfuWithTimeout(3,2000);
        lfuTest.put("1","1");
        lfuTest.put("2","2");
        lfuTest.put("3","3");
        lfuTest.print();
        System.out.println("3-------");
        lfuTest.put("4","4");
        lfuTest.print();
        System.out.println("4-------");
        lfuTest.put("3","3");
        lfuTest.put("5","5");
        lfuTest.print();
        System.out.println("35-------");
        lfuTest.put("6","6");
        lfuTest.print();
        System.out.println("6-------");
        TimeUnit.SECONDS.sleep(3);
        lfuTest.put("7","7");
        lfuTest.print();
        System.out.println("7-------");
    }
}
