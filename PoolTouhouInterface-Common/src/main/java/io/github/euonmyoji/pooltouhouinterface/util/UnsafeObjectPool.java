package io.github.euonmyoji.pooltouhouinterface.util;

import java.util.Iterator;

/**
 * A not-ordered collection for object
 * if you are reading from iterator and allocated object, you will read the object that just allocated.
 *
 * @param <E> The element type
 * @author yinyangshi
 */
public class UnsafeObjectPool<E> {
    private final ObjectIterator iterator = new ObjectIterator();
    private final E[] array;
    private int allocatedCount = 0;


    /**
     * the non-null object array for pool to use
     *
     * @param array the array that every element is non-null for pool to use
     */
    public UnsafeObjectPool(E[] array) {
        this.array = array;
    }

    public int count() {
        return allocatedCount;
    }

    public boolean contains(Object o) {
        for (int i = 0; i < allocatedCount; i++) {
            if (array[i].equals(o)) {
                return true;
            }
        }
        return false;
    }


    public Iterator<E> allocatedObjectIterator() {
        this.iterator.i = 0;
        return this.iterator;
    }


    public E allocateObject() {
        return array[allocatedCount++];
    }

    public boolean remove(Object o) {
        for (int i = 0; i < allocatedCount; i++) {
            if (array[i].equals(o)) {
                remove(i);
                return true;
            }
        }
        return false;
    }


    public void releaseAll() {
        allocatedCount = 0;
    }

    private void remove(int index) {
        allocatedCount -= 1;
        if (index != allocatedCount) {
            E obj = array[index];
            array[index] = array[allocatedCount];
            array[allocatedCount] = obj;
        }

    }

    public class ObjectIterator implements Iterator<E> {
        private int i = 0;

        @Override
        public boolean hasNext() {
            return i < allocatedCount;
        }

        @Override
        public E next() {
            return UnsafeObjectPool.this.array[i++];
        }

        @Override
        public void remove() {
            i -= 1;
            UnsafeObjectPool.this.remove(i);
        }
    }

}
