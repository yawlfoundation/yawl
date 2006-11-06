/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

package com.nexusbpm.editor.editors.schedule;

import java.util.Date;

public class ScheduleInformation {

	private String uri;
	private String cronExpression;
	private Date startDate;
	private Date endDate;
	private String scheduleName;
	
	public String getScheduleName() {
		return scheduleName;
	}
	public void setScheduleName(String scheduleName) {
		this.scheduleName = scheduleName;
	}
	public ScheduleInformation(String scheduleName, String uri, String cronExpression, Date startDate, Date endDate) {
		super();
		this.scheduleName = scheduleName;
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
		return "run '" + scheduleName + "' {" + uri + " (" + startDate + "-" + endDate + ")}" + "@cron {" + cronExpression + "}" ; 
	}
}
