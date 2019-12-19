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

package org.yawlfoundation.yawl.procletService.interactionGraph;

//colset ClassProcBlockNode = record classID:ClassID * procletID:ProcletID * blockID:BlockID;
public class InteractionNode {
	
	private String classID = "";
	private String procletID = "";
	private String blockID = "";
	
	public InteractionNode(String classID, String procletID, String blockID) {
		this.classID = classID;
		this.procletID = procletID;
		this.blockID = blockID;
	}
	
	public String getClassID () {
		return this.classID;
	}
	
	public String getProcletID () {
		return this.procletID;
	}
	
	public String getBlockID () {
		return this.blockID;
	}
	
	public void setClassID (String classID) {
		this.classID = classID;
	}
	
	public void setProcletID (String procletID) {
		this.procletID = procletID;
	}
	
	public void setBlockID (String blockID) {
		this.blockID = blockID;
	}
	
	public String toString() {
		return this.classID + "," + this.procletID + "," + this.blockID;
	}

}
