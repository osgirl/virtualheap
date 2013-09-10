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
package com.betfair.platform.virtualheap.updates;

import java.util.Set;

/**
 *
 */
public class RemoveChildren extends NodeUpdate {

    private transient Set<Integer> deallocatedIds;

    public RemoveChildren(int id, Set<Integer> deallocatedIds) {
        super(id);
        this.deallocatedIds = deallocatedIds;
    }

    @Override
    public UpdateType getUpdateType() {
        return UpdateType.REMOVE_CHILDREN;
    }

    @Override
    public void visit(UpdateVisitor visitor) {
        visitor.onChildrenRemove(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RemoveChildren that = (RemoveChildren) o;

        if (getId() != that.getId()) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return getId();
    }

    public Set<Integer> getDeallocatedIds() {
        return deallocatedIds;
    }

    @Override
    public String toString() {
        return "RemoveChildren(" + getId() + ")";
    }
}
