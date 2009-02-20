package org.yawlfoundation.yawl.jcouplingService;

import javax.jms.Destination;
import javax.jms.MessageProducer;
import javax.jms.TextMessage;

public class JMSSender extends JMSConnector{

	private String requestDestName = "requestQueue";
    private Destination requestDest;
    
	private String replyDestName = "replyQueue";
    private Destination replyDest;
	
    public JMSSender() throws Exception {
		super();
	}
	
	public String send(String content) throws Exception{
		this.connect();
    	requestDest = (Destination) this.getContext().lookup(requestDestName);
    	MessageProducer sender = this.getSession().createProducer(requestDest);
        this.getConnection().start();
        
		TextMessage textMessage = this.getSession().createTextMessage();
		textMessage.setText(content);
		replyDest = (Destination) this.getContext().lookup(replyDestName);
		textMessage.setJMSReplyTo(replyDest);
        sender.send(textMessage);
        // save msgID
        
        System.out.println("text:" + textMessage.getText());
        System.out.println("reply to:" + textMessage.getJMSReplyTo());
        System.out.println("Msg ID:" + textMessage.getJMSMessageID());
        System.out.println("Correlation ID:" + textMessage.getJMSCorrelationID());
        
        return textMessage.getJMSMessageID();
    }
}
