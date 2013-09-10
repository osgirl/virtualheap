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
package com.betfair.platform.virtualheap.projection;

import com.betfair.platform.virtualheap.HListScalar;
import com.betfair.platform.virtualheap.ListNode;

public class ScalarListProjection<T> extends AbstractListProjection<T> implements HListScalar<T> {


	private ScalarProjector<T> valueProjector;

    public ScalarListProjection(ListNode node, ScalarProjector<T> valueProjector) {
		super(node);
        this.valueProjector = valueProjector;
	}

	public T get(int index) {
        if (index >= 0 && index < size()) {
            return node.getIndexAt(index).project(valueProjector).get();
        } else {
            throw new IndexOutOfBoundsException();
        }
    }

    @Override
    public void addFirst(T value) {
        insertAt(0, value);
    }

    @Override
    public void addLast(T value) {
        insertAt(size(), value);
    }

    @Override
    public void insertAt(int index, T value) {
        node.insertAt(index, valueProjector.getType()).project(valueProjector).set(value);
    }

    @Override
    public void push(T value) {
        addFirst(value);
    }
}
