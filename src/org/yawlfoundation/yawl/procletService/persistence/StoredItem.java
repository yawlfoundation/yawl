/*
 * Copyright (c) 2004-2020 The YAWL Foundation. All rights reserved.
 * The YAWL Foundation is a collaboration of individuals and
 * organisations who are committed to improving workflow technology.
 *
 * This file is part of YAWL. YAWL is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation.
 *
 * YAWL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with YAWL. If not, see <http://www.gnu.org/licenses/>.
 */

package org.yawlfoundation.yawl.procletService.persistence;

import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.procletService.interactionGraph.InteractionNode;
import org.yawlfoundation.yawl.procletService.util.EntityMID;

/**
 * @author Michael Adams
 * @date 1/02/12
 */
public class StoredItem {

    private long pkey;
    private String classID;
    private String procletID;
    private String blockID;
    private String emid;
    private boolean selected;
    private Item itemType;

    public StoredItem() { }
    
    public StoredItem(String classID, String procletID, String blockID, Item itemType) {
        this.classID = classID;
        this.procletID = procletID;
        this.blockID = blockID;
        this.itemType = itemType;
    }
    
    public StoredItem(String classID, String procletID, String blockID, String emid,
                      Item itemType) {
        this.classID = classID;
        this.procletID = procletID;
        this.blockID = blockID;
        this.emid = emid;
        this.itemType = itemType;
    }
    
    public StoredItem(WorkItemRecord wir, Item itemType) {
        this(wir.getSpecURI(), wir.getCaseID(), wir.getTaskID(), itemType);
    }
    
    public StoredItem(WorkItemRecord wir, String emid, Item itemType) {
        this(wir.getSpecURI(), wir.getCaseID(), wir.getTaskID(), emid, itemType);
    }
    
    
    public InteractionNode newInteractionNode() {
        return new InteractionNode(classID, procletID, blockID);
    }


    public EntityMID newEntityMID() {
        return new EntityMID(emid);
    }

    public String getClassID() {
        return classID;
    }

    public void setClassID(String id) {
        classID = id;
    }

    public String getProcletID() {
        return procletID;
    }

    public void setProcletID(String id) {
        procletID = id;
    }

    public String getBlockID() {
        return blockID;
    }

    public void setBlockID(String id) {
        blockID = id;
    }

    public String getEmid() {
        return emid;
    }

    public void setEmid(String emid) {
        this.emid = emid;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public Item getItemType() {
        return itemType;
    }

    public void setItemType(Item itemType) {
        this.itemType = itemType;
    }

    public long getPkey() {
        return pkey;
    }

    public void setPkey(long pkey) {
        this.pkey = pkey;
    }
}
