package au.edu.qut.yawl.jcouplingModule;


public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Test test = new Test();
		JCouplingModule jcm = new JCouplingModule();
//		test.send(jcm);
		test.receive(jcm);

	}
	
	private void send(JCouplingModule jcm) {
		String id1 = jcm.performSend("1000 - First message from YAWL Engine to JCoupling.");
		System.out.println("Message " + id1 + " was sent.");
		
	}

	private void receive(JCouplingModule jcm) {
//		receive
		Interaction inter1 = new Interaction();
        inter1._sendID = "100";
        jcm._outStandingInteractions.add(inter1);
        
        Interaction inter2 = new Interaction();
        inter2._sendID = "200";
        jcm._outStandingInteractions.add(inter2);
        
        Interaction inter3 = new Interaction();
        inter3._sendID = "300";
        jcm._outStandingInteractions.add(inter3);
        
//		now send msg with id1 through openJMS command line
		jcm.run();
		

	}

}
