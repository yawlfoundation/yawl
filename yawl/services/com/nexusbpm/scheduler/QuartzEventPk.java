/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

package com.nexusbpm.scheduler;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Embeddable;

@Embeddable
public class QuartzEventPk implements Serializable{
	private String triggerName;
	private Date scheduledFireTime;
	public Date getScheduledFireTime() {
		return scheduledFireTime;
	}
	public void setScheduledFireTime(Date actualFireTime) {
		this.scheduledFireTime = actualFireTime;
	}
	public String getTriggerName() {
		return triggerName;
	}
	public void setTriggerName(String triggerName) {
		this.triggerName = triggerName;
	}

}
