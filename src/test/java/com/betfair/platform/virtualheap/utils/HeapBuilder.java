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
package com.betfair.platform.virtualheap.utils;

import com.betfair.platform.virtualheap.HeapListener;
import com.betfair.platform.virtualheap.MutableHeap;
import com.betfair.platform.virtualheap.Node;
import com.betfair.platform.virtualheap.updates.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * User: mcintyret2
 * Date: 16/07/2012
 *
 * A wrapper for Heap objects that adds utility methods and makes some otherwise private fields available through
 * reflection
 *
 */
public class HeapBuilder extends MutableHeap {

    protected final List<Update> allUpdates = new LinkedList<Update>();
    protected final List<Update> lastUpdate = new LinkedList<Update>();

    public HeapBuilder() {
        super("some uri");

        addListener(new HeapListener() {
            @Override
            public void applyUpdate(UpdateBlock update) {
                allUpdates.addAll(update.list());

                lastUpdate.clear();
                lastUpdate.addAll(update.list());
            }
        }, false);
    }

    public HeapBuilder(UpdateBlock block) {
        this();
        update(block);
    }

    public HeapBuilder(List<? extends Update> updates) {
        this(new UpdateBlock(updates));
    }

    public void update(Update delta) {
        List<Update> updates = new ArrayList<Update>();
        updates.add(delta);
        update(updates);
    }

    public void update(List<? extends Update> deltas) {
        update(new UpdateBlock(deltas));
    }

    public void update(UpdateBlock updateBlock) {
        asListener().applyUpdate(updateBlock);
    }

    @Override
    public String toString() {
        return removeTrailingNulls(new ArrayList<Node>(heap)).toString();
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (other == this) {
            return true;
        }

        if (!(other instanceof HeapBuilder)) {
            return false;
        }
        return removeTrailingNulls(heap).equals(removeTrailingNulls(((HeapBuilder) other).heap));
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    private static List<?> removeTrailingNulls(List<?> list) {
        ListIterator<?> it = list.listIterator(list.size());
        while (it.hasPrevious() && it.previous() == null) {
            it.remove();
        }
        return list;
    }

    public UpdateBlock getAllUpdates() {
        return new UpdateBlock(allUpdates);
    }

    public UpdateBlock getLastUpdate() {
        return new UpdateBlock(new ArrayList<Update>(lastUpdate));
    }

    public int getNumUpdatesApplied() {
        return allUpdates.size();
    }


}
