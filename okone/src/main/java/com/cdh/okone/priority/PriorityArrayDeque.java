package com.cdh.okone.priority;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * 优先级ArrayDeque
 * 从大到小排序
 * Created by chidehang on 2021/1/12
 */
public class PriorityArrayDeque<E> extends ArrayDeque<E> implements Deque<E> {

    private LinkedList<E> queue;

    public PriorityArrayDeque() {
        queue = new LinkedList<>();
    }

    public PriorityArrayDeque(int numElements) {
        super(numElements);
        queue = new LinkedList<>();
    }

    public PriorityArrayDeque(Collection<? extends E> c) {
        super(c);
        queue = new LinkedList<>(c);
        for (E e : c) {
            sortAdd(e);
        }
    }

    @Override
    public void addFirst(E e) {
        super.addFirst(e);
        sortAdd(e);
    }

    @Override
    public void addLast(E e) {
        super.addLast(e);
        sortAdd(e);
    }

    /**
     * 按序插入
     */
    private void sortAdd(E x) {
        Comparable<? super E> key = (Comparable<? super E>) x;
        ListIterator<E> i = queue.listIterator(queue.size());
        while (i.hasPrevious()) {
            E e = i.previous();
            if (key.compareTo(e) <= 0) {
                i.next();
                i.add(x);
                return;
            }
        }
        queue.addFirst(x);
    }

    @Override
    public E pollFirst() {
        queue.pollFirst();
        return super.pollFirst();
    }

    @Override
    public E pollLast() {
        queue.pollLast();
        return super.pollLast();
    }

    @Override
    public boolean removeFirstOccurrence(Object o) {
        queue.remove(o);
        return super.removeFirstOccurrence(o);
    }

    @Override
    public boolean removeLastOccurrence(Object o) {
        queue.remove(o);
        return super.removeLastOccurrence(o);
    }

    @Override
    public void clear() {
        super.clear();
        queue.clear();
    }

    @Override
    public Iterator<E> iterator() {
        return queue.iterator();
    }
}
