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

import com.betfair.platform.virtualheap.conflate.Conflater;

/**
 *
 */
public class ImmutableHeap extends MutableHeap {
    public ImmutableHeap(String uri) {
        super(uri);
    }
    public ImmutableHeap(String uri, Conflater newListenerConflater) {
        super(uri, newListenerConflater);
    }

    public ImmutableHeap(String uri, Heap srcHeap, boolean populateWithCurrentState) {
        super(uri, srcHeap, populateWithCurrentState);
    }

    public ImmutableHeap(String uri, Heap srcHeap, boolean populateWithCurrentState, Conflater newListenerConflater) {
        super(uri, srcHeap, populateWithCurrentState, newListenerConflater);
    }

    @Override
    public void assertLock() {
        // do nothing, locks not required for immutable heaps
    }

    @Override
    protected Node createNode(int id, NodeType type) {
        Node node;
        switch(type) {
            case LIST:
                node = new ListNode(id, this) {
                    @Override
                    protected void beforeMutation() {
                        throw new ImmutableHeapException("Can't mutate an immutable heap");
                    }
                };
                break;
            case OBJECT:
                node = new ObjectNode(id, this) {
                    @Override
                    protected void beforeMutation() {
                        throw new ImmutableHeapException("Can't mutate an immutable heap");
                    }
                };
                break;
            case MAP:
                node = new MapNode(id, this) {
                    @Override
                    protected void beforeMutation() {
                        throw new ImmutableHeapException("Can't mutate an immutable heap");
                    }
                };
                break;
            case SCALAR:
                node = new ScalarNode(id, this) {
                    @Override
                    protected void beforeMutation() {
                        throw new ImmutableHeapException("Can't mutate an immutable heap");
                    }
                };
                break;
            default: throw new IllegalArgumentException("Can't create node for type: "+type);
        }
        return node;
    }

    private class ImmutableListNode extends ListNode {
        private ImmutableListNode(int id, MutableHeap heap) {
            super(id, heap);
        }

        @Override
        protected void beforeMutation() {
            throw new ImmutableHeapException("Can't mutate an immutable heap");
        }
    }


    @Override
    protected void installRoot(boolean fromListener, int id, NodeType type) {
        if (!fromListener) {
            throw new ImmutableHeapException("Can't install root on an immutable heap");
        }
        super.installRoot(fromListener, id, type);
    }

    @Override
    protected void setScalar(boolean fromListener, int id, Object value) {
        if (!fromListener) {
            throw new ImmutableHeapException("Can't set scalar on an immutable heap");
        }
        super.setScalar(fromListener, id, value);
    }

    @Override
    protected void removeChildren(boolean fromListener, int id) {
        if (!fromListener) {
            throw new ImmutableHeapException("Can't remove children on an immutable heap");
        }
        super.removeChildren(fromListener, id);
    }

    @Override
    protected void installIndex(boolean fromListener, int parentId, int id, int index, NodeType type) {
        if (!fromListener) {
            throw new ImmutableHeapException("Can't install index on an immutable heap");
        }
        super.installIndex(fromListener, parentId, id, index, type);
    }

    @Override
    protected void removeIndex(boolean fromListener, int parentId, int id) {
        if (!fromListener) {
            throw new ImmutableHeapException("Can't remove index on an immutable heap");
        }
        super.removeIndex(fromListener, parentId, id);
    }

    @Override
    protected void installField(boolean fromListener, int parentId, int id, String name, NodeType type) {
        if (!fromListener) {
            throw new ImmutableHeapException("Can't install field on an immutable heap");
        }
        super.installField(fromListener, parentId, id, name, type);
    }

    @Override
    protected void removeField(boolean fromListener, int parentId, int id) {
        if (!fromListener) {
            throw new ImmutableHeapException("Can't remove field on an immutable heap");
        }
        super.removeField(fromListener, parentId, id);
    }
}
