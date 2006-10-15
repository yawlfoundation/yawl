package au.edu.qut.yawl.persistence.engine;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.LinkedList;
import java.util.Set;

import au.edu.qut.yawl.persistence.StringProducerXML;
import au.edu.qut.yawl.persistence.StringProducerYAWL;

import junit.framework.TestCase;
import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.elements.state.YIdentifier;
import au.edu.qut.yawl.engine.AbstractEngine;
import au.edu.qut.yawl.engine.EngineClearer;
import au.edu.qut.yawl.engine.EngineFactory;
import au.edu.qut.yawl.engine.YEngine;
import au.edu.qut.yawl.engine.YEngineInterface;
import au.edu.qut.yawl.engine.YNetRunner;
import au.edu.qut.yawl.engine.domain.YWorkItem;
import au.edu.qut.yawl.engine.domain.YWorkItemRepository;
import au.edu.qut.yawl.persistence.TestHibernateMarshal;
import au.edu.qut.yawl.persistence.dao.DAO;
import au.edu.qut.yawl.persistence.dao.DAOFactory;
import au.edu.qut.yawl.persistence.dao.DAOFactory.PersistenceType;
import au.edu.qut.yawl.persistence.dao.restrictions.PropertyRestriction;
import au.edu.qut.yawl.persistence.dao.restrictions.Unrestricted;
import au.edu.qut.yawl.persistence.dao.restrictions.PropertyRestriction.Comparison;
import au.edu.qut.yawl.persistence.managed.DataContext;
import au.edu.qut.yawl.persistence.managed.DataProxy;
import au.edu.qut.yawl.elements.YAWLServiceReference;
import au.edu.qut.yawl.unmarshal.YMarshal;
import au.edu.qut.yawl.elements.YAtomicTask;
import au.edu.qut.yawl.util.YVerificationMessage;
import au.edu.qut.yawl.engine.EngineClearer;

public class TestEnginePersistence extends TestCase {

	YAWLServiceReference ys =null;
	YSpecification testSpec;
	
	protected void setUp() throws Exception {
		super.setUp();

		DAO hib = DAOFactory.getDAO( PersistenceType.HIBERNATE );
		DataContext context = new DataContext( hib );
		AbstractEngine.setDataContext(context);

			YEngineInterface engine = EngineFactory.getTransactionalEngine();
			engine.getYEngine().getWorkItemRepository().clear();
			ys = new YAWLServiceReference(
					"http://localhost:8080/timeService/ib", null);
			ys.setDocumentation("Time service, allows tasks to be a timeout task.");
			engine.addYawlService(ys);
		
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		YEngineInterface engine = EngineFactory.getTransactionalEngine();
		engine.removeYawlService(ys.getYawlServiceID());
	}

	private DAO getDAO() {
		return DAOFactory.getDAO( PersistenceType.HIBERNATE );
	}
	
	public void testStartCase() {
		try {	

		
			StringProducerXML spx = StringProducerYAWL.getInstance();
			File f = spx.getTranslatedFile("Timer.xml", true);
			

			YEngineInterface engine = EngineFactory.getTransactionalEngine();


			DataContext context = AbstractEngine.getDataContext();			
			
			LinkedList errors = new LinkedList();
			engine.addSpecifications(f, false, errors);	
			
			String caseid_string = engine.launchCase("test", "Timer.xml", null, null);
			YIdentifier caseid= engine.getCaseID(caseid_string);
			List runners = context.retrieveAll(YNetRunner.class, null);    	
        	
			/*
			 * Check that one runner was added to the database
			 * */
			assertTrue(runners.size()==1);
			System.out.println("Caseid: " + caseid);
			
			engine.cancelCase(caseid);
			
			engine.unloadSpecification("Timer.xml");


		} catch (Exception e) {
			e.printStackTrace();
			fail("No exception should be thrown");
		}	
	}

	public void testCancelCase() {
		try {
						
			StringProducerXML spx = StringProducerYAWL.getInstance();
			File f = spx.getTranslatedFile("Timer.xml", true);
			
			YEngineInterface engine = EngineFactory.getTransactionalEngine();
			DataContext context = AbstractEngine.getDataContext();
			
			LinkedList errors = new LinkedList();
			engine.addSpecifications(f, false, errors);	
			
			String caseid_string = engine.launchCase("test", "Timer.xml", null, null);
			YIdentifier caseid= engine.getCaseID(caseid_string);
			
			List runners = context.retrieveAll(YNetRunner.class, null);    	
        	
			/*
			 * Check that no runners was added to the database
			 */
			assertTrue(runners.size()==1);
			
			DataProxy runner = (DataProxy) runners.get(0);
			
			engine.cancelCase(caseid);
			
			engine.unloadSpecification("Timer.xml");
			

		} catch (Exception e) {
			e.printStackTrace();
			fail("No exception should be thrown");
		}		
	}
	
	/*
	 * Ensure that when a workitem is completed
	 * its data is mapped back to YCaseData to make it 
	 * available to the next workitem
	 * */
	public void testDataMappingStorage() {
		/*
		 * */
	}
	
	public void testStartCaseMultipleInstanceDecomp() {
		try {
			
			StringProducerXML spx = StringProducerYAWL.getInstance();
			File f = spx.getTranslatedFile("YAWL_Specification1.xml", true);
			YEngineInterface engine = EngineFactory.getTransactionalEngine();
			DataContext context = AbstractEngine.getDataContext();
			engine.addSpecifications(f, false, new LinkedList());
	
			String caseid_string = engine.launchCase("test", "YAWL_Specification1.xml", null, null);
			YIdentifier caseid = engine.getCaseID(caseid_string);

			//System.out.println("test");
			//Thread.sleep(10000);
				
			engine.cancelCase(caseid);
			
			engine.unloadSpecification("YAWL_Specification1.xml");
			
			
		} catch (Exception e) {
			e.printStackTrace();
			fail("No exception should be thrown");			
		}	

	}

	public void testCancelWithBusyTasks() {
		try {
			
			StringProducerXML spx = StringProducerYAWL.getInstance();
			File f = spx.getTranslatedFile("Timer.xml", true);
			
			YEngineInterface engine = EngineFactory.getTransactionalEngine();
			DataContext context = AbstractEngine.getDataContext();			
			
			LinkedList errors = new LinkedList();
			engine.addSpecifications(f, false, errors);	
			
			String caseid_string = engine.launchCase("test", "Timer.xml", null, null);
			YIdentifier caseid= engine.getCaseID(caseid_string);
			
			YWorkItem workItem = engine.getWorkItem(caseid.toString() + ":a");
			engine.startWorkItem(workItem, "tore");
			
			engine.cancelCase(caseid);
	
			List runners = context.retrieveAll(YNetRunner.class, null);    	
        	
			assertTrue(runners.size()==0);
			
			engine.unloadSpecification("Timer.xml");

		} catch (Exception e) {
			e.printStackTrace();
			fail("No exception should be thrown");
		}		

	
	}
	public void testCompleteCase() {
		try {
			
		} catch (Exception e) {
		}
	}

	public void testRestoreWithBusyTasks() {
		try {
			
		} catch (Exception e) {
		}
	}

	public void testSuspendTask() {
		try {


		} catch (Exception e) {
		}
	}

	public void testMultiInstanceCompletionDataMapping() {
		try {

		} catch (Exception e) {
		}
	}

}
