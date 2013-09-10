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

/**
 *
 */
public class SetScalar extends NodeUpdate {

    private final Object value;

    public SetScalar(int id, Object value) {
        super(id);
        this.value = value;
    }

    @Override
    public UpdateType getUpdateType() {
        return UpdateType.SET_SCALAR;
    }

    @Override
    public void visit(UpdateVisitor visitor) {
        visitor.onScalarSet(this);
    }

    public Object getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SetScalar setScalar = (SetScalar) o;

        if (getId() != setScalar.getId()) return false;
        if (value != null ? !value.equals(setScalar.value) : setScalar.value != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = getId();
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "SetScalar(" + getId() + ", " + value + ")";
    }
}
