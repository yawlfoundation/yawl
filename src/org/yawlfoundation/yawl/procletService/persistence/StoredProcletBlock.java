/*
 * Copyright (c) 2004-2012 The YAWL Foundation. All rights reserved.
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

import org.yawlfoundation.yawl.procletService.models.procletModel.ProcletBlock;

/**
 * @author Michael Adams
 * @date 2/02/12
 */
public class StoredProcletBlock {
    
    private long pkey;
    private String classID;
    private String blockID;
    private String blockType;
    private boolean create = false;
    private int timeOut = 0;

    public StoredProcletBlock() { }

    public StoredProcletBlock(String classID, String blockID, String blockType,
                              boolean create, int timeOut) {
        this.classID = classID;
        this.blockID = blockID;
        this.blockType = blockType;
        this.create = create;
        this.timeOut = timeOut;
    }
    
    public ProcletBlock newProcletBlock() {
        return new ProcletBlock(blockID, ProcletBlock.getBlockTypeFromString(blockType),
                create, timeOut);
    }

    public String getClassID() {
        return classID;
    }

    public void setClassID(String classID) {
        this.classID = classID;
    }

    public String getBlockID() {
        return blockID;
    }

    public void setBlockID(String blockID) {
        this.blockID = blockID;
    }

    public String getBlockType() {
        return blockType;
    }

    public void setBlockType(String blockType) {
        this.blockType = blockType;
    }

    public boolean isCreate() {
        return create;
    }

    public void setCreate(boolean create) {
        this.create = create;
    }

    public int getTimeOut() {
        return timeOut;
    }

    public void setTimeOut(int timeOut) {
        this.timeOut = timeOut;
    }

    public long getPkey() {
        return pkey;
    }

    public void setPkey(long pkey) {
        this.pkey = pkey;
    }
}
