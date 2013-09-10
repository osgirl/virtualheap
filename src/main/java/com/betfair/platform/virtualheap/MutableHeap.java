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
import com.betfair.platform.virtualheap.updates.*;

import java.util.*;


public class MutableHeap extends Heap {

    private int idgen = 0;
    // previously deallocated ids which are available for reuse
    private Queue<Integer> availableIds = new ArrayDeque<Integer>();
    protected List<Node> heap = new ArrayList<Node>();
    private Node root = null;
    private Heap srcHeap;
    private Conflater newListenerConflater;
    private UpdateBlock newListenerState = new UpdateBlock();

    public MutableHeap(String uri) {
        super(uri);
    }

    public MutableHeap(String uri, Conflater newListenerConflater) {
        super(uri);
        this.newListenerConflater = newListenerConflater;
    }

    public MutableHeap(String uri, Heap srcHeap, boolean populateWithCurrentState) {
        this(uri);
        this.srcHeap = srcHeap;
        srcHeap.addListener(asListener(), populateWithCurrentState);
    }

    public MutableHeap(String uri, Heap srcHeap, boolean populateWithCurrentState, Conflater newListenerConflater) {
        this(uri, newListenerConflater);
        this.srcHeap = srcHeap;
        srcHeap.addListener(asListener(), populateWithCurrentState);
    }

    public void disconnectFromSourceHeap() {
        if (srcHeap != null) {
            srcHeap.removeListener(asListener());
            srcHeap = null;
        }
    }

    int allocateId() {
        if (!availableIds.isEmpty()) {
            return availableIds.poll();
        }
        return idgen++;
    }

    Node allocateNode(int id, NodeType type) {
        Node node = createNode(id, type);
        while (heap.size() <= id) {
            heap.add(null);
        }
        heap.set(id, node);
        return node;
    }

    protected Node createNode(int id, NodeType type) {
        Node node;
        switch (type) {
            case LIST:
                node = new ListNode(id, this);
                break;
            case MAP:
                node = new MapNode(id, this);
                break;
            case OBJECT:
                node = new ObjectNode(id, this);
                break;
            case SCALAR:
                node = new ScalarNode(id, this);
                break;
            default:
                throw new IllegalArgumentException("Can't create node for type: " + type);
        }
        return node;
    }

    void deallocateNode(final boolean fromListener, Node node, final Set<Integer> deallocatedIds) {
        heap.set(node.id, null);
        if (!fromListener) {
            availableIds.add(node.id);
        }
        node.visitChildren(new NodeVisitor() {
            @Override
            public void visitNode(Node child) {
                heap.set(child.id, null);
                deallocatedIds.add(child.id);
                if (!fromListener) {
                    availableIds.add(child.id);
                }
            }
        });
    }

    public Node ensureRoot(NodeType type) {
        if (root != null) {
            if (!root.getType().equals(type)) {
                throw new IllegalArgumentException("Can't change node type of an existing root node");
            }
            return root;
        }
        installRoot(false, allocateId(), type);
        return root;
    }

    Node getNode(int id) {
        return heap.get(id);
    }

    public Node getRoot() {
        return root;
    }

    @Override
    public boolean isRootInstalled() {
        return root != null;
    }

    @Override
    protected void onEndUpdate(UpdateBlock block) {
        if (newListenerConflater != null) {
            newListenerState = newListenerConflater.conflate(newListenerState, block);
        }
        super.onEndUpdate(block);
    }

    /**
     * Give the current state of this heap to the given listener. Requires that this listener is not already listening to the heap.
     */
    public void traverse(final HeapListener listener) {
        lock.readLock().lock();
        try {
            if (getListeners() != null && getListeners().contains(listener)) {
                throw new IllegalArgumentException("Passed listener is already listening to this heap!");
            }
            if (newListenerConflater != null) {
                listener.applyUpdate(newListenerState);
            }
            else {
                List<Update> updates = new HeapCopyingNodeVisitor().copy(getRoot());

                // last thing if we're there..
                if (terminated) {
                    updates.add(new TerminateHeap());
                }
                listener.applyUpdate(new UpdateBlock(updates));
            }
        }
        finally {
            lock.readLock().unlock();
        }
    }

    // --- Package private methods

    void assertCanUpdate() {
        assertLock();
        assertHaveUpdateBlock();
        assertNotTerminated();
    }

    // ---- Protected methods

    protected void removeField(boolean fromListener, int parentId, int id) {
        MapNode parent = (MapNode) heap.get(parentId);
        if (parent == null) {
            throw new IllegalStateException("Trying to remove a field from a parentId which doesn't map to a node: "+ parentId);
        }
        parent.removeField(fromListener, id);
    }

    protected void removeIndex(boolean fromListener, int parentId, int id) {
        Node parent = heap.get(parentId);
        if (parent instanceof ListNode) {
            ((ListNode) parent).removeById(fromListener, id);
        }
        else {
            throw new IllegalArgumentException("Can't remove an index from a node of type "+parent.getClass().getName());
        }
    }

    protected void installField(boolean fromListener, int parentId, int id, String name, NodeType type) {
        MapNode parent = (MapNode) heap.get(parentId);
        parent.installField(fromListener, id, name, type);
    }

    protected void installRoot(boolean fromListener, int id, NodeType type) {
        if (isRootInstalled()) {
            throw new IllegalStateException("Root already installed");
        }
        this.root = allocateNode(id, type);
        emit(new InstallRoot(id, type));
    }

    @SuppressWarnings("unchecked")
    protected void setScalar(boolean fromListener, int id, Object value) {
        ScalarNode node = (ScalarNode) heap.get(id);
        node.set(fromListener, value);
    }

    protected void removeChildren(boolean fromListener, int id) {
        Node node = heap.get(id);
        switch (node.getType()) {
            case LIST: ((ListNode)node).clear(fromListener); return;
            case MAP: ((MapNode)node).clear(fromListener); return;
            case OBJECT: ((ObjectNode)node).clear(fromListener); return;
        }
        throw new IllegalStateException(id +  " " + node.getClass() + " " + node.getType());
    }

    protected void installIndex(boolean fromListener, int parentId, int id, int index, NodeType type) {
        Node parent = heap.get(parentId);
        if (parent instanceof ListNode) {
            ((ListNode) parent).installIndex(fromListener, id, index, type);
        }
        else {
            throw new IllegalArgumentException("Can't install an index on a node of type "+parent.getClass().getName());
        }
    }

    // --- Private methods

    private static class HeapCopyingNodeVisitor implements NodeVisitor {

        private final List<Update> updates = new ArrayList<Update>();

        public List<Update> copy(Node rootNode) {
            if (rootNode != null) {
                updates.add(new InstallRoot(rootNode.id, rootNode.getType()));
                visitNode(rootNode);
                rootNode.visitChildren(this);
            }
            return updates;

        }

        @Override
        public void visitNode(Node node) {
            switch (node.getType()) {
                case MAP:
                case OBJECT:
                    MapNode mapNode = (MapNode) node;
                    for (String field : mapNode.getFields()) {
                        Node childNode = mapNode.getField(field);
                        if (childNode != null) {
                            updates.add(new InstallField(node.id, childNode.id, field, childNode.getType()));
                        }
                    }
                    break;
                case LIST:
                    ListNode listNode = (ListNode) node;
                    for (int i = 0; i < listNode.size(); i++) {
                        Node childNode = listNode.getIndexAt(i);
                        if (childNode != null) {
                            updates.add(new InstallIndex(node.id, childNode.id, childNode.index, childNode.getType()));
                        }
                    }
                    break;
                case SCALAR:
                    ScalarNode scalarNode = (ScalarNode) node;
                    updates.add(new SetScalar(node.id, scalarNode.value));
                    break;
                default:
                    throw new AssertionError("Unknown node type: " + node.getType());

            }
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (Node n : heap) {
            sb.append(n).append("\n");
        }

        return sb.toString();
    }

    public String prettyPrint() {
        return root == null ? "null" : root.prettyPrint();
    }
}
