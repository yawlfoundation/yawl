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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;

@Entity
@IdClass(QuartzEventPk.class)
public class QuartzEvent implements Serializable{

	public enum State{COMPLETED, ERRORED, FIRED, MISFIRED, UNKNOWN};
	
	@Id private String triggerName;
	@Id private Date scheduledFireTime; 
	@Column private Date actualFireTime;
	@Column private String caseId;
	@Column private String fireStatus;
	@Column(length=4096) private String message;

	public QuartzEvent() {}
	
	public QuartzEvent(String triggerName, Date scheduledFireTime, Date actualFireTime, String caseId, String fireStatus) {
		super();
		this.triggerName = triggerName;
		this.actualFireTime = actualFireTime;
		this.scheduledFireTime = scheduledFireTime;
		this.caseId = caseId;
		this.fireStatus = fireStatus;
	}

	public QuartzEvent(String triggerName, Date scheduledFireTime, Date actualFireTime, String caseId, String fireStatus, String message) {
		super();
		this.triggerName = triggerName;
		this.actualFireTime = actualFireTime;
		this.scheduledFireTime = scheduledFireTime;
		this.caseId = caseId;
		this.fireStatus = fireStatus;
		this.message = message;
	}
	
	public Date getActualFireTime() {
		return actualFireTime;
	}
	public void setActualFireTime(Date actualFireTime) {
		this.actualFireTime = actualFireTime;
	}
	public String getCaseId() {
		return caseId;
	}
	public void setCaseId(String caseId) {
		this.caseId = caseId;
	}
	public String getFireStatus() {
		return fireStatus;
	}
	public void setFireStatus(String fireStatus) {
		this.fireStatus = fireStatus;
	}
	public Date getScheduledFireTime() {
		return scheduledFireTime;
	}
	public void setScheduledFireTime(Date scheduledFireTime) {
		this.scheduledFireTime = scheduledFireTime;
	}
	public String getTriggerName() {
		return triggerName;
	}
	public void setTriggerName(String triggerName) {
		this.triggerName = triggerName;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
}
