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
package com.betfair.platform.virtualheap.scorestest;


import com.betfair.platform.virtualheap.MapNode;
import com.betfair.platform.virtualheap.ObjectNode;
import com.betfair.platform.virtualheap.projection.ScalarProjection;

import static com.betfair.platform.virtualheap.projection.ProjectorFactory.intProjector;
import static com.betfair.platform.virtualheap.projection.ProjectorFactory.stringProjector;

/**
 * This is class will be generated from
 * 
 * 	<dataType name="GoalDetail">
 *   	<parameter name="scorer" type="string"/>
 *   	<parameter name="minutes" type="i32"/>
 *	</dataType>
 *
 */
public class GoalDetail {
    private final ObjectNode node;

    public GoalDetail(ObjectNode node) {
        this.node = node;
    }

    public void setScorer(String s) {
        node.ensureField("scorer", stringProjector.getType()).project(stringProjector).set(s);
    }
    public String getScorer() {
        return node.ensureField("scorer", stringProjector.getType()).project(stringProjector).get();
    }

    public void setMinutes(Integer i) {
        node.ensureField("minutes", intProjector.getType()).project(intProjector).set(i);
    }
    public Integer getMinutes() {
        return node.ensureField("minutes", intProjector.getType()).project(intProjector).get();
    }
}
