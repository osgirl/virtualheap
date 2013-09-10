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

import com.betfair.platform.virtualheap.NodeType;
import com.betfair.platform.virtualheap.updates.*;
import com.betfair.platform.virtualheap.utils.HeapBuilder;
import com.betfair.platform.virtualheap.utils.RandomHeapBuilder;
import junit.framework.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * User: mcintyret2
 * Date: 23/07/2012
 */

public class SmartConflaterTest extends ConflaterTest {

    @Override
    protected Conflater getConflater() {
        return new SmartConflater();
    }

    @Test
    public void testSmartConflater()  {
        doConflaterTest(10000, false);
    }

    @Test
    public void testAcrossTransactionBoundaries()  {
        RandomHeapBuilder randomHeapBuilder = new RandomHeapBuilder();

        randomHeapBuilder.randomUpdate(1000);
        UpdateBlock tx1 = randomHeapBuilder.getLastUpdate();

        randomHeapBuilder.randomUpdate(1000);
        UpdateBlock tx2 = randomHeapBuilder.getLastUpdate();

        doTestAcrossTransactionBoundaries(tx1.list(), tx2.list(), false);
    }


    @Test
    public void singleTransactionEdgeCase1()  {
        // Index 1 is installed. Index 2 is installed later at the same index. Index 1 is then removed.
        // between the installation of B and the removal of 1, all installs that should potentially be
        // reindexed should take into account that 2 'uses up' one of the required index shifts.

        // In this case none of the subsequent installs need to be reindexed.

        List<Update> list = new LinkedList<Update>();

        list.add(new InstallRoot(0, NodeType.LIST));
        list.add(new InstallIndex(0, 1, 0, NodeType.SCALAR));
        list.add(new InstallIndex(0, 2, 0, NodeType.SCALAR));
        list.add(new InstallIndex(0, 3, 1, NodeType.SCALAR));
        list.add(new InstallIndex(0, 4, 2, NodeType.SCALAR));
        list.add(new RemoveIndex(0, 1, -1, null));

        doConflaterTest(list, false);

    }

    @Test
    public void singleTransactionEdgeCase2()  {
        // As in singleTransactioneEdgeCase1, except that index 4 DOES need to be reindexed because the number of indicies between
        // it and the previous install (2) is greater than the number consumed by index 2's install (1)

        List<Update> list = new LinkedList<Update>();

        list.add(new InstallRoot(0, NodeType.LIST));
        list.add(new InstallIndex(0, 1, 0, NodeType.SCALAR));
        list.add(new InstallIndex(0, 2, 0, NodeType.SCALAR));
        list.add(new InstallIndex(0, 3, 1, NodeType.SCALAR));
        list.add(new InstallIndex(0, 4, 3, NodeType.SCALAR));
        list.add(new RemoveIndex(0, 1, -1, null));

        doConflaterTest(list, false);
    }

    @Test
    public void multipleTransactionEdgeCase1()  {
        // This test shows that the removal of pre-existing indices effects the minIndex value - the minimum index at
        // which subsequent installs need to be reindexed.

        // Between the installation and removal of 5 we install 6 and 7. By the rules of singleTransactionEdgeCase1(),
        // we should not need to reindex these installs since 6 essentially replaces 5 (it is installed at the same index)
        // and 7 is installed immediately after. However, 2, a pre-existing index in a position before 7's destination,
        // is removed before 7 is installed. This means that 7 DOES need to be reindexed to ensure that it ends up in its
        // proper position in the heap, before 3.

        // This test will break if all references to removals of existing indices are removed from the onRemoveIndex() method
        // of the conflater. However, it will work again if the logic surrounding 'minIndex--' is reinstated - it does not
        // depend on the 'removed.decrementIndex()' (see multipleTransactionEdgeCase3) or on whether 'install.index' or
        // 'remove.index' is compared against minIndex (see multipleTransactionEdgeCase2).


        List<Update> tx1 = new ArrayList<Update>();
        tx1.add(new InstallRoot(0, NodeType.LIST));
        tx1.add(new InstallIndex(0, 1, 0, NodeType.SCALAR));
        tx1.add(new InstallIndex(0, 2, 1, NodeType.SCALAR));
        tx1.add(new InstallIndex(0, 3, 2, NodeType.SCALAR));

        List<Update> tx2 = new ArrayList<Update>(); // [1, 2, 3]
        tx2.add(new RemoveIndex(0, 1, -1, null)); // [2, 3]
        tx2.add(new InstallIndex(0, 5, 1, NodeType.SCALAR)); // [2, 5, 3]
        tx2.add(new InstallIndex(0, 6, 1, NodeType.SCALAR)); // [2, 6, 5, 3]
        tx2.add(new RemoveIndex(0, 2, -1, null)); // [6, 5, 3]
        tx2.add(new InstallIndex(0, 7, 2, NodeType.SCALAR)); // [6, 5, 7, 3]
        tx2.add(new RemoveIndex(0, 5, -1, null)); // [6, 7, 3]

        doTestAcrossTransactionBoundaries(tx1, tx2, false);

    }

    @Test
    public void multipleTransactionEdgeCase2()  {
        // This test shows that the index from which the pre-exising index was REMOVED (as opposed to where it was
        // originally INSTALLED) is important. If you modify the source code to use install.getIndex() < minIndex rather
        // than removed.getIndex() < minIndex this test will break.

        // This is because although 4 is installed at index 1, the removal of 1 just beforehand means 4 is effectively
        // installed at index 0, and this is the position it is later removed from. This is relevant to the reindexing
        // that then takes place.

        List<Update> tx1 = new ArrayList<Update>();
        tx1.add(new InstallRoot(0, NodeType.LIST));
        tx1.add(new InstallIndex(0, 1, 0, NodeType.SCALAR));
        tx1.add(new InstallIndex(0, 2, 1, NodeType.SCALAR));
        tx1.add(new InstallIndex(0, 3, 2, NodeType.SCALAR));

        List<Update> tx2 = new ArrayList<Update>();
        tx2.add(new RemoveIndex(0, 1, -1, null));
        tx2.add(new InstallIndex(0, 4, 1, NodeType.SCALAR));
        tx2.add(new RemoveIndex(0, 2, -1, null));
        tx2.add(new InstallIndex(0, 5, 1, NodeType.SCALAR));
        tx2.add(new RemoveIndex(0, 4, -1, null));

        doTestAcrossTransactionBoundaries(tx1, tx2, false);
    }

    @Test
    public void multipleTransactionEdgeCase3()  {
        // This test shows that remove instructions for pre-existing indices must be reindexed in the same way as the
        // install instructions. In this case the install of 5 means 1 is removed from index 1, but the subsequent
        // removal of 5 means the removal of 1 should be reindexed to 0. This is important for the reindexing of
        // 6 after 4 is removed - the reindexing of 1 means we know to reindex 6 too.

        // Simply commenting out the 'remove.decrementIndex()' line in SmartConflater.onIndexRemove will break this test.

        List<Update> tx1 = new ArrayList<Update>();
        tx1.add(new InstallRoot(0, NodeType.LIST));
        tx1.add(new InstallIndex(0, 1, 0, NodeType.SCALAR));
        tx1.add(new InstallIndex(0, 2, 1, NodeType.SCALAR));
        tx1.add(new InstallIndex(0, 3, 2, NodeType.SCALAR));

        List<Update> tx2 = new ArrayList<Update>();
        tx2.add(new InstallIndex(0, 4, 1, NodeType.SCALAR));
        tx2.add(new InstallIndex(0, 5, 0, NodeType.SCALAR));
        tx2.add(new RemoveIndex(0, 1, -1, null));
        tx2.add(new RemoveIndex(0, 5, -1, null));
        tx2.add(new InstallIndex(0, 6, 1, NodeType.SCALAR));
        tx2.add(new RemoveIndex(0, 4, -1, null));

        doTestAcrossTransactionBoundaries(tx1, tx2, false);
    }

    private void doTestAcrossTransactionBoundaries(List<? extends Update> tx1, List<? extends Update> tx2, boolean print) {
        HeapBuilder unconflated = new HeapBuilder(tx1);

        HeapBuilder conflated = new HeapBuilder(tx1);

        unconflated.update(tx2);
        UpdateBlock tx2conflated = getConflater().conflate(unconflated.getLastUpdate());

        if (print) {
            System.out.println("Heap: " + conflated.toString());

            System.out.println("Unconflated:");
            System.out.println(unconflated.getLastUpdate());
            System.out.println("Conflated:");
            System.out.println(tx2conflated);
        }

        conflated.update(tx2conflated);
        Assert.assertEquals("Heaps are not equivalent after conflation", unconflated, conflated);
        Assert.assertTrue("No conflation took place",
                          unconflated.getNumUpdatesApplied() > conflated.getNumUpdatesApplied());

        if (print) {
            System.out.println("Unconflated instructions: " + unconflated.getAllUpdates().list().size());
            System.out.println("Conflated instructions: " + conflated.getAllUpdates().list().size());
        }

    }

}
