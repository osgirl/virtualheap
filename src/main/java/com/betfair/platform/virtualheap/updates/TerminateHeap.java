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

public class TerminateHeap implements Update {

    @Override
    public UpdateType getUpdateType() {
        return UpdateType.TERMINATE_HEAP;
    }

    @Override
    public void visit(UpdateVisitor visitor) {
        visitor.onHeapTermination(this);
    }

    @Override
    public String toString() {
        return "TerminateHeap()";
    }

    @Override
    public boolean equals(Object object) {
        return object != null && object instanceof TerminateHeap;
    }

    @Override
    public int hashCode() {
        return getUpdateType().hashCode();
    }
}
