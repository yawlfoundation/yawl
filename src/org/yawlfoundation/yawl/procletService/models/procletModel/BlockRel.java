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

package org.yawlfoundation.yawl.procletService.models.procletModel;

// colset BlockRel = record iBlock:STRING * oBlock:STRING;
public class BlockRel {
	
	private ProcletBlock iBlock = null;
	private ProcletBlock oBlock = null;
	
	public BlockRel(ProcletBlock iBlock, ProcletBlock oBlock) {
		this.iBlock = iBlock;
		this.oBlock = oBlock;
	}
	
	public ProcletBlock getIBlock () {
		return this.iBlock;
	}
	
	public ProcletBlock getOBlock () {
		return this.oBlock;
	}
	
	public String toString () {
		return iBlock.toString() + "->" + oBlock.toString();
	}
}
