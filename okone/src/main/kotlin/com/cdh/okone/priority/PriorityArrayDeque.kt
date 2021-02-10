package com.cdh.okone.priority

import java.util.*

/**
 * 优先级ArrayDeque
 * 从大到小排序
 * Created by chidehang on 2021/1/12
 */
class PriorityArrayDeque<E> : ArrayDeque<E>, Deque<E> {
    private var queue: LinkedList<E>

    constructor() {
        queue = LinkedList()
    }

    constructor(numElements: Int) : super(numElements) {
        queue = LinkedList()
    }

    constructor(c: Collection<E>) : super(c) {
        queue = LinkedList(c)
        for (e in c) {
            sortAdd(e)
        }
    }

    override fun addFirst(e: E) {
        super.addFirst(e)
        sortAdd(e)
    }

    override fun addLast(e: E) {
        super.addLast(e)
        sortAdd(e)
    }

    /**
     * 按序插入
     */
    private fun sortAdd(x: E) {
        val key = x as Comparable<E>
        val i = queue.listIterator(queue.size)
        while (i.hasPrevious()) {
            val e = i.previous()
            if (key <= e) {
                i.next()
                i.add(x)
                return
            }
        }
        queue.addFirst(x)
    }

    override fun pollFirst(): E? {
        queue.pollFirst()
        return super.pollFirst()
    }

    override fun pollLast(): E? {
        queue.pollLast()
        return super.pollLast()
    }

    override fun removeFirstOccurrence(o: Any?): Boolean {
        queue.remove(o)
        return super.removeFirstOccurrence(o)
    }

    override fun removeLastOccurrence(o: Any?): Boolean {
        queue.remove(o)
        return super.removeLastOccurrence(o)
    }

    override fun clear() {
        super.clear()
        queue.clear()
    }

    override fun iterator(): MutableIterator<E> {
        return queue.iterator()
    }
}