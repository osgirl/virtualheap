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


import com.betfair.platform.virtualheap.HListComplex;
import com.betfair.platform.virtualheap.HListScalar;
import com.betfair.platform.virtualheap.ObjectNode;
import com.betfair.platform.virtualheap.projection.ComplexListProjector;
import com.betfair.platform.virtualheap.projection.ObjectProjector;
import com.betfair.platform.virtualheap.projection.ScalarListProjector;

import static com.betfair.platform.virtualheap.projection.ProjectorFactory.*;

/**
 * This is class will be generated from
 * 
 * 	<dataType name="FootballScores">
 * 		<parameter name="home" type="i32"/>
 *   	<parameter name="away" type="i32"/>
 *   	<parameter name="goals" type="list(GoalDetail)"/>    
 *	</dataType>
 * 
 */
public class FootballScores {

    private final ObjectNode node;

    private static final ComplexListProjector<GoalDetail> goalsProjector =
            listProjector(objectProjector(GoalDetail.class));

    private static final ObjectProjector<GoalDetail> goalProjector = objectProjector(GoalDetail.class);
    private static final ScalarListProjector<String> scalarListProjector =
            listProjector(stringProjector);

    public FootballScores(ObjectNode node) {
        this.node = node;
    }

    public Integer getHome() {
        return node.ensureField("home", intProjector.getType()).project(intProjector).get();
    }
    public void setHome(Integer i) {
        node.ensureField("home", intProjector.getType()).project(intProjector).set(i);
    }

    public Integer getAwayClient() {
        return node.getField("away") != null ? node.getField("away").project(intProjector).get() : null;
    }
    public Integer getAway() {
        return node.ensureField("away", intProjector.getType()).project(intProjector).get();
    }
    public void setAway(Integer i) {
        node.ensureField("away", intProjector.getType()).project(intProjector).set(i);
    }

    public HListComplex<GoalDetail> goals() {
        return node.ensureField("goals", goalsProjector.getType()).project(goalsProjector);
    }

    public GoalDetail getGoal() {
        return node.ensureField("goal", goalProjector.getType()).project(goalProjector);
    }

    public HListScalar<String> getListOfStrings() {
        return node.ensureField("strings2", scalarListProjector.getType()).project(scalarListProjector);
    }

    public void clear() {
        node.clear();
    }
}
