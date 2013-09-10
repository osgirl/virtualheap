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

import com.betfair.platform.virtualheap.MutableHeap;
import com.betfair.platform.virtualheap.NodeType;
import com.betfair.platform.virtualheap.projection.NodeProjector;
import junit.framework.Assert;

import org.junit.Test;

import com.betfair.platform.virtualheap.Heap;

import static com.betfair.platform.virtualheap.projection.ProjectorFactory.objectProjector;


public class SimpleTest {

    NodeProjector<FootballScores> scoresFactory = objectProjector(FootballScores.class);
	
	@Test
	public void simple() {
		Heap src = new MutableHeap("src");

		Heap target = new MutableHeap("target");

		src.addListener(target.asListener(), false);

		src.beginUpdate();

		FootballScores scores = scoresFactory.project(src.ensureRoot(NodeType.OBJECT));
		src.endUpdate();
		
		Assert.assertEquals(src.toString(), target.toString());
		
		src.beginUpdate();
		scores.setAway(0);
		scores.setHome(1);
		GoalDetail firstGoal = scores.goals().addLast();
		firstGoal.setScorer("Rooney");
		firstGoal.setMinutes(34);
		src.endUpdate();
		
		Assert.assertEquals(src.toString(), target.toString());
		
		src.beginUpdate();
		scores.goals().clear();
		src.endUpdate();
		
		Assert.assertEquals(src.toString(), target.toString());
		
		src.beginUpdate();
		scores.clear();
		src.endUpdate();
		
		Assert.assertEquals(src.toString(), target.toString());
		
		
	}

}
