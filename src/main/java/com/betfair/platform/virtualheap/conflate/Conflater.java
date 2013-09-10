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
package com.betfair.platform.virtualheap.conflate;

import com.betfair.platform.virtualheap.updates.UpdateBlock;

/**
 * User: mcintyret2
 * Date: 16/07/2012
 */
public interface Conflater {

    /**
     * <p>Returns an {@link UpdateBlock} containing the updates in the provided blocks conflated to remove any
     * unnecessary updates.</p>
     *
     * <p>Examples of conflation:</p>
     *
     * <ul>
     * <li>[SetScalar(1, "hello"), SetScalar(1, "there"), SetScalar(1, "world")] -> [SetScalar(1, "world")]</li>
     * <li>[InstallField(0, 1, "myScalar", SCALAR), SetScalar(1, "myValue"), RemoveField(0, 1)] -> []</li>
     * </ul>
     * @param updateBlocks the blocks to conflate into a single UpdateBlock
     * @return the conflated UpdateBlock
     */
    UpdateBlock conflate(UpdateBlock... updateBlocks);

}
