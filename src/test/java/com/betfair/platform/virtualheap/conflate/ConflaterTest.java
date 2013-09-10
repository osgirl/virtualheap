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
package com.betfair.platform.virtualheap.conflate;

import com.betfair.platform.virtualheap.updates.Update;
import com.betfair.platform.virtualheap.updates.UpdateBlock;
import com.betfair.platform.virtualheap.utils.HeapBuilder;
import com.betfair.platform.virtualheap.utils.RandomHeapBuilder;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * User: mcintyret2
 * Date: 23/07/2012
 */
public abstract class ConflaterTest {

    protected abstract Conflater getConflater();

    public void doConflaterTest(List<Update> updates, boolean print)  {
        RandomHeapBuilder randomHeapBuilder = new RandomHeapBuilder();
        randomHeapBuilder.update(updates);
        doConflaterTest(randomHeapBuilder, print);
    }

    public void doConflaterTest(int updates, boolean print)  {
        RandomHeapBuilder randomHeapBuilder = new RandomHeapBuilder();
        randomHeapBuilder.randomUpdate(updates);

        doConflaterTest(randomHeapBuilder, print);
    }

    private void doConflaterTest(RandomHeapBuilder randomHeapBuilder, boolean print)  {
        Conflater conflater = getConflater();

        if (print) {
            System.out.println("Unconflated:");
            System.out.println(randomHeapBuilder.getAllUpdates());
            System.out.println();
        }

        UpdateBlock conflatedDeltas = conflater.conflate(randomHeapBuilder.getAllUpdates());

        if (print) {
            System.out.println("Conflated:");
            System.out.println(conflatedDeltas);
        }

        HeapBuilder conflatedHeap = new HeapBuilder(conflatedDeltas);

        assertEquals(randomHeapBuilder, conflatedHeap);
    }

}
