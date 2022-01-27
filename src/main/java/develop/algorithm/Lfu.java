package develop.algorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created on 2022/1/12. [昨天面试被问到的
 * 缓存淘汰算法FIFO、LRU、LFU及Java实现](https://mp.weixin.qq.com/s/x1-5Ib1lgTvY6YcFfdRg6A)
 * 
 * @author zhiqiang bao
 */
public class Lfu<K, V> {

    class LfuNode {

        K key;

        V value;

        int count;

        public LfuNode(K key, V value) {
            this.key = key;
            this.value = value;
            count = 1;
        }
    }

    Map<K, LfuNode> nodeMap;

    Map<Integer, List<LfuNode>> levelMap;

    int minCount;

    int capacity;

    public Lfu(int capacity) {
        nodeMap = new HashMap<>();
        levelMap = new HashMap<>();
        minCount = 0;
        this.capacity = capacity;
    }

    public V get(K key) {
        LfuNode node = nodeMap.get(key);
        if (node == null) {
            return null;
        } else {
            moveToNextLevel(node);
            return node.value;
        }
    }

    public void set(K key, V value) {
        LfuNode node = nodeMap.get(key);
        if (node == null) {
            node = new LfuNode(key, value);
            if (capacity == nodeMap.size()) {
                removeMinNode();
            }
            addNewNode(node);
        } else {
            node.value = value;
            moveToNextLevel(node);
        }
    }

    private void moveToNextLevel(LfuNode node) {
        int oldCount = node.count;
        List<LfuNode> oldLevelList = levelMap.get(oldCount);
        oldLevelList.remove(node);
        if (minCount == oldCount && oldLevelList.size() == 0) {
            minCount++;
        }
        int newCount = ++node.count;
        List<LfuNode> newLevelList = levelMap.get(newCount);
        if (newLevelList == null) {
            newLevelList = new ArrayList<>();
            newLevelList.add(node);
            levelMap.put(newCount, newLevelList);
        } else {
            newLevelList.add(node);
        }
    }

    private void removeMinNode() {
        List<LfuNode> minLevelList = levelMap.get(minCount);
        LfuNode remove = minLevelList.remove(0);
        nodeMap.remove(remove.key);
    }

    private void addNewNode(LfuNode node) {
        nodeMap.put(node.key, node);
        minCount = 1;
        List<LfuNode> levelOneList = levelMap.get(minCount);
        if (levelOneList == null) {
            levelOneList = new ArrayList<>();
            levelOneList.add(node);
            levelMap.put(minCount, levelOneList);
        } else {
            levelOneList.add(node);
        }
    }

    void print() {
        int count = nodeMap.size();
        int length = 0;
        while (count > 0) {
            List<LfuNode> nodes = levelMap.get(length);
            if (nodes != null && nodes.size() > 0) {
                for (LfuNode node : nodes) {
                    System.out.print(node.key + "|" + node.value + "|" + node.count + " , ");
                    count--;
                }
                System.out.println();
            }
            length++;
        }
    }

    public static void main(String[] args) {
        Lfu<String, String> cache = new Lfu<>(3);
        cache.set("keyA", "valueA");
        System.out.println("put keyA");
        cache.print();
        System.out.println("=========================");

        cache.set("keyB", "valueB");
        System.out.println("put keyB");
        cache.print();
        System.out.println("=========================");

        cache.set("keyC", "valueC");
        System.out.println("put keyC");
        cache.print();
        System.out.println("=========================");

        cache.get("keyA");
        System.out.println("get keyA");
        cache.print();
        System.out.println("=========================");

        cache.set("keyD", "valueD");
        System.out.println("put keyD");
        cache.print();
    }

}
