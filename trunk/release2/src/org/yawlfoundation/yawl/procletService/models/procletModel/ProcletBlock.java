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

package org.yawlfoundation.yawl.procletService.models.procletModel;

// colset Block = record blockID:BlockID * bt:BlockType * create:BOOL * ports:Ports * timeOut:INT;


public class ProcletBlock {
	
	// colset BlockType = with CP | PI | FO;	
	public enum BlockType {
	    CP, PI, FO;
	}

	private String blockID = "";
	private BlockType blockType = BlockType.FO;
	private boolean create = false;
	private int timeOut = 0;
	
	public ProcletBlock (String blockID, BlockType blockType, boolean create, int timeOut) {
		this.blockID = blockID;
		this.blockType = blockType;
		this.create = create;
		this.timeOut = timeOut;
	}
	
	public String getBlockID () {
		return this.blockID;
	}
	
	public void setBlockID (String blockID){
		this.blockID = blockID;
	}
	
	public BlockType getBlockType () {
		return this.blockType;
	}
	
	public void setBlockType (BlockType bt) {
		this.blockType = bt;
	}
	
	public boolean isCreate () {
		return this.create;
	}
	
	public void setCreate(boolean c) {
		this.create = c;
	}
	
	public int getTimeOut() {
		return this.timeOut;
	}
	
	public void setTimeOut(int timeOut) {
		this.timeOut = timeOut;
	}
	
	public static BlockType getBlockTypeFromString(String blockType) {
		if (blockType.equals("CP")) {
			return BlockType.CP;			
		}
		if (blockType.equals("PI")) {
			return BlockType.PI;			
		}
		if (blockType.equals("FO")) {
			return BlockType.FO;			
		}
		return null;
	}
	
	public String toString () {
		return this.blockID;
	}

}
