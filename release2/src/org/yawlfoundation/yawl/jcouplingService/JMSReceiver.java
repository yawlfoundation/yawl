package org.yawlfoundation.yawl.jcouplingService;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

public class JMSReceiver extends JMSConnector implements MessageListener{

	private String destName = "replyQueue";
	
	public JMSReceiver() throws Exception {
		super();
		this.connect();
		Destination dest = (Destination) this.getContext().lookup(destName);
        MessageConsumer consumer = this.getSession().createConsumer(dest);
        MessageListener listener = this;
        consumer.setMessageListener(listener);
        
        this.getConnection().start();	
        
	}

	
	
    /**
     * Receives message, which was sent by JCoupling. When message contains correlationID, the appropriate messageID is found 
     * within Mapping and 
     * @throws JMSException 
     * 
     */
	public void onMessage(Message message){
		TextMessage textMessage = (TextMessage) message;
				
		//1. filter was succesfully registred
		try {
			if (textMessage.getJMSCorrelationID()!=null){
				
				for (Controller controller: JCouplingService.get_outStandingControllers()){
					if (controller.getState().equals(State.filter_registration)){
						if(controller.getMapping().getMsg_ID().equals(message.getJMSCorrelationID())){
							controller.getMapping().setRequestKey(textMessage.getText());
							controller.setState(State.requestKey_received);
						}
					}
				}
			}
			
			//2. message command task
			else if (message.getJMSCorrelationID()==null){
				String msgRequestKey = textMessage.getText();
				String response = textMessage.getText();
				System.out.println(response);
				
				for (Controller controller: JCouplingService.get_outStandingControllers()){
					if (controller.getState().equals(State.requestKey_received)){
						if(controller.getMapping().getRequestKey().equals(msgRequestKey)){
							controller.setResponse(response);
							controller.setState(State.message_received);
						}
					}
				}
			}
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
	
	
}
