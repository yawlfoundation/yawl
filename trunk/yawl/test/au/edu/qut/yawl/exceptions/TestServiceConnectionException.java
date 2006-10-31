package au.edu.qut.yawl.exceptions;

import java.io.File;

import au.edu.qut.yawl.engine.AbstractEngine;
import au.edu.qut.yawl.engine.YEngine;
import au.edu.qut.yawl.engine.EngineFactory;
import au.edu.qut.yawl.engine.YEngineInterface;
import au.edu.qut.yawl.persistence.dao.DAO;
import au.edu.qut.yawl.persistence.dao.DAOFactory;
import au.edu.qut.yawl.persistence.dao.DAOFactory.PersistenceType;
import au.edu.qut.yawl.persistence.managed.DataContext;
import au.edu.qut.yawl.elements.YAWLServiceReference;
import junit.framework.TestCase;
import java.util.ArrayList;
import au.edu.qut.yawl.persistence.StringProducer;
import au.edu.qut.yawl.persistence.StringProducerYAWL;
import au.edu.qut.yawl.util.YVerificationMessage;

public class TestServiceConnectionException extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();

		EngineFactory.resetEngine();
		YEngineInterface engine = EngineFactory.getTransactionalEngine();
		
		engine.getYEngine().getWorkItemRepository().clear();
		YAWLServiceReference ys = new YAWLServiceReference(
				"http://fefefeaeesf.cece/noservice", null);
		ys.setDocumentation("No Service");
		engine.addYawlService(ys);
		
	}
	
	
	public void testConnection()
	{
		try {
			YEngine engine = EngineFactory.getExistingEngine();
			StringProducer spx = StringProducerYAWL.getInstance();
			File f = spx.getTranslatedFile("TestService.xml", true);
		
			ArrayList errors = new ArrayList();
			engine.addSpecifications(f, false, errors);
			
			for (int i = 0; i < errors.size();i++) {
				System.out.println(((YVerificationMessage) errors.get(i)).getMessage());
			}
			

			String caseid_string = engine.launchCase("test", "TestService", null, null);

			System.out.println(engine.getStateForCase(engine.getCaseID(caseid_string)));

			engine.cancelCase(engine.getCaseID(caseid_string));

			System.out.println(engine.getStateForCase(caseid_string));

			engine.unloadSpecification("TestService");
			
			engine.removeYawlService("http://fefefeaeesf.cece/noservice");
			
			
			YAWLServiceReference ys = new YAWLServiceReference(
					"http://fefefeaeesf.cece/noservice", null);
			ys.setDocumentation("No Service - again");
			engine.addYawlService(ys);
			
			//This should contain the state including an error
			
			//This last state should return the case state
			
			
		} catch (Exception e) {
			e.printStackTrace();
			fail("No exception should be thrown");
		}

	}
	
}
