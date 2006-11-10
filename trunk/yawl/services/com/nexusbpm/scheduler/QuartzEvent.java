package com.nexusbpm.scheduler;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;

@Entity
@IdClass(QuartzEventPk.class)
public class QuartzEvent {

	@Id private String triggerName;
	@Id private Date actualFireTime;
	@Column private Date scheduledFireTime; 
	@Column private String caseId;
	@Column private String fireStatus;

	public QuartzEvent() {}
	
	public QuartzEvent(String triggerName, Date scheduledFireTime, Date actualFireTime, String caseId, String fireStatus) {
		super();
		this.triggerName = triggerName;
		this.actualFireTime = actualFireTime;
		this.scheduledFireTime = scheduledFireTime;
		this.caseId = caseId;
		this.fireStatus = fireStatus;
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
	
}
