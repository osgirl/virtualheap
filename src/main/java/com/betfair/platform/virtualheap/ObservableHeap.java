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

import com.betfair.platform.virtualheap.updates.Update;
import com.betfair.platform.virtualheap.updates.UpdateBlock;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public abstract class ObservableHeap {
    private Set<HeapListener> listeners;

    protected void addListener(HeapListener listener) {
        if (listeners == null) {
            listeners = new CopyOnWriteArraySet<HeapListener>();
        }
        listeners.add(listener);
    }

    public void removeListener(HeapListener listener) {
        if (listeners != null) {
            listeners.remove(listener);
        }
    }

    protected Set<HeapListener> getListeners() {
        return listeners;
    }

    protected void onEndUpdate(UpdateBlock block) {
        if (listeners != null && !block.list().isEmpty()) {
            for (HeapListener listener : listeners) {
                listener.applyUpdate(block);
            }
        }
    }

    abstract void emit(Update delta);
}
