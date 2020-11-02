package io.github.euonmyoji.pooltouhouinterface.util;

import junit.framework.TestCase;
import org.junit.Test;

import java.util.*;

/**
 * @author yinyangshi
 */
public class ObjectCollectionTest {
    private static final Object O = new Object();
    private static final int ADD_OBJ_COUNT = 1000;
    private static final int ADD_RM_TIMES = 1000;

    @Test
    public void writeWhileReading() {
        int[][] value = new int[5][1];

        UnsafeObjectPool<int[]> oc = new UnsafeObjectPool<>(value);
        oc.allocateObject()[0] = 1;
        oc.allocateObject()[0] = 2;
        oc.allocateObject()[0] = 2;

        Iterator<int[]> it = oc.allocatedObjectIterator();

        int i = 0;
        while (it.hasNext()) {
            if (++it.next()[0] == 3) {
                it.remove();
            }
            if (i++ <= 1) {
                oc.allocateObject()[0] = 0;
            }
        }
        it = oc.allocatedObjectIterator();
        List<Integer> list = new ArrayList<>();
        while (it.hasNext()) {
            list.add(it.next()[0]);
        }
        Collections.sort(list);
        TestCase.assertEquals(new ArrayList<Integer>() {{
            add(1);
            add(1);
            add(2);
        }}, list);
    }

    @Test
    public void testBaseOperator() {
        int[][] value = new int[5][1];

        UnsafeObjectPool<int[]> oc = new UnsafeObjectPool<>(value);
        oc.allocateObject()[0] = 1;
        oc.allocateObject()[0] = 2;
        oc.allocateObject()[0] = 3;
        TestCase.assertEquals(3, oc.count());

        Iterator<int[]> it = oc.allocatedObjectIterator();
        TestCase.assertTrue(it.hasNext());
        TestCase.assertEquals(1, it.next()[0]);
        TestCase.assertEquals(2, it.next()[0]);
        TestCase.assertEquals(3, it.next()[0]);
        TestCase.assertFalse(it.hasNext());
        it.remove();
        TestCase.assertFalse(it.hasNext());
        TestCase.assertEquals(2, oc.count());

    }

    @Test
    public void testRemove() {
        int[][] value = new int[5][1];

        UnsafeObjectPool<int[]> oc = new UnsafeObjectPool<>(value);
        oc.allocateObject()[0] = 1;
        oc.allocateObject()[0] = 2;
        oc.allocateObject()[0] = 3;
        Iterator<int[]> it = oc.allocatedObjectIterator();
        ArrayList<Integer> list = new ArrayList<>();
        list.add(it.next()[0]);
        it.remove();
        list.add(it.next()[0]);
        it.remove();
        list.add(it.next()[0]);
        it.remove();
        TestCase.assertFalse(it.hasNext());
        TestCase.assertEquals(0, oc.count());
        Collections.sort(list);
        TestCase.assertEquals(new ArrayList<Integer>() {{
            add(1);
            add(2);
            add(3);
        }}, list);
    }

    @Test()
    public void timeMyAddRemove() {
        Object[] objects = new Object[ADD_OBJ_COUNT];
        Arrays.fill(objects, O);
        UnsafeObjectPool<Object> oc = new UnsafeObjectPool<>(objects);
        for (int i = 0; i < ADD_RM_TIMES; i++) {
            for (int j = 0; j < ADD_OBJ_COUNT; j++) {
                oc.allocateObject();
            }
            Iterator<Object> it = oc.allocatedObjectIterator();
            while (it.hasNext()) {
                it.next();
                it.remove();
            }
        }
    }

    @Test()
    public void timeArrayListAddRemove() {
        Collection<Object> oc = new ArrayList<>();
        for (int i = 0; i < ADD_RM_TIMES; i++) {
            for (int j = 0; j < ADD_OBJ_COUNT; j++) {
                oc.add(O);
            }
            Iterator<Object> it = oc.iterator();
            while (it.hasNext()) {
                it.next();
                it.remove();
            }
        }

    }

    @Test()
    public void timeLinkedListAddRemove() {
        LinkedList<Object> oc = new LinkedList<>();
        for (int i = 0; i < ADD_RM_TIMES; i++) {
            for (int j = 0; j < ADD_OBJ_COUNT; j++) {
                oc.add(O);
            }
            Iterator<Object> it = oc.iterator();
            while (it.hasNext()) {
                it.next();
                it.remove();
            }
        }

    }
}
