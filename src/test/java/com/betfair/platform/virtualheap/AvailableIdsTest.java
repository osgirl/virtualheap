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
package com.betfair.platform.virtualheap;

import org.junit.Test;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Queue;

import static junit.framework.Assert.assertEquals;

/**
 *
 */
public class AvailableIdsTest {

    @Test
    public void noDuplicateIds() throws Exception{
        System.out.println("\n1:");
        MutableHeap heap = new MutableHeap("sdsdf");
        heap.beginUpdate();
        heap.ensureRoot(NodeType.LIST);
        ListNode root = ((ListNode) heap.getNode(0));
        System.out.println(heap.endUpdate());

        System.out.println("\n2:");
        heap.beginUpdate();
        ObjectNode node1 = (ObjectNode) root.insertAt(0, NodeType.OBJECT);
        node1.ensureField("id", NodeType.SCALAR);
        node1.ensureField("message", NodeType.SCALAR);
        System.out.println(heap.endUpdate());

        System.out.println("\n3:");
        heap.beginUpdate();
        ObjectNode node2 = (ObjectNode) root.insertAt(1, NodeType.OBJECT);
        ((ScalarNode<String>)node2.ensureField("id", NodeType.SCALAR)).set("Test Id");
        ((ScalarNode<String>)node2.ensureField("message", NodeType.SCALAR)).set("Test Message");
        root.removeIndex(0);
        System.out.println(heap.endUpdate());

        System.out.println("\n4:");
        heap.beginUpdate();
        node1 = (ObjectNode) root.insertAt(1, NodeType.OBJECT);
        ((ScalarNode<String>)node1.ensureField("id", NodeType.SCALAR)).set("Testing Id 2");
        ((ScalarNode<String>)node1.ensureField("message", NodeType.SCALAR)).set("Testing Message 2");
        System.out.println(heap.endUpdate());

        Field f = MutableHeap.class.getDeclaredField("availableIds");
        f.setAccessible(true);
        Queue<Integer> availableIds = (Queue<Integer>) f.get(heap);

        assertEquals(new HashSet<Integer>(availableIds).size(), availableIds.size());


        System.out.println("\n5:");
        heap.beginUpdate();
        ObjectNode node3 = (ObjectNode) root.insertAt(2, NodeType.OBJECT);
        node3.ensureField("id", NodeType.SCALAR);
        node3.ensureField("message", NodeType.SCALAR);
        root.removeIndex(1);
        root.removeIndex(0);
        System.out.println(heap.endUpdate());

        assertEquals(new HashSet<Integer>(availableIds).size(), availableIds.size());

    }


}
