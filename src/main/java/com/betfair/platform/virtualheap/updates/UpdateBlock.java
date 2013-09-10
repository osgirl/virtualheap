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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class UpdateBlock {

    public static UpdateBlock merge(UpdateBlock... updateBlocks) {
        int size = 0;
        for (UpdateBlock block : updateBlocks) {
            size += block.list().size();
        }

        List<Update> merged = new ArrayList<Update>(size);
        for (UpdateBlock updateBlock : updateBlocks) {
            merged.addAll(updateBlock.list());
        }
        return new UpdateBlock(merged);
    }

    private final List<? extends Update> block;

    public UpdateBlock(List<? extends Update> block) {
        this.block = Collections.unmodifiableList(block);
    }

    public UpdateBlock() {
        this(Collections.<Update>emptyList());
    }

    public List<? extends Update> list() {
        return block;
    }

    public String toString() {
        String s = "[";
        for (Update delta : block) {
            s += "\t" + delta + "\n";
        }
        return s + "]";
    }

    public void visit(UpdateVisitor visitor) {
        for (Update update : block) {
            update.visit(visitor);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        UpdateBlock that = (UpdateBlock) o;

        return !(block != null ? !block.equals(that.block) : that.block != null);

    }

    private int cachedHash = 0;
    @Override
    public int hashCode() {
        if (cachedHash == 0 && !block.isEmpty()) {
            cachedHash = block.hashCode();
        }
        return cachedHash;
    }
}
