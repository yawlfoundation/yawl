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

package org.yawlfoundation.yawl.procletService.util;

public class EntityID {
	
	private EntityMID emid = null;
	private EntitySID esid = null;
	
	public EntityID (String emid, String esid) {
		this.emid = new EntityMID(emid);
		this.esid = new EntitySID(esid);
	}
	
	public EntityID (EntityMID emid, EntitySID esid) {
		this.emid = emid;
		this.esid = esid;
	}
	
	public EntityMID getEmid() {
		return this.emid;
	}
	
	public EntitySID getEsid() {
		return this.esid;
	}
	
	public String toString() {
		return this.getEmid() + "," + this.getEsid();
	}
}
