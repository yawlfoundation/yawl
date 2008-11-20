package org.yawlfoundation.yawl.jcouplingService;


public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Test test = new Test();
		JCouplingModule jcm = new JCouplingModule();
		test.send(jcm);
	}
	
	private void send(JCouplingModule jcm) {
		String id1 = jcm.performSend("1000 - First message from YAWL Engine to JCoupling.");
		System.out.println("Message " + id1 + " was sent.");
		
	}
}
