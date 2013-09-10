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

import com.betfair.platform.virtualheap.NodeType;

/**
 *
 */
public class InstallIndex extends IndexedUpdate {

    private final NodeType type;

    public InstallIndex(int parentId, int id, int index, NodeType type) {
        super(parentId, id, index);
        this.type = type;
    }

    @Override
    public UpdateType getUpdateType() {
        return UpdateType.INSTALL_INDEX;
    }


    @Override
    public void visit(UpdateVisitor visitor) {
        visitor.onIndexInstall(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        InstallIndex that = (InstallIndex) o;

        if (getId() != that.getId()) return false;
        if (getIndex() != that.getIndex()) return false;
        if (getParentId() != that.getParentId()) return false;
        if (type != that.type) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = getParentId();
        result = 31 * result + getId();
        result = 31 * result + getIndex();
        result = 31 * result + type.hashCode();
        return result;
    }

    public NodeType getType() {
        return type;
    }

    @Override
    public String toString() {
        return "InstallIndex(" + getParentId() + ", " + getId() + ", " + getIndex()+ ", " + type + ")";
    }
}
