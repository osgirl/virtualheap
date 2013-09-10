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

import com.betfair.platform.virtualheap.updates.UpdateBlock;
import com.betfair.platform.virtualheap.utils.HeapBuilder;
import com.betfair.platform.virtualheap.utils.RandomHeapBuilder;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * User: mcintyret2
 * Date: 20/11/2012
 */
public class HeapDiffTest {

    @Test
    public void shouldGenerateHeapDiff() {

        // Get two identical heaps
        RandomHeapBuilder to = new RandomHeapBuilder(1000);
        RandomHeapBuilder from = new RandomHeapBuilder(to.getAllUpdates());

        // Diverge them
        to.randomUpdate(100);
        from.randomUpdate(100);

        // generate the diff
        UpdateBlock diff = HeapDiff.getHeapDiffFrom(from).to(to);

        // apply the diff
        from.update(diff);

        // test that the diff was accurate
        assertEquals(to, from);

    }

    @Test
    public void shouldReturnEmptyUpdateBlockForTwoEmptyHeaps() {
        // given
        Heap to = new MutableHeap(null);
        Heap from = new MutableHeap(null);

        // when
        UpdateBlock block = HeapDiff.getHeapDiffFrom(from).to(to);

        // then
        assertTrue(block.list().isEmpty());
    }

    @Test
    public void shouldReturnEmptyUpdateBlockForTwoIdenticalHeaps() {
        // Get two identical heaps
        RandomHeapBuilder to = new RandomHeapBuilder(1000);
        RandomHeapBuilder from = new RandomHeapBuilder(to.getAllUpdates());

        // when
        UpdateBlock block = HeapDiff.getHeapDiffFrom(from).to(to);

        // then
        assertTrue(block.list().isEmpty());
    }

    @Test
    public void shouldGenerateHeapDiffWhenFromHeapIsEmpty() {
        // given
        Heap to = new RandomHeapBuilder(1000);
        Heap from = new MutableHeap(null);

        // when
        UpdateBlock block = HeapDiff.getHeapDiffFrom(from).to(to);
        Heap result = new HeapBuilder(block);

        // then
        assertEquals(to, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailConvertingNonEmptyToEmpty() {
         // given
        Heap to = new MutableHeap(null);
        Heap from = new RandomHeapBuilder(1000);

        // when
        HeapDiff.getHeapDiffFrom(from).to(to);
    }

}
