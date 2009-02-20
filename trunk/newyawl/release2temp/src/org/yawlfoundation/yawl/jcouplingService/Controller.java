package org.yawlfoundation.yawl.jcouplingService;


import org.jdom.Element;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;

public class Controller {
	
	private WorkItemRecord workItemRecord;
	
	/** dataForEngine - XML Element that enables process the message commmand task within the YAWL Engine */
	private Element dataForEngine;
	
	private State state;
	
	/** Mapping - will be replaced by dtb for persistence reasons in later version */
	private Mapping mapping;

	/** Response of JCoupling, created when message command task is received from JCoupling	*/	
	private String response;
	


	public Controller(WorkItemRecord workItemRecord) {
		this.workItemRecord = workItemRecord;
		this.state = State.started;
		this.mapping.setWorkItemID(workItemRecord.getID());
		
	}

	/** Chechs the state of communication and performs necessary actions
	 *  <b>started</b> - the communication just begin, filter must be registered at JCoupling <p>
	 * 
	 * <b>filter_registration</b> - the request for filter registration was sent to JCoupling, 
	 * waiting for response from JCoupling <p>
	 * 
	 * <b>requestKey_received</b> - response from JCoupling was received, the request key was written in Mapping , 
	 * waiting for message command task from JCoupling <p>
	 * 
	 * <b>message_received</b> - message command task from JCoupling was received, filter removal can start  <p>
	 * 
	 * <b>flter_removed</b> - request for filter removal was sent to JCoupling, interaction can be finished  <p>
	 * 
	 * <b>finished</b> - the interaction between JCoupling and YAWL Engine has ended, the communication can be 
	 * removed from memory 
	 * @throws Exception when is state of workItem not defined/
	 * 	*/	
	public void checkState() throws Exception{
		State status = this.getState();
		switch(status){
			case filter_registration: 
				//do nothing, wait onMessage() from JMS server to receive the requestKey
			break;
				
			case requestKey_received: 
				//do nothing, wait onMessage() from JMS server to receive the message command task	
			break;
			
			case message_received: 
				this.sendMessage("remove filter" + this.getMapping().getRequestKey(), State.finished);
			break;
			
			case finished: 
				// wait until instance will be finished
			break;
			
			default: throw new Exception("Unknown state of communication.");
			
		}
		
	}
	
	
    /**
     * Sends the message through JMS Channel
     * @param message String representation of the message
     * @param state State after the message is sent, sets the state of controller
     */
	public void sendMessage(String message, State state) throws Exception{
		JMSSender sender = new JMSSender();
		String msg_ID = sender.send(message);
		this.mapping.setMsg_ID(msg_ID);
		this.setState(state);
	}
	
		
	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}
	
	
	public WorkItemRecord getWorkItemRecord() {
		return workItemRecord;
	}
	
	public void setWorkItemRecord(WorkItemRecord workItemRecord) {
		this.workItemRecord = workItemRecord;
	}
	
	public Element getDataForEngine() {
		return dataForEngine;
	}
	
	public void setDataForEngine(Element dataForEngine) {
		this.dataForEngine = dataForEngine;
	}
	
	public State getState() {
		return state;
	}
	
	public void setState(State state) {
		this.state = state;
	}
	
	public Mapping getMapping() {
		return mapping;
	}
	
	public void setMapping(Mapping mapping) {
		this.mapping = mapping;
	}
}
