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
public class InstallRoot extends NodeUpdate {

    private final NodeType type;

    public InstallRoot(int id, NodeType type) {
        super(id);
        this.type = type;
    }

    @Override
    public UpdateType getUpdateType() {
        return UpdateType.INSTALL_ROOT;
    }

    @Override
    public void visit(UpdateVisitor visitor) {
        visitor.onRootInstall(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        InstallRoot that = (InstallRoot) o;

        if (getId() != that.getId()) return false;
        if (type != that.type) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = getId();
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }

    public NodeType getType() {
        return type;
    }

    @Override
    public String toString() {
        return "InstallRoot(" + getId() + ", " + type + ")";
    }
}
