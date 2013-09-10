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

import com.betfair.platform.virtualheap.projection.ProjectorFactory;
import com.betfair.platform.virtualheap.projection.ScalarProjection;
import com.betfair.platform.virtualheap.updates.InstallRoot;
import com.betfair.platform.virtualheap.updates.SetScalar;
import com.betfair.platform.virtualheap.updates.UpdateBlock;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * Created with IntelliJ IDEA.
 * User: langfords
 * Date: 10/07/12
 * Time: 12:15
 * To change this template use File | Settings | File Templates.
 */
public class ScalarNodeTest {
    @Test
    public void setNull() {
        Heap h = new MutableHeap("");
        h.beginUpdate();
        ScalarProjection<Integer> scalar = ProjectorFactory.intProjector.project(h.ensureRoot(NodeType.SCALAR));
        scalar.set(null);
        UpdateBlock block = h.endUpdate();

        assertEquals(1, block.list().size());
        assertTrue(block.list().get(0) instanceof InstallRoot);
    }

    @Test
    public void setNullAfterSetting() {
        Heap h = new MutableHeap("");
        h.beginUpdate();
        ScalarProjection<Integer> scalar = ProjectorFactory.intProjector.project(h.ensureRoot(NodeType.SCALAR));
        scalar.set(1);
        scalar.set(null);
        UpdateBlock block = h.endUpdate();

        assertEquals(3, block.list().size());
        assertTrue(block.list().get(0) instanceof InstallRoot);
        assertTrue(block.list().get(1) instanceof SetScalar);
        assertEquals(1, ((SetScalar) block.list().get(1)).getValue());
        assertTrue(block.list().get(2) instanceof SetScalar);
        assertEquals(null, ((SetScalar) block.list().get(2)).getValue());
    }

    @Test
    public void setNewValue() {
        Heap h = new MutableHeap("");
        h.beginUpdate();
        ScalarProjection<Integer> scalar = ProjectorFactory.intProjector.project(h.ensureRoot(NodeType.SCALAR));
        scalar.set(1);
        scalar.set(2);
        UpdateBlock block = h.endUpdate();

        assertEquals(3, block.list().size());
        assertTrue(block.list().get(0) instanceof InstallRoot);
        assertTrue(block.list().get(1) instanceof SetScalar);
        assertEquals(1, ((SetScalar) block.list().get(1)).getValue());
        assertTrue(block.list().get(2) instanceof SetScalar);
        assertEquals(2, ((SetScalar) block.list().get(2)).getValue());
    }
}
