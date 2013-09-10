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


import com.betfair.platform.virtualheap.HeapListener;
import com.betfair.platform.virtualheap.updates.InstallField;
import com.betfair.platform.virtualheap.updates.InstallIndex;
import com.betfair.platform.virtualheap.updates.InstallRoot;
import com.betfair.platform.virtualheap.updates.RemoveChildren;
import com.betfair.platform.virtualheap.updates.RemoveField;
import com.betfair.platform.virtualheap.updates.RemoveIndex;
import com.betfair.platform.virtualheap.updates.SetScalar;
import com.betfair.platform.virtualheap.updates.Update;
import com.betfair.platform.virtualheap.updates.UpdateBlock;

public class LoggingTreeListener implements HeapListener {

    @Override
    public void applyUpdate(UpdateBlock update) {
        System.out.println("BEGIN");
        for (Update u : update.list()) {
            switch (u.getUpdateType()) {
                case INSTALL_ROOT:
                    InstallRoot installRoot = (InstallRoot) u;
                    System.out.println("\t+ROOT {id: " + installRoot.getId() + ", type: " + installRoot.getUpdateType() + "}");
                    break;
                case INSTALL_FIELD:
                    InstallField installField = (InstallField) u;
                    System.out.println("\t+ {parentId: " + installField.getParentId() + ", id: " + installField.getId() + ", name: \"" + installField.getName() + "\", type: " + installField.getUpdateType() + "}");
                    break;
                case INSTALL_INDEX:
                    InstallIndex installIndex = (InstallIndex) u;
                    System.out.println("\t+ {parentId: " + installIndex.getParentId() + ", id: " + installIndex.getId() + ", index: " + installIndex.getIndex() + ", type: " + installIndex.getUpdateType() + "}");
                    break;
                case SET_SCALAR:
                    SetScalar setScalar = (SetScalar) u;
                    System.out.println("\t# {id: " + setScalar.getId() + ", value: " + setScalar.getValue() + "}");
                    break;
                case REMOVE_FIELD:
                    RemoveField removeField = (RemoveField) u;
                    System.out.println("\t- {parentId: " + removeField.getParentId() + ", id: \"" + removeField.getId() + "\"}");
                    break;
                case REMOVE_INDEX:
                    RemoveIndex removeIndex = (RemoveIndex) u;
                    System.out.println("\t# {parentId: " + removeIndex.getParentId()+ ", id: " + removeIndex.getId() + "}");
                    break;
                case REMOVE_CHILDREN:
                    RemoveChildren removeChildren = (RemoveChildren) u;
                    System.out.println("\t! {id: " + removeChildren.getId() + "}");
                    break;
                case TERMINATE_HEAP:
                    System.out.println("\t! TERMINATE HEAP");
                    break;
                default:
                    throw new IllegalStateException("Unrecognised update type: "+u.getUpdateType());
            }
        }
        System.out.println("END");
    }

}
