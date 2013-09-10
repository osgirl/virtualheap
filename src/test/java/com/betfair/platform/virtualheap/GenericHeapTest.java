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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.List;

import static junit.framework.Assert.assertEquals;

/**
 *
 */
@RunWith(Parameterized.class)
public class GenericHeapTest {

    private Class<? extends Heap> heapClass;

    @Parameterized.Parameters
    public static List<Object[]> parameters() {
        return Arrays.asList(new Object[][]{{MutableHeap.class}, {ImmutableHeap.class}});
    }

    public GenericHeapTest(Class<? extends Heap> heapClass) {
        this.heapClass = heapClass;
    }

    private Heap createHeap(String uri, Heap src, boolean populateWithCurrentState) throws Exception {
        if (src == null) {
            return heapClass.getConstructor(String.class).newInstance(uri);
        } else {
            return heapClass.getConstructor(String.class, Heap.class, boolean.class).newInstance(uri, src, populateWithCurrentState);
        }
    }

    @Test
    public void listeningHeap() throws Exception {
        Heap src = new MutableHeap("src");

        // test connected heap
        Heap mid = createHeap("mid", src, true);

        src.beginUpdate();
        ScalarProjection<Integer> root = ProjectorFactory.intProjector.project(src.ensureRoot(NodeType.SCALAR));
//        ScalarNode<Integer> root = src.ensureRoot(Domain.intFactory);

        root.set(1);
        src.endUpdate();
        assertEquals(src.toString(), mid.toString());

        src.beginUpdate();
        root.set(null);
        src.endUpdate();
        assertEquals(src.toString(), mid.toString());

        src.beginUpdate();
        root.set(2);
        src.endUpdate();
        assertEquals(src.toString(), mid.toString());

        // now test adding a heap later..
        Heap target = createHeap("target", mid, true);

        assertEquals(src.toString(), target.toString());

        src.beginUpdate();
        root.set(3);
        src.endUpdate();
        assertEquals(src.toString(), target.toString());
    }


}
