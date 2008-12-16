package org.yawlfoundation.yawl.jcouplingService;

import java.net.MalformedURLException;

import javax.jms.JMSException;

import org.exolab.jms.administration.AdminConnectionFactory;
import org.exolab.jms.administration.JmsAdminServerIfc;
import org.yawlfoundation.yawl.jcouplingServiceTest.TestReceiver;


/**	
 *	JMSAdmin class enables creating queues for communication between JCoupling & JCouplingService within YAWL Engine
 *	2 queues are created - replyQueue & requestQueue
 */
public class JMSAdmin {
	
	public static void main(String[] args) {

		JMSAdmin  admin = new JMSAdmin();
		try {
			admin.process();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void process() throws Exception, Exception {

	String url = "tcp://localhost:3035/";
    String user = "admin";
    String password = "openjms";
    
    JmsAdminServerIfc admin = AdminConnectionFactory.create(url, user, password);
    
    
    String replyQueue = "replyQueue";
    Boolean isReplyQueue = Boolean.TRUE;
    if (!admin.addDestination(replyQueue, isReplyQueue)) {
        System.err.println("Failed to create queue " + replyQueue);
    }else{
    	System.out.println("replyQueue created");
    }
    
    String requestQueue = "requestQueue";
    Boolean isRequestQueue = Boolean.TRUE;
    if (!admin.addDestination(requestQueue, isRequestQueue)) {
        System.err.println("Failed to create queue " + requestQueue);
    }else{
    	System.out.println("requestQueue created");
    }
    
    admin.close();
	
	}
}