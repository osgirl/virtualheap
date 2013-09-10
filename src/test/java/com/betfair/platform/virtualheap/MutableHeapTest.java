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

import static junit.framework.Assert.assertEquals;

public class MutableHeapTest {

    @Test(expected = IllegalStateException.class)
    public void installRootOutsideLock() {
        MutableHeap heap = new MutableHeap("");
        heap.installRoot(false, 0, NodeType.SCALAR);
    }

    @Test(expected = IllegalStateException.class)
    public void installFieldOutsideLock() {
        MutableHeap heap = new MutableHeap("");
        heap.beginUpdate();
        heap.installRoot(false, 0, NodeType.MAP);
        heap.endUpdate();

        heap.installField(false, 0, 1, "fred", NodeType.SCALAR);
    }

    @Test(expected = IllegalStateException.class)
    public void removeFieldOutsideLock() {
        MutableHeap heap = new MutableHeap("");
        heap.beginUpdate();
        heap.installRoot(false, 0, NodeType.MAP);
        heap.installField(false, 0, 1, "fred", NodeType.SCALAR);
        heap.endUpdate();

        heap.removeField(false, 0, 1);
    }

    @Test(expected = IllegalStateException.class)
    public void setScalarOutsideLock() {
        MutableHeap heap = new MutableHeap("");
        heap.beginUpdate();
        heap.installRoot(false, 0, NodeType.SCALAR);
        heap.endUpdate();

        heap.setScalar(false, 0, "someValue");
    }

    @Test(expected = IllegalStateException.class)
    public void installIndexOutsideLock() {
        MutableHeap heap = new MutableHeap("");
        heap.beginUpdate();
        heap.installRoot(false, 0, NodeType.LIST);
        heap.endUpdate();

        heap.installIndex(false, 0, 1, 0, NodeType.SCALAR);
    }

    @Test(expected = IllegalStateException.class)
    public void removeIndexOutsideLock() {
        MutableHeap heap = new MutableHeap("");
        heap.beginUpdate();
        heap.installRoot(false, 0, NodeType.LIST);
        heap.installIndex(false, 0, 1, 0, NodeType.SCALAR);
        heap.endUpdate();

        heap.removeIndex(false, 0, 1);
    }

    @Test(expected = IllegalStateException.class)
    public void removeChildrenOutsideLock() {
        MutableHeap heap = new MutableHeap("");
        heap.beginUpdate();
        heap.installRoot(false, 0, NodeType.LIST);
        heap.installIndex(false, 0, 1, 0, NodeType.SCALAR);
        heap.endUpdate();

        heap.removeChildren(false, 0);
    }

    @Test(expected = IllegalStateException.class)
    public void terminateHeapOutsideLock() {
        MutableHeap heap = new MutableHeap("");
        heap.beginUpdate();
        heap.installRoot(false, 0, NodeType.LIST);
        heap.installIndex(false, 0, 1, 0, NodeType.SCALAR);
        heap.endUpdate();

        heap.terminateHeap();
    }

    @Test
    public void ensureRootIdempotent() {
        MutableHeap heap = new MutableHeap("");
        heap.beginUpdate();
        heap.ensureRoot(NodeType.SCALAR);
        heap.ensureRoot(NodeType.SCALAR);
        heap.endUpdate();

        heap = new MutableHeap("");
        heap.beginUpdate();
        heap.ensureRoot(NodeType.LIST);
        heap.ensureRoot(NodeType.LIST);
        heap.endUpdate();

        heap = new MutableHeap("");
        heap.beginUpdate();
        heap.ensureRoot(NodeType.MAP);
        heap.ensureRoot(NodeType.MAP);
        heap.endUpdate();

        heap = new MutableHeap("");
        heap.beginUpdate();
        heap.ensureRoot(NodeType.OBJECT);
        heap.ensureRoot(NodeType.OBJECT);
        heap.endUpdate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void cantChangeRootType() {
        MutableHeap heap = new MutableHeap("");
        heap.beginUpdate();
        heap.ensureRoot(NodeType.SCALAR);
        heap.ensureRoot(NodeType.OBJECT);
        heap.endUpdate();
    }

    @Test
    public void copying() {
        MutableHeap heap = new MutableHeap("");
        heap.beginUpdate();
        heap.installRoot(false, 0, NodeType.LIST);

        heap.installIndex(false, 0, 1, 0, NodeType.MAP);
        heap.installField(false, 1, 2, "list", NodeType.LIST);
        heap.endUpdate();
        Heap copy = new MutableHeap("", heap, true);
        assertEquals(heap.toString(), copy.toString());
    }
}
