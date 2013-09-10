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

import com.betfair.platform.virtualheap.updates.InstallIndex;
import com.betfair.platform.virtualheap.updates.RemoveChildren;
import com.betfair.platform.virtualheap.updates.RemoveIndex;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ListNode extends Node implements CollectionNode {
    private final List<Node> children = new ArrayList<Node>();

    ListNode(int id, MutableHeap heap) {
        super(id, heap);
    }

    Node installIndex(boolean fromListener, int id, int index, NodeType type) {
        if (!fromListener) {
            beforeMutation();
        }
        Node child = heap.allocateNode(id, type);
        child.index = index;
        children.add(index, child);
        //re-index
        for (int i = index + 1; i < children.size(); i++) {
            children.get(i).index = i;
        }
        heap.emit(new InstallIndex(this.id, id, index, type));
        return child;
    }

    void removeById(boolean fromListener, int id) {
        if (!fromListener) {
            beforeMutation();
        }
        Set<Integer> deallocatedIds = new HashSet<Integer>();
        Node child = heap.getNode(id);
        children.remove(child.index);
        //re-index
        for (int i = child.index; i < children.size(); i++) {
            Node next = children.get(i);
            next.index = i;
        }
        heap.deallocateNode(fromListener, child, deallocatedIds);
        deallocatedIds.add(child.id);
        heap.emit(new RemoveIndex(this.id, id, child.index, deallocatedIds));
    }

    public Node getIndexAt(int index) {
        return children.get(index);
    }

    public Node insertAt(int index, NodeType type) {
        return installIndex(false, heap.allocateId(), index, type);
    }

    public void removeIndex(int index) {
        removeById(false, children.get(index).id);
    }

    @Override
    void visitChildren(NodeVisitor visitor) {
        for (Node child : children) {
            visitor.visitNode(child);
            child.visitChildren(visitor);
        }
    }

    public void clear() {
       clear(false);
    }

    void clear(boolean fromListener) {
        if (!fromListener) {
            beforeMutation();
        }
        if (size() == 0) {
            return;
        }

        Set<Integer> deallocatedIds = new HashSet<Integer>();
        for (Node child : children) {
            heap.deallocateNode(fromListener, child, deallocatedIds);
            deallocatedIds.add(child.id);
        }
        children.clear();
        heap.emit(new RemoveChildren(this.id, deallocatedIds));
    }

    public int size() {
        return children.size();
    }

    public String toString() {
        StringBuilder s = new StringBuilder("[");
        int i = 0;
        for (Node child : children) {
            s.append(child.id);
            if (i < children.size() - 1) {
                s.append(", ");
            }
            i++;
        }
        return s.append(']').toString();
    }

    @Override
    public String prettyPrint(int depth, boolean collapse) {
        StringBuilder s = new StringBuilder("[");
        int i = 0;
        for (Node child : children) {
            s.append(child.prettyPrint(depth, true));
            if (i < children.size() - 1) {
                s.append(", ");
            }
            i++;
        }
        return s.append(']').toString();
    }

    public NodeType getType() {
        return NodeType.LIST;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ListNode listNode = (ListNode) o;

        if (children != null ? !children.equals(listNode.children) : listNode.children != null) {
            return false;
        }

        return true;
    }

    public List<Node> children() {
        return children;
    }

}
