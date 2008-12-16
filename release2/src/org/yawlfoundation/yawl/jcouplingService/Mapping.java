package org.yawlfoundation.yawl.jcouplingService;

public class Mapping {
	
	private String workItemID;
	private String requestKey;
	private String msg_ID;
	
	
	
	public Mapping(String workItemID) {
		this.workItemID = workItemID;
	}

	public String getMsg_ID() {
		return msg_ID;
	}

	public void setMsg_ID(String msg_ID) {
		this.msg_ID = msg_ID;
	}

	public String getWorkItemID() {
		return workItemID;
	}
	
	public void setWorkItemID(String workItemID) {
		this.workItemID = workItemID;
	}
	
	public String getRequestKey() {
		return requestKey;
	}
	
	public void setRequestKey(String requestKey) {
		this.requestKey = requestKey;
	}
	
}
