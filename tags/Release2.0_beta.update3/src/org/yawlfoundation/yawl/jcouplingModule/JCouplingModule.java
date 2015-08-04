package org.yawlfoundation.yawl.jcouplingModule;

import java.util.ArrayList;
import java.util.List;

import org.jdom.Element;

import org.yawlfoundation.yawl.elements.data.YParameter;
import org.yawlfoundation.yawl.engine.interfce.interfaceB.InterfaceBWebsideController;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;

/** Bridge between JCoupling and YAWL Engine */

public class JCouplingModule extends InterfaceBWebsideController implements Runnable{

	
	
/**	
 * not yet finished interactions, these interactions are still running inside the YAWL Engine
*/
	public static List _outStandingInteractions = new ArrayList(); //	make private after testing

/**	
 * 	reports, if the interaction is still running (interaction hasn’t finished yet), or if it has already 
 * 	finished. Only finished interactions are removed from _outstandingInteractions List 
*/
    private static boolean _running = false;
    
/**  
 *	identification of the session connection with YAWL Engine. This session is regularly checked, if is active. 
 *	If not, new session is created. The session check is also necessary for the first connection with the YAWL 
 *	Engine. 
 */
	private static String _sessionHandle = null;
	 
/**	
 *	input parameter for YAWL Editor - outgoing message text
 */
	private static final String OUTGOING_MESSAGE = "outgoing_message";
	
/**	
 *	input parameter for YAWL Editor - reply message text
 */
	private static final String REPLY_MESSAGE = "reply_message";

	
/**	
 * <br>1) Check the connection, i fit is OK(_sessionHandle)
 * <br>2) Parse XML from YAWL Engine, trying to find OUTGOING_MESSAGE Element, save this element as String and send it through JMS Channel to JCoupling
 * <br>3) Create new Interaction, that will be waiting for the answer from JCoupling
 * <br>4) Run new thread with this Interaction
*/
	@Override
	public void handleEnabledWorkItemEvent(WorkItemRecord enabledWorkItem) {
        try {
        	
/** login */        	
            if (!checkConnection(_sessionHandle)) {
                _sessionHandle = connect(DEFAULT_ENGINE_USERNAME, DEFAULT_ENGINE_PASSWORD);
            }
            if (successful(_sessionHandle)) {

/** checking children tasks of workItems -> from enable to fired status */            	
                	List children = checkOutAllInstancesOfThisTask(enabledWorkItem, _sessionHandle);
                      
                    for (int i = 0; i < children.size(); i++) {
                        WorkItemRecord itemRecord = (WorkItemRecord) children.get(i);
                        //System.out.println("WebServiceController::processEnabledAnnouncement() itemRecord = " + itemRecord);
                        
/** prepares the replying element, used later in interaction */       	              
                        Element dataForEngine = prepareReplyRootElement(enabledWorkItem, _sessionHandle);

/** get msg input parameters */                        
                        Element inputData = itemRecord.getWorkItemData();
                        String message = inputData.getChildText(OUTGOING_MESSAGE);

                        System.out.println("message: " + message);	
                        
                        if (message!=null){
                        	String sendID = this.performSend(message);
                        	
                            Interaction inter = new Interaction();
                            inter._sendID = sendID;
                            inter._workItemRecord = itemRecord;
                            inter._dataForEngine = dataForEngine;
                            inter._finished = false;
                            _outStandingInteractions.add(inter);
                        }                    
                    }
                 
            }
        } catch (Exception e) {
          	e.printStackTrace();
        }
        if (!_running) {
            Thread replyThread = new Thread(this, "ReplyThread");
            replyThread.start();
        }
	}

/** 
 * describes the input parameters for YAWL Engine
 */ 
	public YParameter[] describeRequiredParams() {
        YParameter[] params = new YParameter[3];
        YParameter param;

        param = new YParameter(null, YParameter._INPUT_PARAM_TYPE);
        param.setDataTypeAndName(XSD_STRINGTYPE, OUTGOING_MESSAGE, XSD_NAMESPACE);
        param.setDocumentation("This is the outgoing message content");
        params[0] = param;

        param = new YParameter(null, YParameter._INPUT_PARAM_TYPE);
        param.setDataTypeAndName(XSD_STRINGTYPE, REPLY_MESSAGE, XSD_NAMESPACE);
        param.setDocumentation("This is the reply message content.");
        params[1] = param;

        return params;
    }
    
/**
 * sends the message through the openJMS to the JCoupling
 */	
    public String performSend(String message) {
    	String id = message.substring(0, 3); // change to something unique
    	
//    	send via openJMS
//		create new channel
//    	create openJMS Listener, send via channel - queue
    	
			System.out.println("Message ID " + id + " with content:<" + message + "> was sent");

		return id;
	}
    
     
/**
* Removes (if necessary) the work items from the YAWL Engine
*/	
    @Override
    public void handleCancelledWorkItemEvent(WorkItemRecord workItemRecord) {
        synchronized (this) {
            System.out.println("PreCancel::_outStandingInteractions = " + _outStandingInteractions);
            for (int i = 0; i < _outStandingInteractions.size(); i++) {
                Interaction _interaction = (Interaction)
                        _outStandingInteractions.get(i);
                if (_interaction._workItemRecord.getID().equals(workItemRecord.getID())) {
                    _outStandingInteractions.remove(i);
                }
            }
        }
        System.out.println("\tPostCancel::_outStandingInteractions = " + _outStandingInteractions);
    }


	@Override
	public void run() {
		_running = true;

//		ToDo when remove the interaction itself from the engine? -  ???after some time left, or when??? 
		while (_outStandingInteractions.size() > 0) {
			 
		synchronized(this){
			for(int i=0; i< _outStandingInteractions.size(); i++){
				Interaction inter = (Interaction) _outStandingInteractions.get(i);
			  try {
				if(this.getReplies(inter._sendID).size()>0){
					List<Reply> replies = this.getReplies(inter._sendID);
					for(int j=0; j<replies.size(); j++){
//	getting data from response messages and sending them to the engine			
							inter._replyMessage = (Reply) replies.get(j);
							Element replyMsg = new Element(REPLY_MESSAGE);
							replyMsg.setText(inter._replyMessage._messageText);
							
							System.out.println("MsgID <" + inter._sendID + "> with content:<" + replyMsg.getText() + "> was received.");
							
//			uncomment after testing
//							inter._dataForEngine.addContent(replyMsg);
							
// checkin inside/outside the for cycle		
							
//			uncomment after testing	
//								checkInWorkItem(inter._workItemRecord.getID(),
//										inter._workItemRecord.getWorkItemData(),
//										inter._dataForEngine,
//								        _sessionHandle);
					}
					   inter._finished = true;
				}else{
               	 System.out.println("interaction <" + inter._sendID + "> has no replies.");
                }
				
			  }catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			  }

			}
			
			for (int i = 0; i < _outStandingInteractions.size(); i++) {
                 Interaction inter = (Interaction) _outStandingInteractions.get(i);

                 if (inter._finished) {
                     _outStandingInteractions.remove(i);
                     System.out.println("interaction " + inter._sendID + " has been removed");
                 }else{
                	 System.out.println("interaction " + inter._sendID + " has not finished yet.");
                 }  	 
            }
		}
		
		try {
		  Thread.sleep(10000);
		}catch (Exception e) {
          e.printStackTrace();     	
		}
	  }
		_running = false;
	}
                    
                    


	private List getReplies(String sendID){
//		ToDo
// open reciever and receive
//		jak poznam, ke ktere interakci dana odpoved patri - parsovani dle sendID if - then! - hashmap
		
		List replies = new ArrayList();
		
		if(sendID == "100"){
			Reply r1 =new Reply();
			r1._messageText = "Reply from JCoupling";
			r1._replyID = "111";
			r1._sendID = "100";
			
			replies.add(r1);
		}
		
		return replies;
	}
}



class Interaction {
    String _sendID; // solve how sendID will be created and in what format "2.5462"
    Reply _replyMessage;
    boolean _finished;
    WorkItemRecord _workItemRecord;
    public Element _dataForEngine;
}



class Reply {
    String _sendID;
    String _replyID;
    String _messageText;
}
