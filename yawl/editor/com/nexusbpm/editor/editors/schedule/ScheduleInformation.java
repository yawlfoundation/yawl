package com.nexusbpm.editor.editors.schedule;

import java.util.Date;

public class ScheduleInformation {

	private String uri;
	private String cronExpression;
	private Date startDate;
	private Date endDate;
	
	public ScheduleInformation(String uri, String cronExpression, Date startDate, Date endDate) {
		super();
		this.uri = uri;
		this.cronExpression = cronExpression;
		this.startDate = startDate;
		this.endDate = endDate;
	}
	public String getCronExpression() {
		return cronExpression;
	}
	public void setCronExpression(String cronExpression) {
		this.cronExpression = cronExpression;
	}
	public Date getEndDate() {
		return endDate;
	}
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	public Date getStartDate() {
		return startDate;
	}
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
	@Override
	public String toString() {
		return "run " + uri + "@cron {" + cronExpression + "} from " + startDate + " through " + endDate; 
	}
}
