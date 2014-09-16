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

package org.yawlfoundation.yawl.scheduling;


public class Mapping {
	private String workItemId;
	private Integer requestKey;
	private String workItemStatus;
	private boolean isLocked;
	
	public static final String WORKITEM_STATUS_PARENT = "parent";
    public static final String WORKITEM_STATUS_CHECKOUT = "checkout";
    public static final String WORKITEM_STATUS_CACHED = "cached";
    public static final String WORKITEM_STATUS_PROCESSING = "processing";


    private Mapping() { }                         // for hibernate

	public Mapping(String workItemId, Integer requestKey, String workItemStatus) {
		this.workItemId = workItemId;
		this.requestKey = requestKey;
		this.workItemStatus = workItemStatus;
	}

	/**
	 * @return the workItemId
	 */
	public String getWorkItemId() {
		return workItemId;
	}

	/**
	 * @param workItemId the workItemId to set
	 */
	public void setWorkItemId(String workItemId) {
		this.workItemId = workItemId;
	}

	/**
	 * @return the requestKey
	 */
	public Integer getRequestKey() {
		return requestKey;
	}

	/**
	 * @param requestKey the requestKey to set
	 */
	public void setRequestKey(Integer requestKey) {
		this.requestKey = requestKey;
	}

	/**
	 * @return the isLocked
	 */
	public boolean isLocked() {
		return isLocked;
	}

	/**
	 * @param isLocked the isLocked to set
	 */
	public void setLocked(boolean isLocked) {
		this.isLocked = isLocked;
	}

	public String getWorkItemStatus() {
		return workItemStatus;
	}

	public void setWorkItemStatus(String workItemStatus) {
		this.workItemStatus = workItemStatus;
	}

	public String toString() {
        StringBuilder sb = new StringBuilder(128);        
		sb.append("{workItemId=").append(workItemId).append(Constants.CSV_DELIMITER)
          .append("requestKey=").append(requestKey).append(Constants.CSV_DELIMITER)
          .append("workItemStatus=").append(workItemStatus).append(Constants.CSV_DELIMITER)
          .append("isLocked=").append(isLocked).append("}");
        return sb.toString();
	}

}
