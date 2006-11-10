package com.nexusbpm.scheduler;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Embeddable;

@Embeddable
public class QuartzEventPk implements Serializable{
	private String triggerName;
	private Date actualFireTime;
	public Date getActualFireTime() {
		return actualFireTime;
	}
	public void setActualFireTime(Date actualFireTime) {
		this.actualFireTime = actualFireTime;
	}
	public String getTriggerName() {
		return triggerName;
	}
	public void setTriggerName(String triggerName) {
		this.triggerName = triggerName;
	}

}
