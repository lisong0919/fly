package com.wealth.fly.core;

import java.math.BigDecimal;


/**
 * MA计算器，用来计算十日均线，三十日均线等
 */
public class MACalculator {

    private volatile Node<BigDecimal> first;
    private volatile Node<BigDecimal> last;
    private volatile BigDecimal total = new BigDecimal("0");
    private volatile BigDecimal average = new BigDecimal("0");
    private volatile BigDecimal count;

    private String name;
    private int capacity;
    private int scale;

    public MACalculator(String name, int capacity) {
        this(name, capacity, 2);//默认保留两位小数
    }

    public MACalculator(String name, int capacity, int scale) {
        this.name = name;
        this.capacity = capacity;
        this.scale = scale;
    }

    /**
     * push一个数据进来,如果数量已满后，继续push，会挤掉头部数据
     *
     * @param value
     * @return 返回push后的MA数据，如果数量没达到capacity返回null
     */
    public synchronized BigDecimal push(Object tag, BigDecimal value) {
        Node<BigDecimal> newNode = new Node<>(tag, value);

        //第一次push进行初始化
        if (count == null || count.intValue() == 0) {
            count = new BigDecimal(0);
            first = newNode;
            last = newNode;
        }
        //已满，挤掉头部
        if (count.intValue() == capacity) {
            total = total.subtract(first.value);
            count = count.subtract(new BigDecimal(1));
            first.next.prev = null;
            first = first.next;
        }

        //新节点放到最后一个
        count = count.add(new BigDecimal(1));
        total = total.add(newNode.value);
        last.next = newNode;
        newNode.prev = last;
        last = newNode;

        if (count.intValue() == capacity) {
//            printAllElement();
            average = total.divide(count, scale, BigDecimal.ROUND_HALF_UP);
            return average;
        } else {
            return null;
        }
    }

    private void printAllElement() {
        Node node = first;
        while (node != null) {
            System.out.println(node.getTag() + ":" + node.getValue());
            node = node.next;
        }
    }

    /**
     * 替换最后一个数据，并把替换后的MA数据返回
     *
     * @param tag
     * @param value
     * @return 返回MA数据，如果没有达到capacity，返回null
     */
    public synchronized BigDecimal replaceLast(String tag, BigDecimal value) {

        if (count.intValue() != capacity) {
            return null;
        }

        Node<BigDecimal> newNode = new Node(tag, value);
        total = total.subtract(last.value);
        total = total.add(newNode.value);

        last.prev.next = newNode;
        newNode.prev = last.prev;
        last = newNode;

        average = total.divide(count, scale, BigDecimal.ROUND_HALF_UP);
        return average;
    }

    public BigDecimal getAverage() {
        return average;
    }

    public Node<BigDecimal> getFirst() {
        return first;
    }

    public Node<BigDecimal> getLast() {
        return last;
    }

    public BigDecimal getCount() {
        return count;
    }

    public String getName() {
        return name;
    }

    public int getCapacity() {
        return capacity;
    }

    public static class Node<T> {
        private Node next;
        private Node prev;
        private Object tag;
        private T value;

        public Node(Object tag, T value) {
            this.tag = tag;
            this.value = value;
        }

        public Node getNext() {
            return next;
        }

        public Node getPrev() {
            return prev;
        }

        public Object getTag() {
            return tag;
        }

        public T getValue() {
            return value;
        }

        @Override
        public String toString() {
            return "Node{" +
                    "next=" + next +
                    ", prev=" + prev +
                    ", tag='" + tag + '\'' +
                    ", value=" + value +
                    '}';
        }
    }

}
