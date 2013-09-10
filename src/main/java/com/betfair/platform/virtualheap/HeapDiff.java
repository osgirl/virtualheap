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

import java.util.*;

/**
 * User: mcintyret2
 * Date: 20/11/2012
 */
public class HeapDiff {

    public static HeapDiffBuilder getHeapDiffFrom(Heap from) {
        return new HeapDiffBuilder(from);
    }

    private HeapDiff() {

    }

    private static UpdateBlock getHeapDiff(Heap to, Heap from) {
        Node toRoot = to.getRoot();
        Node fromRoot = from.getRoot();

        List<Update> installs = new ArrayList<Update>();
        List<Update> removes = new ArrayList<Update>();

        if (fromRoot == null) {
            if (toRoot != null) {
                // Empty heap - simply add everything
                installs.add(new InstallRoot(toRoot.id, toRoot.getType()));
                deepAdd(toRoot, installs);
            }
        } else {
            if (toRoot == null) {
                throw new IllegalArgumentException("Cannot update a non-empty heap to an empty heap (cannot uninstall root nodes)");
            }
            getUpdates(toRoot, fromRoot, installs, removes);
        }

        List<Update> diffs = new ArrayList<Update>(removes);
        diffs.addAll(installs);

        return new UpdateBlock(diffs);
    }

    private static void getUpdates(Node toNode, Node fromNode, List<Update> installs, List<Update> removes) {
        if (toNode.getType() != fromNode.getType()) {
            throw new IllegalArgumentException("Error generating heap diff");
        }
        switch (toNode.getType()) {
            case MAP:
            case OBJECT:
                getMapUpdates((MapNode) toNode, (MapNode) fromNode, installs, removes);
                break;
            case LIST:
                getListUpdates((ListNode) toNode, (ListNode) fromNode, installs, removes);
                break;
            case SCALAR:
                getScalarUpdates((ScalarNode) toNode, (ScalarNode) fromNode, installs);
                break;
            default:
                throw new IllegalArgumentException("Unknown NodeType: " + toNode.getType());

        }
    }

    private static void getListUpdates(ListNode toNode, ListNode fromNode, List<Update> installs, List<Update> removes) {

        // get the longest common subsequence
        List<Node> lcs = lcs(toNode, fromNode);

        // Remove everything from 2 that isn't in lcs
        Iterator<Node> lcsIt = lcs.iterator();
        int index = 0;
        while (lcsIt.hasNext() && index < fromNode.size()) {
            Node fromLcs = lcsIt.next();
            Node fromListTwo;
            while (!nodesEqualExclIndex(fromLcs, fromListTwo = fromNode.getIndexAt(index++))) {
                removes.add(new RemoveIndex(fromNode.id, fromListTwo.id, fromListTwo.index, null));
            }
            getUpdates(fromLcs, fromListTwo, installs, removes);
        }
        for (int i = index; i < fromNode.size(); i++) {
            Node toRemove = fromNode.getIndexAt(i);
            removes.add(new RemoveIndex(fromNode.id, toRemove.id, toRemove.index, null));
        }

        // Add everything in 1 that isn't in lcs
        lcsIt = lcs.iterator();
        index = 0;
        while (lcsIt.hasNext() && index < toNode.size()) {
            Node fromLcs = lcsIt.next();
            Node fromListOne;
            while (!nodesEqualExclIndex(fromLcs, fromListOne = toNode.getIndexAt(index++))) {
                installs.add(new InstallIndex(toNode.id, fromListOne.id, fromListOne.index, fromListOne.getType()));
                deepAdd(fromListOne, installs);
            }
        }
        for (int i = index; i < toNode.size(); i++) {
            Node toInstall = toNode.getIndexAt(i);
            installs.add(new InstallIndex(toNode.id, toInstall.id, toInstall.index, toInstall.getType()));
            deepAdd(toInstall, installs);
        }

    }

    private static List<Node> lcs(final ListNode one, final ListNode two) {
        final int[][] table = new int[one.size() + 1][two.size() +1];
        for (int i = 0; i < table.length; i++) {
            for (int j = 0; j < table[0].length; j++) {
                if (i == 0 || j == 0) {
                    table[i][j] = 0;
                } else if (nodesEqualExclIndex(one.getIndexAt(i-1), two.getIndexAt(j-1))) {
                    table[i][j] = table[i-1][j-1] + 1;
                } else {
                    table[i][j] = Math.max(table[i-1][j], table[i][j-1]);
                }
            }
        }

        class Reconstructor {
            List<Node> reconstruct(int x, int y) {
                if (x == 0 || y == 0) {
                    return new ArrayList<Node>();
                } else if (nodesEqualExclIndex(one.getIndexAt(x - 1), two.getIndexAt(y - 1))) {
                    List<Node> list = reconstruct(x-1, y-1);
                    list.add(one.getIndexAt(x-1));
                    return list;
                } else if (table[x-1][y] > table[x][y-1]) {
                    return reconstruct(x-1, y);
                } else {
                    return reconstruct(x, y-1);
                }
            }
        }

        return new Reconstructor().reconstruct(one.size(), two.size());
    }

    private static void getScalarUpdates(ScalarNode toNode, ScalarNode fromNode, List<Update> installs) {
        if (!equalsNullSafe(toNode.get(), fromNode.get())) {
            installs.add(new SetScalar(fromNode.id, toNode.get()));
        }
    }

    private static void getMapUpdates(MapNode toNode, MapNode fromNode, List<Update> installs, List<Update> removes) {
        Set<String> inOneNotTwo = difference(toNode.getFields(), fromNode.getFields());
        for (String field : inOneNotTwo) {
            Node child = toNode.getField(field);
            installs.add(new InstallField(fromNode.id, child.id, field, child.getType()));
            deepAdd(child, installs);
        }
        Set<String> inTwoOneNot = difference(fromNode.getFields(), toNode.getFields());
        for (String field : inTwoOneNot) {
            Node child = fromNode.getField(field);
            removes.add(new RemoveField(fromNode.id, child.id, child.name, null));
        }
        // Possible that we have children with the same name but are actually different
        Set<String> inBoth = intersection(toNode.getFields(), fromNode.getFields());
        for (String field : inBoth) {
            Node oneChild = toNode.getField(field);
            Node twoChild = fromNode.getField(field);
            if (!nodesEqual(oneChild, twoChild)) {
                removes.add(new RemoveField(fromNode.id, twoChild.id, twoChild.name, null));
                installs.add(new InstallField(fromNode.id, oneChild.id, oneChild.name, oneChild.getType()));
                deepAdd(oneChild, installs);
            } else {
                getUpdates(oneChild, twoChild, installs, removes);
            }
        }

    }

    // Called when a new node has been added, so we want to add all of its children
    private static void deepAdd(Node node, List<Update> installs) {
        switch (node.getType()) {
            case MAP:
            case OBJECT:
                MapNode mapNode = (MapNode) node;
                for (String field : mapNode.getFields()) {
                    Node child = mapNode.getField(field);
                    installs.add(new InstallField(node.id, child.id, field, child.getType()));
                    deepAdd(child, installs);
                }
                break;
            case LIST:
                ListNode listNode = (ListNode) node;
                for (int i = 0; i < listNode.size(); i++) {
                    Node child = listNode.getIndexAt(i);
                    installs.add(new InstallIndex(node.id, child.id, i, child.getType()));
                    deepAdd(child, installs);
                }
                break;
            case SCALAR:
                ScalarNode scalarNode = (ScalarNode) node;
                if (scalarNode.get() != null) {
                    installs.add(new SetScalar(node.id, scalarNode.get()));
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown NodeType: " + node.getType());
        }
    }

    private static boolean equalsNullSafe(Object a, Object b) {
        return a == null ? b == null : a.equals(b);
    }

    private static boolean nodesEqualExclIndex(Node a, Node b) {
        return a.getType() == b.getType() &&
                a.id == b.id &&
                equalsNullSafe(a.name, b.name);
    }

    private static boolean nodesEqual(Node a, Node b) {
        return nodesEqualExclIndex(a, b) &&
                a.index == b.index;
    }

    private static Set<String> difference(Set<String> a, Set<String> b) {
        Set<String> aCopy = new HashSet<String>(a);
        aCopy.removeAll(b);
        return aCopy;
    }

    private static Set<String> intersection(Set<String> a, Set<String> b) {
        Set<String> aCopy = new HashSet<String>(a);
        aCopy.retainAll(b);
        return aCopy;
    }

    public static class HeapDiffBuilder {

        private final Heap fromHeap;

        public HeapDiffBuilder(Heap fromHeap) {
            this.fromHeap = fromHeap;
        }

        public UpdateBlock to(Heap toHeap) {
            return getHeapDiff(toHeap, fromHeap);
        }
    }
}
