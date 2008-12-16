package org.yawlfoundation.yawl.jcouplingService;

import org.jdom.Element;
import org.yawlfoundation.yawl.elements.data.YParameter;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.engine.interfce.interfaceB.InterfaceBWebsideController;

import java.util.ArrayList;
import java.util.List;

/** Bridge between JCoupling and YAWL Engine */

public class JCouplingService extends InterfaceBWebsideController implements Runnable{

/**	
 * not yet finished interactions, these interactions are still running inside the YAWL Engine
*/
	private static List<Controller> _outStandingControllers = new ArrayList(); 

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
 *	JMS receiver enables receiving messages from JMS server 
 */
	private JMSReceiver receiver;
		
		
	
/**	
 * <br>1) Check the connection, if it is OK(_sessionHandle)
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
                        	Controller controller = new Controller(itemRecord);
                        	controller.sendMessage(message, State.filter_registration);
                        	receiver = new JMSReceiver();
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
* Removes (if necessary) the work items from the YAWL Engine
*/	
    @Override
    public void handleCancelledWorkItemEvent(WorkItemRecord workItemRecord) {
        synchronized (this) {
            System.out.println("PreCancel::_outStandingInteractions = " + _outStandingControllers);
            for (int i = 0; i < _outStandingControllers.size(); i++) {
                Controller _controller = (Controller)
                        _outStandingControllers.get(i);
                if (_controller.getWorkItemRecord().getID().equals(workItemRecord.getID())) {
                    _outStandingControllers.remove(i);
                }
            }
        }
        System.out.println("\tPostCancel::_outStandingInteractions = " + _outStandingControllers);
    }


	public void run() {
		_running = true;


		while (_outStandingControllers.size() > 0) {
			 
	synchronized(this){
		for(int i=0; i< _outStandingControllers.size(); i++){
			Controller controller = (Controller) _outStandingControllers.get(i);
		  try {
			  
			  controller.checkState();
			  
			  if(controller.getState().equals(State.finished)){
				  Element replyMsg = new Element(REPLY_MESSAGE);
				  replyMsg.setText(controller.getResponse());
						
				  Element dataForEngine = controller.getDataForEngine();
				  dataForEngine.addContent(replyMsg);
				  
					
				System.out.println("Content of Reply for Message <" + controller.getMapping().getWorkItemID() + "> " +
						"is:<" + controller.getResponse() + ">");
			  
				  checkInWorkItem(controller.getWorkItemRecord().getID(),
						  			controller.getWorkItemRecord().getWorkItemData(),
									controller.getDataForEngine(),
								        _sessionHandle);
				  
				  _outStandingControllers.remove(i);
			 
			  
			  }else{
				System.out.println("interaction <" + controller.getMapping().getWorkItemID() + "> has no replies.");
				}		
		 }catch (Exception e) {
				e.printStackTrace();
			 }
		}
			
		try {
			Thread.sleep(10000);
		}
		catch (Exception e) {
			e.printStackTrace();     	
		}
	  }
	  _running = false;
	 }
	}
	
    
	public static List<Controller> get_outStandingControllers() {
		return _outStandingControllers;
	}

}


