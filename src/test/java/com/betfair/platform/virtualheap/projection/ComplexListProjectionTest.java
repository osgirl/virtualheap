/*
 Copyright 2013, The Sporting Exchange Limited

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
package com.betfair.platform.virtualheap.projection;

import com.betfair.platform.virtualheap.Heap;
import com.betfair.platform.virtualheap.ListNode;
import com.betfair.platform.virtualheap.MutableHeap;
import com.betfair.platform.virtualheap.NodeType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;

/**
 *
 */
public class ComplexListProjectionTest {

    private MutableHeap heap;
    private ListNode listNode;
    private ObjectProjector<ObjectNode> objectProjector;
    private ComplexListProjection<ObjectNode> complexListProjection;

    @Before
    public void before() {
        heap = new MutableHeap("test");
        heap.beginUpdate();
        listNode = (ListNode) heap.ensureRoot(NodeType.LIST);
        objectProjector = new ObjectProjector<ObjectNode>(ObjectNode.class);

        complexListProjection = new ComplexListProjection<ObjectNode>(listNode, objectProjector);
    }

    @After
    public void after() {
        heap.endUpdate();
    }


    @Test
    public void testGet() {
        complexListProjection.addLast().val = 1;
        assertEquals(1,complexListProjection.get(0).val);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testIndexOutOfRange() {
        complexListProjection.addLast().val = 1;
        complexListProjection.get(1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testInsertAtOutOfBounds() {
        complexListProjection.insertAt(5).val = 5;
    }

    @Test
    public void testInsertAtWhenEmpty() {
        complexListProjection.insertAt(0).val = 0;
        assertEquals(0,complexListProjection.get(0).val);
    }

    @Test
    public void testInsertAtEnd() {
        complexListProjection.insertAt(0).val = 0;
        complexListProjection.insertAt(1).val = 1;

        assertEquals(0,complexListProjection.get(0).val);
        assertEquals(1,complexListProjection.get(1).val);
    }

    @Test
    public void testInsertAt0() {
        complexListProjection.insertAt(0).val = 0;
        assertEquals(0,complexListProjection.get(0).val);
    }

    @Test
    public void testInsertAtBetween() {
        complexListProjection.addLast().val = 0;
        complexListProjection.addLast().val = 2;
        complexListProjection.insertAt(1).val = 1;
        assertEquals(0,complexListProjection.get(0).val);
        assertEquals(1,complexListProjection.get(1).val);
        assertEquals(2,complexListProjection.get(2).val);
    }

    @Test
    public void addLast() {
        complexListProjection.addFirst().val = 1;
        complexListProjection.addFirst().val = 0;
        complexListProjection.addLast().val = 2;

        assertEquals(0,complexListProjection.get(0).val);
        assertEquals(1,complexListProjection.get(1).val);
        assertEquals(2,complexListProjection.get(2).val);

    }

    @Test
    public void addFirst() {
        complexListProjection.addLast().val = 1;
        complexListProjection.addLast().val = 2;
        complexListProjection.addFirst().val = 0;

        assertEquals(0,complexListProjection.get(0).val);
        assertEquals(1,complexListProjection.get(1).val);
        assertEquals(2,complexListProjection.get(2).val);

    }


    @Test
    public void testIterate() {
        complexListProjection.addLast().val = 0;
        complexListProjection.addLast().val = 1;
        complexListProjection.addLast().val = 2;

        int i=0;
        for (Iterator<ObjectNode> itr = complexListProjection.iterator(); itr.hasNext(); ) {
            ObjectNode objectNode = itr.next();
            assertEquals(i++,objectNode.val);
        }
    }

    @Test
    public void testIteratorRemoveMiddle() {


        for (int i=0; i<3; i++) {
            complexListProjection.addLast().val = i;
        }

        List<Integer> mirror = new ArrayList<Integer>(Arrays.asList(0,1,2));

        Iterator<ObjectNode> objectNodeIterator = complexListProjection.iterator();
        Iterator<Integer> mirrorIterator = mirror.iterator();

        while (mirrorIterator.hasNext()) {
            Integer integer = mirrorIterator.next();
            ObjectNode objectNode = objectNodeIterator.next();

            assertEquals(integer.intValue(),objectNode.val);

            if (objectNode.val == 1) {
                objectNodeIterator.remove();
                mirrorIterator.remove();
            }
        }

        assertEquals(mirror.size(),complexListProjection.size());
        for (int i=0; i<mirror.size(); i++) {
            assertEquals(mirror.get(i).intValue(),complexListProjection.get(i).val);
        }

    }


    @Test
    public void testIteratorRemoveLast() {

        for (int i=0; i<3; i++) {
            complexListProjection.addLast().val = i;
        }

        List<Integer> mirror = new ArrayList<Integer>(Arrays.asList(0,1,2));

        Iterator<ObjectNode> objectNodeIterator = complexListProjection.iterator();
        Iterator<Integer> mirrorIterator = mirror.iterator();

        while (mirrorIterator.hasNext()) {
            Integer integer = mirrorIterator.next();
            ObjectNode objectNode = objectNodeIterator.next();

            assertEquals(integer.intValue(),objectNode.val);

            if (objectNode.val == 2) {
                objectNodeIterator.remove();
                mirrorIterator.remove();
            }
        }

        assertEquals(mirror.size(),complexListProjection.size());
        for (int i=0; i<mirror.size(); i++) {
            assertEquals(mirror.get(i).intValue(),complexListProjection.get(i).val);
        }

    }


    @Test
    public void testIteratorRemoveFirst() {
        for (int i=0; i<3; i++) {
            complexListProjection.addLast().val = i;
        }

        List<Integer> mirror = new ArrayList<Integer>(Arrays.asList(0,1,2));

        Iterator<ObjectNode> objectNodeIterator = complexListProjection.iterator();
        Iterator<Integer> mirrorIterator = mirror.iterator();

        while (mirrorIterator.hasNext()) {
            Integer integer = mirrorIterator.next();
            ObjectNode objectNode = objectNodeIterator.next();

            assertEquals(integer.intValue(),objectNode.val);

            if (objectNode.val == 0) {
                objectNodeIterator.remove();
                mirrorIterator.remove();
            }
        }

        assertEquals(mirror.size(),complexListProjection.size());
        for (int i=0; i<mirror.size(); i++) {
            assertEquals(mirror.get(i).intValue(),complexListProjection.get(i).val);
        }

    }



    @Test
    public void testIteratorRemoveAll() {


        for (int i=0; i<3; i++) {
            complexListProjection.addLast().val = i;
        }

        int i=0;
        for (Iterator<ObjectNode> itr = complexListProjection.iterator(); itr.hasNext(); ) {
            assertEquals(i++, itr.next().val);
            itr.remove();
        }

        assertEquals(0,complexListProjection.size());
    }


    public static class ObjectNode {
        int val;

        public ObjectNode(com.betfair.platform.virtualheap.ObjectNode node) {
        }

    }
}
