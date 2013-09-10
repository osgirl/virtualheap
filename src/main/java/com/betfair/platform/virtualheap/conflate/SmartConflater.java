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

import com.betfair.platform.virtualheap.updates.*;

import java.util.*;

/**
 * User: mcintyret2
 * Date: 09/08/2012
 */
public class SmartConflater implements Conflater {

    @Override
    public UpdateBlock conflate(UpdateBlock... blocks) {
        UpdateBlock allUpdates = UpdateBlock.merge(blocks);

        ConflatingVisitor conflater = new ConflatingVisitor(allUpdates.list().size());

        return conflater.conflate(allUpdates);
    }

    // This is to avoid exposing the UpdateVisitor interface, which should not be used directly.
    private class ConflatingVisitor implements UpdateVisitor {

        private Map<Integer, Update> installs;
        private Map<Integer, Update> removals;
        private Map<Integer, SetScalar> updates;
        private List<Update> conflated;

        private Counter<Integer> clearCount = new Counter<Integer>();

        private ConflatingVisitor(int size) {
            initCollections(size);
        }

        private UpdateBlock conflate(UpdateBlock unconflated) {
            for (Update update : unconflated.list()) {
                update.visit(this);
            }

            removeSequentialRemoveChildrens();

            return new UpdateBlock(conflated);
        }

        private void removeSequentialRemoveChildrens() {
            RemoveChildren prev = null;
            ListIterator<Update> it = conflated.listIterator();
            while (it.hasNext()) {
                Update u = it.next();
                if (u.getUpdateType() == Update.UpdateType.REMOVE_CHILDREN) {
                    RemoveChildren rc = (RemoveChildren) u;
                    if (prev != null && prev.getId() == rc.getId()) {
                        it.remove();
                    }
                    prev = rc;
                } else {
                    prev = null;
                }
            }
        }

        private void onParentDeallocated(Set<Integer> deallocated, int removedId) {

            // TODO: may be faster to do a removeAll here
            for (Integer id : deallocated) {
                //drop everything we know
                Update update = updates.remove(id);
                if (update != null) {
                    conflated.remove(update);
                }

                Update install = installs.remove(id);
                int cleared = clearCount.remove(id);
                if (install != null) {
                    conflated.remove(install);
                    if (cleared > 0) {
                        removeLastN(new RemoveChildren(id, null), cleared);
                    }
                }

                if (id != removedId) {
                    Update remove = removals.remove(id);
                    if (remove != null) {
                        conflated.remove(remove);
                    }
                }
            }
        }

        @Override
        public void onScalarSet(SetScalar setScalar) {
            Update lastUpdate = updates.put(setScalar.getId(), setScalar);
            if (lastUpdate != null) {
                conflated.remove(lastUpdate);
            }
            conflated.add(setScalar);
        }

        @Override
        public void onFieldInstall(InstallField installField) {
            installs.put(installField.getId(), installField);
            removals.remove(installField.getId());
            conflated.add(installField);
        }

        @Override
        public void onFieldRemove(RemoveField removeField) {
            int id = removeField.getId();
            if (!installs.containsKey(id)) {
                conflated.add(removeField);
                removals.put(id, removeField);
            }
            onParentDeallocated(removeField.getDeallocatedIds(), id);
        }

        @Override
        public void onIndexInstall(InstallIndex installIndex) {
            installs.put(installIndex.getId(), installIndex);
            removals.remove(installIndex.getId());
            conflated.add(installIndex);
        }

        @Override
        public void onIndexRemove(RemoveIndex removeIndex) {
            InstallIndex install = (InstallIndex) installs.get(removeIndex.getId());
            if (install != null) {

                int pos = conflated.indexOf(install);

                //apply any shifts
                int minIndex = install.getIndex();
                ListIterator<Update> updateIt = conflated.listIterator(pos + 1);
                while(updateIt.hasNext()) {
                    Update d = updateIt.next();
                    if (d.getUpdateType() == Update.UpdateType.INSTALL_INDEX) {
                        InstallIndex indexInstall = (InstallIndex) d;
                        if (indexInstall.getParentId() == removeIndex.getParentId()) {
                            if (indexInstall.getIndex() <= minIndex) {
                                minIndex++;
                            } else {
                                InstallIndex newInstall = new InstallIndex(
                                        indexInstall.getParentId(),
                                        indexInstall.getId(),
                                        indexInstall.getIndex() - 1,
                                        indexInstall.getType());
                                updateIt.set(newInstall);
                                installs.put(newInstall.getId(), newInstall);
                            }
                        }
                    } else if (d.getUpdateType() == Update.UpdateType.REMOVE_INDEX) {
                        RemoveIndex removed = (RemoveIndex) d;
                        if (removed.getParentId() == removeIndex.getParentId()) {
                            if (removed.getIndex() < minIndex) {
                                minIndex--;
                            } else {
                                RemoveIndex newRemove = new RemoveIndex(
                                        removed.getParentId(),
                                        removed.getId(),
                                        removed.getIndex() - 1,
                                        removed.getDeallocatedIds());
                                updateIt.set(newRemove);
                                if (removals.containsKey(newRemove.getId())) {
                                    removals.put(newRemove.getId(), newRemove);
                                }
                            }
                        }
                    }
                }
            } else {
                conflated.add(removeIndex);
                removals.put(removeIndex.getId(), removeIndex);
            }
            onParentDeallocated(removeIndex.getDeallocatedIds(), removeIndex.getId());
        }

        @Override
        public void onChildrenRemove(RemoveChildren removeChildren) {
            conflated.add(removeChildren);
            clearCount.add(removeChildren.getId());
            onParentDeallocated(removeChildren.getDeallocatedIds(), -1);
        }

        @Override
        public void onRootInstall(InstallRoot installRoot) {
            conflated.add(installRoot);
        }

        @Override
        public void onHeapTermination(TerminateHeap terminateHeap) {
            conflated.add(terminateHeap);
        }

        private void initCollections(int size) {
            // TODO: set the initial size to some sensible proportion of the number of updates based on empirical evidence
            installs = new HashMap<Integer, Update>(size / 2);
            removals = new HashMap<Integer, Update>(size / 2);
            updates = new HashMap<Integer, SetScalar>(size / 2);
            conflated = new ArrayList<Update>(size / 2);
        }

        private void removeLastN(Update toRemove, int n) {
            int found = 0;
            ListIterator<Update> it = conflated.listIterator(conflated.size());
            while (found < n && it.hasPrevious()) {
                if (toRemove.equals(it.previous())) {
                    it.remove();
                    found++;
                }
            }
        }
    }

    private static class Counter<K> {
        private final Map<K, Integer> map = new HashMap<K, Integer>();

        public void add(K k) {
            Integer count = map.get(k);
            if (count == null) {
                map.put(k, 1);
            } else {
                map.put(k, count + 1);
            }
        }

        public int remove(K k) {
            Integer count = map.remove(k);
            return count == null ? 0 : count;
        }
    }
}
