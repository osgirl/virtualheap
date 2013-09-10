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

import com.betfair.platform.virtualheap.projection.*;
import org.junit.Test;

import static com.betfair.platform.virtualheap.projection.ProjectorFactory.booleanProjector;

/**
 *
 */
public class ImmutableHeapTest {

    @Test(expected = ImmutableHeapException.class)
    public void rootInstallationFails() {
        ImmutableHeap heap = new ImmutableHeap("");
        heap.installRoot(false, 0, NodeType.SCALAR);
    }

    @Test(expected = ImmutableHeapException.class)
    public void fieldInstallationFails() {
        Heap src = new MutableHeap("src");
        ImmutableHeap heap = new ImmutableHeap("");
        src.addListener(heap.asListener());
        src.beginUpdate();
        src.installRoot(false, 0, NodeType.MAP);
        src.endUpdate();
        
        heap.installField(false, 0, 1, "fred", NodeType.SCALAR);
    }

    @Test(expected = ImmutableHeapException.class)
    public void indexInstallationFails() {
        Heap src = new MutableHeap("src");
        ImmutableHeap heap = new ImmutableHeap("");
        src.addListener(heap.asListener());
        src.beginUpdate();
        src.installRoot(false, 0, NodeType.LIST);
        src.endUpdate();

        heap.installIndex(false, 0, 1, 0, NodeType.SCALAR);
    }

    @Test(expected = ImmutableHeapException.class)
    public void scalarSet() {
        Heap src = new MutableHeap("src");
        ImmutableHeap heap = new ImmutableHeap("");
        src.addListener(heap.asListener());
        src.beginUpdate();
        src.installRoot(false, 0, NodeType.SCALAR);
        src.endUpdate();

        heap.beginUpdate();
        ScalarProjection<Boolean> root = ProjectorFactory.booleanProjector.project(heap.getRoot());
        root.set(false);
        heap.endUpdate();
    }

    @Test(expected = ImmutableHeapException.class)
    public void listAdd() {
        Heap src = new MutableHeap("src");
        ImmutableHeap heap = new ImmutableHeap("");
        src.addListener(heap.asListener());
        src.beginUpdate();
        src.installRoot(false, 0, NodeType.LIST);
        src.endUpdate();

        heap.beginUpdate();
        ScalarListProjection<Boolean> root = ProjectorFactory.listProjector(ProjectorFactory.booleanProjector).project(heap.getRoot());
        root.addFirst(false);
        heap.endUpdate();
    }

    @Test(expected = ImmutableHeapException.class)
    public void listRemove() {
        Heap src = new MutableHeap("src");
        ImmutableHeap heap = new ImmutableHeap("");
        src.addListener(heap.asListener());
        src.beginUpdate();
        src.installRoot(false, 0, NodeType.LIST);
        src.installIndex(false, 0, 1, 0, NodeType.SCALAR);
        src.endUpdate();

        heap.beginUpdate();
        ScalarListProjection<Boolean> root = ProjectorFactory.listProjector(ProjectorFactory.booleanProjector).project(heap.getRoot());
        root.removeFirst();
        heap.endUpdate();
    }

    @Test(expected = ImmutableHeapException.class)
    public void mapPut() {
        Heap src = new MutableHeap("src");
        ImmutableHeap heap = new ImmutableHeap("");
        src.addListener(heap.asListener());
        src.beginUpdate();
        src.installRoot(false, 0, NodeType.MAP);
        src.endUpdate();

        heap.beginUpdate();
        ScalarMapProjection<Boolean> root = ProjectorFactory.mapProjector(ProjectorFactory.booleanProjector).project(heap.getRoot());
        root.put("a", null);
        heap.endUpdate();
    }

    @Test(expected = ImmutableHeapException.class)
    public void mapPutSet() {
        Heap src = new MutableHeap("src");
        ImmutableHeap heap = new ImmutableHeap("");
        src.addListener(heap.asListener());
        src.beginUpdate();
        src.installRoot(false, 0, NodeType.MAP);
        src.endUpdate();

        heap.beginUpdate();
        ScalarMapProjection<Boolean> root = ProjectorFactory.mapProjector(ProjectorFactory.booleanProjector).project(heap.getRoot());
        root.put("a", false);
        heap.endUpdate();
    }

    @Test(expected = ImmutableHeapException.class)
    public void mapRemove() {
        Heap src = new MutableHeap("src");
        ImmutableHeap heap = new ImmutableHeap("");
        src.addListener(heap.asListener());
        src.beginUpdate();
        src.installRoot(false, 0, NodeType.MAP);
        src.installField(false, 0, 1, "a", NodeType.SCALAR);
        src.endUpdate();

        heap.beginUpdate();
        ScalarMapProjection<Boolean> root = ProjectorFactory.mapProjector(ProjectorFactory.booleanProjector).project(heap.getRoot());
        root.remove("a");
        heap.endUpdate();
    }

    @Test(expected = ImmutableHeapException.class)
    public void objectSet() {
        Heap src = new MutableHeap("src");
        ImmutableHeap heap = new ImmutableHeap("");
        src.addListener(heap.asListener());
        src.beginUpdate();
        src.installRoot(false, 0, NodeType.OBJECT);
        src.endUpdate();

        heap.beginUpdate();
        ObjectNode root = ProjectorFactory.objectProjector(ObjectNode.class).project(heap.getRoot());
        root.a().set(false);
        heap.endUpdate();
    }

    public static class ObjectNode {
        private com.betfair.platform.virtualheap.ObjectNode mapNode;

        public ObjectNode(com.betfair.platform.virtualheap.ObjectNode mapNode) {
            this.mapNode = mapNode;
        }

        public ScalarProjection<Boolean> a() {
            return mapNode.ensureField("a", booleanProjector.getType()).project(booleanProjector);
        }
    }


}
