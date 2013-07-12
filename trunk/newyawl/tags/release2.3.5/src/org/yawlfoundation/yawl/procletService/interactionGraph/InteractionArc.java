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

package org.yawlfoundation.yawl.procletService.interactionGraph;

import org.yawlfoundation.yawl.procletService.util.*;

//colset InteractionArc = record tail:InteractionNode * head:InteractionNode * entityID:EntityID * arcstate:ArcState;
public class InteractionArc {
	
	// colset ArcState = with UNPRODUCED | SENT | CONSUMED | EXECUTED_NONE | EXECUTED_SOURCE | EXECUTED_BOTH | FAILED;
	public enum ArcState {
		UNPRODUCED, SENT, CONSUMED, EXECUTED_NONE, EXECUTED_SOURCE, EXECUTED_BOTH, FAILED;
	}
	
	private InteractionNode tail = null;
	private InteractionNode head = null;
	private EntityID eid = null;
	private ArcState as = null;
	
	public InteractionArc (InteractionNode tail, InteractionNode head, EntityID eid,
			ArcState as) {
		this.tail = tail;
		this.head = head;
		this.eid = eid;
		this.as = as;
	}
	
	public InteractionNode getTail () {
		return this.tail;
	}
	
	public InteractionNode getHead() {
		return this.head;
	}
	
	public EntityID getEntityID() {
		return this.eid;
	}
	
	public ArcState getArcState() {
		return this.as;
	}
	
	public String getArcStateShort() {
		if (this.as.equals(ArcState.CONSUMED)) {
			return "C";
		}
		else if (this.as.equals(ArcState.SENT)) {
			return "S";
		}
		else if (this.as.equals(ArcState.UNPRODUCED)) {
			return "U";
		}
		else if (this.as.equals(ArcState.FAILED)) {
			return "F";
		}
		else if (this.as.equals(ArcState.EXECUTED_BOTH)) {
			return "EB";
		}
		else if (this.as.equals(ArcState.EXECUTED_SOURCE)) {
			return "ES";
		}
		else if (this.as.equals(ArcState.EXECUTED_NONE)) {
			return "EN";
		}
		return "";
	}
	
	public void setTail (InteractionNode tail) {
		this.tail = tail;
	}
	
	public void setHead(InteractionNode head) {
		this.head = head;
	}
	
	public void setEntityID(EntityID eid) {
		this.eid = eid;
	}
	
	public void setArcState(ArcState as) {
		this.as = as;
	}
	
	public static ArcState getArcStateFromString (String as) {
		// UNPRODUCED, SENT, CONSUMED, NR;
		if (as.equals("UNPRODUCED")) {
			return ArcState.UNPRODUCED;
		}
		if (as.equals("SENT")) {
			return ArcState.SENT;
		}
		if (as.equals("CONSUMED")) {
			return ArcState.CONSUMED;
		}
		if (as.equals("EXECUTED_NONE")) {
			return ArcState.EXECUTED_NONE;
		}
		if (as.equals("EXECUTED_SOURCE")) {
			return ArcState.EXECUTED_SOURCE;
		}
		if (as.equals("EXECUTED_BOTH")) {
			return ArcState.EXECUTED_BOTH;
		}
		if (as.equals("FAILED")) {
			return ArcState.FAILED;
		}
		return null;
	}
	
	public String toString() {
		return "ARC:tail:" + this.tail + ",head:" + this.head + ",eid:" + this.eid + 
		"as:" + this.as;
	}
}
