/*
 * ObservableBlockingQueue.java Copyright (C) 2022 Daniel H. Huson
 *
 * (Some files contain contributions from other authors, who are then mentioned separately.)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package tegula.util;

import javafx.collections.ObservableListBase;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * an observable queue
 * Source: https://stackoverflow.com/questions/28449809/why-are-there-no-observablequeues-in-javafx
 *
 * @param <E>
 */
public class ObservableBlockingQueue<E> extends ObservableListBase<E> implements BlockingQueue<E> {
    private final BlockingQueue<E> queue;

    /**
     * Creates an ObservableBlockingQueue backed by the supplied BlockingQueue.
     * Note that manipulations of the underlying queue will not result
     * in notification to listeners.
     *
     * @param queue
     */
    public ObservableBlockingQueue(BlockingQueue<E> queue) {
        this.queue = queue;
    }

    /**
     * Creates an ObservableQueue backed by a LinkedBlockingQueue.
     */
    public ObservableBlockingQueue() {
        this(new LinkedBlockingQueue<>());
    }

    @Override
    public boolean offer(E e) {
        beginChange();
        boolean result = queue.offer(e);
        if (result) {
            nextAdd(queue.size() - 1, queue.size());
        }
        endChange();
        return result;
    }

    @Override
    public void put(E e) throws InterruptedException {
        beginChange();
        queue.put(e);
        nextAdd(queue.size() - 1, queue.size());
        endChange();
    }

    @Override
    public boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException {
        beginChange();
        boolean result;
        try {
            result = queue.offer(e, timeout, unit);
            if (result) {
                nextAdd(queue.size() - 1, queue.size());
            }
        } finally {
            endChange();
        }
        return result;
    }

    @Override
    public boolean add(E e) {
        beginChange();
        try {
            queue.add(e);
            nextAdd(queue.size() - 1, queue.size());
            return true;
        } finally {
            endChange();
        }
    }


    @Override
    public E remove() {
        beginChange();
        try {
            E e = queue.remove();
            nextRemove(0, e);
            return e;
        } finally {
            endChange();
        }
    }

    @Override
    public E poll() {
        beginChange();
        E e = queue.poll();
        if (e != null) {
            nextRemove(0, e);
        }
        endChange();
        return e;
    }

    @Override
    public E element() {
        return queue.element();
    }

    @Override
    public E peek() {
        return queue.peek();
    }

    @Override
    public E take() throws InterruptedException {
        beginChange();
        E e;
        try {
            e = queue.take();
            nextRemove(0, e);
        } finally {
            endChange();
        }
        return e;
    }

    @Override
    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        beginChange();
        E e;
        try {
            e = queue.poll(timeout, unit);
            if (e != null) {
                nextRemove(0, e);
            }
        } finally {
            endChange();
        }
        return e;
    }

    @Override
    public int remainingCapacity() {
        return queue.remainingCapacity();
    }

    @Override
    public int drainTo(Collection<? super E> c) {
        return drainTo(c, Integer.MAX_VALUE);
    }

    @Override
    public int drainTo(Collection<? super E> c, int maxElements) {
        int count = 0;
        while (size() > 0 && count < maxElements) {
            c.add(poll());
            count++;
        }
        return count;
    }


    @Override
    public E get(int index) {
        final Iterator<E> iterator = queue.iterator();
        for (int i = 0; i < index; i++)
            iterator.next();
        return iterator.next();
    }

    @Override
    public int size() {
        return queue.size();
    }

}
