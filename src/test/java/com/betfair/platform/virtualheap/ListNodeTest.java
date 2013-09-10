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

import com.betfair.platform.virtualheap.projection.ComplexListProjection;
import com.betfair.platform.virtualheap.projection.ProjectorFactory;
import com.betfair.platform.virtualheap.projection.ScalarListProjection;
import com.betfair.platform.virtualheap.projection.ScalarProjection;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 *
 */
public class ListNodeTest {

    @Test
    public void iterator() {
        Heap h = new MutableHeap("");
        h.beginUpdate();
        ScalarListProjection<Integer> list = ProjectorFactory.listProjector(ProjectorFactory.intProjector).project(h.ensureRoot(NodeType.LIST));
        list.addLast(1);
        list.addLast(2);
        list.addLast(3);
        h.endUpdate();

        assertEquals(3, list.size());
        int i=0;
        for (Integer n : list) {
            assertEquals(++i, n.intValue());
        }
    }

    @Test
    public void addRemove() {
        Heap h = new MutableHeap("");
        ImmutableHeap ih = new ImmutableHeap("", h, true);
        h.beginUpdate();
        ScalarListProjection<Integer> list = ProjectorFactory.listProjector(ProjectorFactory.intProjector).project(h.ensureRoot(NodeType.LIST));
        list.addLast(1);
        h.endUpdate();
        ScalarListProjection<Integer> ilist = ProjectorFactory.listProjector(ProjectorFactory.intProjector).project(h.getRoot());
        assertEquals(1, list.size());
        assertEquals(1, ilist.size());

        h.beginUpdate();
        list.addLast(2);
        h.endUpdate();
        assertEquals(2, list.size());
        assertEquals(2, ilist.size());

        h.beginUpdate();
        list.removeFirst();
        h.endUpdate();
        assertEquals(1, list.size());
        assertEquals(2, (int) list.get(0));
        assertEquals(1, ilist.size());
        assertEquals(2, (int) ilist.get(0));
    }
}
