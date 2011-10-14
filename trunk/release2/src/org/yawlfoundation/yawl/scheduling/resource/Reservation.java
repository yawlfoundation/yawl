/*
 * Copyright (c) 2004-2011 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.scheduling.resource;

import org.yawlfoundation.yawl.resourcing.resource.AbstractResource;


/**
 * mock class
 * 
 * @author tbe
 * @version $Id: Reservation.java 21312 2010-08-17 13:58:51Z tbe $
 */
public class Reservation {
	public String caseId;
	
	/**
	 * activityName of resource allocation task
	 */
	public String activityName;

	/**
	 * can be: resource, role or type
	 */
	public AbstractResource resource;
	
	public Period period;
	
	/**
	 *
	 */
	public String planningStatus;
	
	/**
	 * workload of resource for this task
	 * e.g. if circulating nurse can work on 5 tasks, she has a minimum workload of 0.2 = 20% per task
	 */
	public double workload;
	
	public Reservation(String caseId, String activityName, AbstractResource resource, Period period,
			String planningStatus, int workload) {
		super();
		this.caseId = caseId;
		this.activityName = activityName;
		this.resource = resource;
		this.period = period;
		this.planningStatus = planningStatus;
		this.workload = workload;
	}
	
	public String toString() {
		return "["+this.caseId+", "+this.activityName+", "+this.resource.getID()+", "+this.period+", "+this.planningStatus+", "+this.workload+"]";
	}
}
