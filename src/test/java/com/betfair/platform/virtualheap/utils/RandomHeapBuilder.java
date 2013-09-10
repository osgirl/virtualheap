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

import com.betfair.platform.virtualheap.*;
import com.betfair.platform.virtualheap.updates.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * User: mcintyret2
 * Date: 16/07/2012
 */
public class RandomHeapBuilder extends HeapBuilder {

    private static final Random RNG = new Random();
    private static final int MIN_REMOVE_CHILDREN_CHANCE = 4;

    public RandomHeapBuilder() {

    }

    public RandomHeapBuilder(UpdateBlock block) {
        super(block);
    }

    public RandomHeapBuilder(int randomUpdates) {
        super();
        randomUpdate(randomUpdates);
    }

    public void randomUpdate() {
        update(getRandomUpdate());
    }

    public void randomUpdate(int n) {
        int existingUpdates = allUpdates.size();
        for (int i = 0; i < n; i++) {
            randomUpdate();
        }
        lastUpdate.clear();
        lastUpdate.addAll(allUpdates.subList(existingUpdates, allUpdates.size()));
    }

    private Update getRandomUpdate() {
        if (!isRootInstalled()) {
            return new InstallRoot(allocateId(), RNG.nextBoolean() ? NodeType.LIST : NodeType.MAP);
        } else {
            Node randomNode = null;
            while (randomNode == null) {
                randomNode = heap.get(RNG.nextInt(heap.size()));
            }
            return getRandomUpdate(randomNode);
        }
    }

    private Update getRandomUpdate(Node node) {
        switch (node.getType()) {
            case LIST:
                return getRandomUpdate((ListNode) node);
            case MAP:
            case OBJECT:
                return getRandomUpdate((MapNode) node);
            case SCALAR:
                return getRandomUpdate((ScalarNode) node);
            default:
                throw new AssertionError("Unknown NodeType: " + node.getType());
        }
    }

    private Update getRandomUpdate(ListNode list) {
        if (list.size() == 0 || RNG.nextBoolean()) {
             // create a new node 1/2 time or if the list is empty
            return new InstallIndex(getNodeId(list), allocateId(), RNG.nextInt(list.size() + 1), randomNodeType());
        } else {
            // Removal operation 1/2 the time
            if (shouldRemoveChildren(list)) {
                return new RemoveChildren(getNodeId(list), null);
            } else {
                Node toDelete = list.getIndexAt(randomChildIndex(list));
                return new RemoveIndex(getNodeId(list), getNodeId(toDelete), -1, null);
            }
        }
    }


    private Update getRandomUpdate(ScalarNode node) {
        return new SetScalar(getNodeId(node), String.valueOf(RNG.nextInt()));
    }

    private Update getRandomUpdate(MapNode map) {
        if (shouldAddNewChild(map)) {
            // create a new node 1/2 times or if the map is empty
            return createNewMapEntry(map);
        } else {
            // Removal operation 1/2 the time
            if (shouldRemoveChildren(map)) {
                return new RemoveChildren(getNodeId(map), null);
            } else {
                Node toDelete = map.getField(new ArrayList<String>(map.getFields()).get(randomChildIndex(map)));
                return new RemoveField(getNodeId(map), getNodeId(toDelete), null, null);
            }
        }
    }


    private boolean shouldAddNewChild(CollectionNode node) {
        return node.size() == 0 || RNG.nextBoolean();
    }

    private boolean shouldRemoveChildren(CollectionNode node) {
        return RNG.nextInt(Math.max(MIN_REMOVE_CHILDREN_CHANCE, node.size())) == 0;
    }

    private int randomChildIndex(CollectionNode node) {
        return RNG.nextInt(node.size());
    }

    private Update createNewMapEntry(MapNode map) {
        NodeType newNodeType = randomNodeType();
        String name = Integer.toString(RNG.nextInt());

        return new InstallField(getNodeId(map), allocateId(), name, newNodeType);
    }

    private static NodeType randomNodeType() {
        return NodeType.values()[RNG.nextInt(NodeType.values().length)];
    }

    private static Field nodeIdField;
    private static int getNodeId(Node node) {
        try {
            if (nodeIdField == null) {
                nodeIdField = Node.class.getDeclaredField("id");
                nodeIdField.setAccessible(true);
            }
            return (Integer) nodeIdField.get(node);
        } catch (Exception e) {
            throw new AssertionError("Could not extract id for node: " + node);
        }
    }

    private static Field nodeNameField;
    private static String getNodeName(Node node) {
        try {
            if (nodeNameField == null) {
                nodeNameField = Node.class.getDeclaredField("name");
                nodeNameField.setAccessible(true);
            }
            return (String) nodeNameField.get(node);
        } catch (Exception e) {
            throw new AssertionError("Could not extract name for node: " + node);
        }
    }

    private List<Integer> available;
    private int allocateId() {
        if (available == null) {
            available = new ArrayList<Integer>();
            for (int i = 0; i < heap.size(); i++) {
                if (heap.get(i) == null) {
                    available.add(i);
                }
            }
        }
        if (available.isEmpty()) {
            return heap.size();
        } else {
            return available.remove(RNG.nextInt(available.size()));
        }
    }

    @Override
    public void update(UpdateBlock block) {
        // reset id allocation used by random updates
        available = null;
        super.update(block);
    }

}
