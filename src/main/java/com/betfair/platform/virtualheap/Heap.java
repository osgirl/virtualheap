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

import com.betfair.platform.virtualheap.updates.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public abstract class Heap extends ObservableHeap {

    private final String uri;

    protected ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private HeapListener meAsListener;
    protected boolean terminated = false;

    private List<Update> currentUpdates;

    public Heap(String uri) {
        this.uri = uri;
    }

    public String getUri() {
        return uri;
    }

    /**
     * Get this heap as a listener. Allows chaining of heaps.
     */
    public HeapListener asListener() {
        if (meAsListener == null) {
            meAsListener = new HeapListener() {

                @Override
                public void applyUpdate(UpdateBlock update) {
                    Heap.this.beginUpdate();
                    for (Update u : update.list()) {
                        switch (u.getUpdateType()) {
                            case INSTALL_ROOT:
                                InstallRoot installRoot = (InstallRoot) u;
                                Heap.this.installRoot(true, installRoot.getId(), installRoot.getType());
                                break;
                            case INSTALL_FIELD:
                                InstallField installField = (InstallField) u;
                                Heap.this.installField(true, installField.getParentId(), installField.getId(),
                                                       installField.getName(), installField.getType());
                                break;
                            case INSTALL_INDEX:
                                InstallIndex installIndex = (InstallIndex) u;
                                Heap.this.installIndex(true, installIndex.getParentId(), installIndex.getId(),
                                                       installIndex.getIndex(), installIndex.getType());
                                break;
                            case SET_SCALAR:
                                SetScalar setScalar = (SetScalar) u;
                                Heap.this.setScalar(true, setScalar.getId(), setScalar.getValue());
                                break;
                            case REMOVE_FIELD:
                                RemoveField removeField = (RemoveField) u;
                                Heap.this.removeField(true, removeField.getParentId(), removeField.getId());
                                break;
                            case REMOVE_INDEX:
                                RemoveIndex removeIndex = (RemoveIndex) u;
                                Heap.this.removeIndex(true, removeIndex.getParentId(), removeIndex.getId());
                                break;
                            case REMOVE_CHILDREN:
                                RemoveChildren removeChildren = (RemoveChildren) u;
                                Heap.this.removeChildren(true, removeChildren.getId());
                                break;
                            case TERMINATE_HEAP:
                                Heap.this.terminateHeap();
                                break;
                            default:
                                throw new IllegalStateException("Unrecognised update type: "+u.getUpdateType());
                        }
                    }
                    Heap.this.endUpdate();
                }
            };
        }
        return meAsListener;
    }

    public void beginUpdate() {
        lock.writeLock().lock();
        if (currentUpdates !=null) {
            throw new IllegalStateException("Heap has current update block, did you finish the previous update?");
        }
        currentUpdates = new ArrayList<Update>();
    }

    public UpdateBlock endUpdate() {
        try {
            assertHaveUpdateBlock();
            UpdateBlock block = new UpdateBlock(currentUpdates);
            onEndUpdate(block);
            currentUpdates = null;
            return block;
        }
        finally {
            lock.writeLock().unlock();
        }
    }

    public void terminateHeap() {
        assertLock();
        terminated = true;
        emit(new TerminateHeap());
    }

    public boolean isTerminated() {
        return terminated;
    }

    public void addListener(HeapListener listener, boolean populateWithCurrentState) {
        if (lock.writeLock().isHeldByCurrentThread()) {
            throw new IllegalStateException("Listeners cannot be added whilst holding the lock");
        }
        lock.readLock().lock();
        try {
            // pass the current state to the listener
            if (populateWithCurrentState) {
                traverse(listener);
            }
            super.addListener(listener);
        } finally {
            lock.readLock().unlock();
        }
    }

    public abstract boolean isRootInstalled();

    public abstract Node ensureRoot(NodeType type);

    public abstract Node getRoot();

    public abstract void traverse(HeapListener listener);

    // ---- Protected methods

    protected void assertLock() {
        if (!lock.writeLock().isHeldByCurrentThread()) {
            throw new IllegalStateException("Lock not held by current thread");
        }
    }

    protected void assertNotTerminated() {
        if (terminated) {
            throw new IllegalStateException("Heap has been terminated and so can't take further updates");
        }
    }

    protected void assertHaveUpdateBlock() {
        if (currentUpdates ==null) {
            throw new IllegalStateException("Heap doesn't have a current update block");
        }
    }

    protected abstract void removeField(boolean fromListener, int parentId, int id);

    protected abstract void removeIndex(boolean fromListener, int parentId, int id);

    protected abstract void installField(boolean fromListener, int parentId, int id, String name, NodeType type);

    protected abstract void installRoot(boolean fromListener, int id, NodeType type);

    protected abstract void setScalar(boolean fromListener, int id, Object value);

    protected abstract void removeChildren(boolean fromListener, int id);

    protected abstract void installIndex(boolean fromListener, int parentId, int id, int index, NodeType type);

    // --- Package private methods

    void emit(Update delta) {
        if (currentUpdates ==null) {
            throw new IllegalStateException("Being asked to emit an delta, yet we don't appear to have started an update");
        }
        currentUpdates.add(delta);
    }

    abstract void assertCanUpdate();

    abstract int allocateId();

    abstract Node allocateNode(int id, NodeType type);

    abstract void deallocateNode(boolean fromListener, Node node, final Set<Integer> deallocatedIds);

    abstract Node getNode(int id);

}
