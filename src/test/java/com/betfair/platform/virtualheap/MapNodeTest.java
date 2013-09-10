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

import com.betfair.platform.virtualheap.projection.AbstractMapProjection;
import com.betfair.platform.virtualheap.projection.ProjectorFactory;
import com.betfair.platform.virtualheap.projection.ScalarMapProjection;
import com.betfair.platform.virtualheap.projection.ScalarProjection;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 *
 */
public class MapNodeTest {

    @Test
    public void addRemove() {
        Heap h = new MutableHeap("");
        ImmutableHeap ih = new ImmutableHeap("", h, true);
        h.beginUpdate();
        ScalarMapProjection<Integer> map = ProjectorFactory.mapProjector(ProjectorFactory.intProjector).project(h.ensureRoot(NodeType.MAP));
        map.put("1",1);
        h.endUpdate();
        ScalarMapProjection<Integer> imap = ProjectorFactory.mapProjector(ProjectorFactory.intProjector).project(h.getRoot());
        assertEquals(1, map.size());
        assertEquals(1, imap.size());

        h.beginUpdate();
        map.put("2",2);
        h.endUpdate();
        assertEquals(2, map.size());
        assertEquals(2, imap.size());

        h.beginUpdate();
        map.remove("1");
        h.endUpdate();
        assertEquals(1, map.size());
        assertEquals(2, (int) map.get("2"));
        assertEquals(1, imap.size());
        assertEquals(2, (int) imap.get("2"));
    }
}
