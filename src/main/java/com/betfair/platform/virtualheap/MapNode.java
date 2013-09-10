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

import com.betfair.platform.virtualheap.updates.InstallField;
import com.betfair.platform.virtualheap.updates.RemoveChildren;
import com.betfair.platform.virtualheap.updates.RemoveField;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class MapNode extends Node implements CollectionNode {

    private final Map<String, Node> children = new LinkedHashMap<String, Node>();

    MapNode(int id, Heap heap) {
        super(id, heap);
    }

    Node installField(boolean fromListener, int id, String name, NodeType type) {
        if (!fromListener) {
            beforeMutation();
        }
        Node child = heap.allocateNode(id, type);
        child.name = name;
        children.put(name, child);
        heap.emit(new InstallField(this.id, id, name, type));
        return child;
    }

    void removeField(boolean fromListener, Node child) {
        if (!fromListener) {
            beforeMutation();
        }
        if (child == null) {
            return;
        }

        Set<Integer> deallocatedIds = new HashSet<Integer>();
        children.remove(child.name);
        heap.deallocateNode(fromListener, child, deallocatedIds);
        deallocatedIds.add(child.id);
        heap.emit(new RemoveField(this.id, child.id, child.name, deallocatedIds));
    }

    void removeField(boolean fromListener, int id) {
        Node child = heap.getNode(id);
        removeField(fromListener, child);
    }

    public Node getField(String name) {
        return children.get(name);
    }

    public Node ensureField(String name, NodeType type) {
        Node node = getField(name);
        if (node == null) {
            node = installField(false, heap.allocateId(), name, type);
        }
        return node;
    }

    public void removeField(String name) {
        removeField(false, children.get(name));
    }

    public int size() {
        return children.size();
    }

    public void clear() {
        clear(false);
    }

    void clear(boolean fromListener) {
        if (size() == 0) {
            return;
        }

        if (!fromListener) {
            beforeMutation();
        }
        Set<Integer> deallocatedIds = new HashSet<Integer>();
        for (Node child : children.values()) {
            heap.deallocateNode(fromListener, child, deallocatedIds);
            deallocatedIds.add(child.id);
        }
        children.clear();
        heap.emit(new RemoveChildren(this.id, deallocatedIds));
    }

    @Override
    void visitChildren(NodeVisitor visitor) {
        for (Node child : children.values()) {
            visitor.visitNode(child);
            child.visitChildren(visitor);
        }
    }

    public String toString() {
        String s = "{";
        int i = 0;
        for (Map.Entry<String, Node> e : children.entrySet()) {
            s += e.getKey() + ":" + e.getValue().id;
            if (i < children.size() - 1) {
                s += ", ";
            }
            i++;
        }
        s += "}";
        return s;
    }

    @Override
    public String prettyPrint(int depth, boolean collapse) {
        StringBuilder sb = new StringBuilder();
        if (!collapse) {
            for (int i = 0; i < depth; i++) {
                sb.append("  ");
            }
        }
        sb.append('{');
        int count = 0;
        for (Map.Entry<String, Node> entry : children.entrySet()) {
            if (!collapse) {
                sb.append("\n");
                for (int i = 0; i <= depth; i++) {
                    sb.append("  ");
                }
            }
            sb.append(entry.getKey()).append(": ").append(entry.getValue().prettyPrint(depth + 1, collapse));
            if (count++ < children.entrySet().size() - 1) {
                sb.append(", ");
            }
        }
        if (!collapse) {
            sb.append("\n");
            for (int i = 0; i < depth; i++) {
                sb.append("  ");
            }
        }
        return sb.append('}').toString();
    }

    public NodeType getType() {
        return NodeType.MAP;
    }

    public Set<String> getFields() {
        return children.keySet();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        MapNode mapNode = (MapNode) o;

        if (children != null ? !children.equals(mapNode.children) : mapNode.children != null) {
            return false;
        }

        return true;
    }

}
