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

package org.yawlfoundation.yawl.procletService.state;

import org.yawlfoundation.yawl.procletService.models.procletModel.ProcletPort;
import org.yawlfoundation.yawl.procletService.util.EntityID;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;


// colset Performative = record time_:INT * channel:STRING * sender:ProcletID * 
// receiver:ProcletID * action_:STRING * content:Content * scope:STRING * 
// direction:Direction * 
// eids:EntityIDs;

public class Performative {
	
	private Timestamp time = null;
	private String channel = "";
	private String sender = "";
	private List<String> receivers = new ArrayList<String>();
	private String action = "";
	private String content = null;
	private String scope = "";
	private ProcletPort.Direction direction = ProcletPort.Direction.IN;
	private List<EntityID> eids = new ArrayList<EntityID> ();
	
	public Performative (String channel, String sender, List<String> receivers, String action, 
			String content, String scope,
			ProcletPort.Direction direction, List<EntityID>  eids) {
		this.time = new Timestamp(System.currentTimeMillis());
		this.channel = channel;
		this.sender = sender;
		this.receivers = receivers;
		this.action = action;
		this.content = content;
		this.scope = scope;
		this.direction = direction;
		this.eids = eids;
	}
	
	public Performative (Timestamp time, String channel, String sender, List<String> receivers, 
			String action, String content, String scope,  
			ProcletPort.Direction direction, List<EntityID>  eids) {
		this(channel,sender,receivers,action, content,scope,direction,eids);
		this.time = time;
	}
	
	public Timestamp getTime() {
		return this.time;
	}
	
	public void setTime (Timestamp time) {
		this.time = time;
	}
	
	public String getChannel () {
		return this.channel;
	}
	
	public void setSender (String sender) {
		this.sender = sender;
	}
	
	public String getSender() {
		return this.sender;
	}
	
	public void setReceiver (List<String> receivers) {
		this.receivers = receivers;
	}
	
	public List<String> getReceivers () {
		return this.receivers;
	}
	
	public void setAction (String action) {
		this.action = action;
	}
	
	public String getAction () {
		return this.action;
	}
	
	public void setContent (String cont) {
		this.content = cont;
	}
	
	public String getContent () {
		return this.content;
	}
	
	public void setScope (String scope) {
		this.scope = scope;
	}
	
	public String getScope () {
		return this.scope;
	}
	
	public ProcletPort.Direction getDirection  () {
		return this.direction;
	}
	
	public void setDirection (ProcletPort.Direction dir) {
		this.direction = dir;
	}
	
	public void setEntityIDs (List<EntityID> eids) {
		this.eids = eids;
	}
	
	public List<EntityID> getEntityIDs () {
		return this.eids;
	}
	
	public boolean equalContent (Object o) {
		if (o instanceof Performative) {
			Performative other = (Performative) o;
			if (this.getTime().equals(other.getTime()) &&
					this.getChannel().equals(other.getChannel()) && 
					this.getSender().equals(other.getSender()) && 
					this.getAction().equals(other.getAction()) &&
					this.getContent().equals(other.getContent()) && 
					this.getScope().equals(other.getScope()) &&
					this.getDirection().toString().equals(other.getDirection().toString()) && 
					this.getEntityIDs().size() == other.getEntityIDs().size()) {
					// check eids
				boolean checkOveral = true;
				for (EntityID eid : this.getEntityIDs()) {
					boolean check = false;
					for (EntityID eidOther : other.getEntityIDs()) {
						if (eid.getEmid().getValue().equals(eidOther.getEmid().getValue()) &&
								eid.getEsid().getValue().equals(eidOther.getEsid().getValue())) {
							// found
							check = true;
							break;
						}
					}
					if (!check){checkOveral = false; break;}
				}
				for (String receiver : this.getReceivers()) {
					boolean check = false;
					for (String receiverOther : other.getReceivers()) {
						if (receiver.equals(receiverOther)) {
							// found
							check = true;
							break;
						}
					}
					if (!check){checkOveral = false; break;}
				}
				if (checkOveral) {
					return true;
				}
			}
		}
		return false;
	}
	
	public String toString() {
		return this.getChannel() + "," + this.getSender() + "," + this.getReceivers() + "," + 
		this.getDirection() + "," + this.getEntityIDs() + "," + this.getContent();
	}
}

