/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

package au.edu.qut.yawl.persistence.engine;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.jdom.JDOMException;

import au.edu.qut.yawl.elements.YAWLServiceReference;
import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.elements.state.YIdentifier;
import au.edu.qut.yawl.engine.EngineClearer;
import au.edu.qut.yawl.engine.EngineFactory;
import au.edu.qut.yawl.engine.YEngineInterface;
import au.edu.qut.yawl.engine.YNetRunner;
import au.edu.qut.yawl.engine.domain.YWorkItem;
import au.edu.qut.yawl.exceptions.YDataStateException;
import au.edu.qut.yawl.exceptions.YPersistenceException;
import au.edu.qut.yawl.exceptions.YQueryException;
import au.edu.qut.yawl.exceptions.YSchemaBuildingException;
import au.edu.qut.yawl.exceptions.YStateException;
import au.edu.qut.yawl.persistence.AbstractTransactionalTestCase;
import au.edu.qut.yawl.persistence.StringProducer;
import au.edu.qut.yawl.persistence.StringProducerYAWL;
import au.edu.qut.yawl.persistence.dao.restrictions.LogicalRestriction;
import au.edu.qut.yawl.persistence.dao.restrictions.PropertyRestriction;
import au.edu.qut.yawl.persistence.dao.restrictions.LogicalRestriction.Operation;
import au.edu.qut.yawl.persistence.dao.restrictions.PropertyRestriction.Comparison;

/**
 * XXX: these tests need to be fixed so that they don't depend on the state of the
 * database from before the tests are run
 */
public class TestEnginePersistence extends AbstractTransactionalTestCase {

	YAWLServiceReference ys =null;
	YSpecification testSpec;
	
	protected void setUp() throws Exception {
		super.setUp();

		YEngineInterface engine = EngineFactory.getTransactionalEngine();
		EngineClearer.clear( engine );
		ys = new YAWLServiceReference(
				"http://localhost:8080/timeService/ib");
		ys.setDocumentation("Time service, allows tasks to be a timeout task.");
		engine.addYawlService(ys);
	}
	
	public void testStartCase() throws YPersistenceException, JDOMException, IOException, YStateException, YDataStateException, YSchemaBuildingException {
		StringProducer spx = StringProducerYAWL.getInstance();
		File f = spx.getTranslatedFile("Timer.xml", true);
		
		YEngineInterface engine = EngineFactory.getTransactionalEngine();

		LinkedList errors = new LinkedList();

		engine.addSpecifications(f, false, errors);	

		String caseid_string = engine.launchCase("test", "Timer.xml", null, null);

		YIdentifier caseid= engine.getCaseID(caseid_string);
		//List runners = context.retrieveAll(YNetRunner.class, null);    	

        List<YNetRunner> runners = engine.getDao().retrieveByRestriction(
        		YNetRunner.class,
        		new LogicalRestriction(
        				new PropertyRestriction( "archived", Comparison.EQUAL, false),
        				Operation.AND,
        				new PropertyRestriction( "basicCaseId", Comparison.EQUAL, caseid_string)
        				)
        		);
		
		/*
		 * Check that one runner was added to the database
		 * */
        try {
        	assertEquals("number of runners found", 1, runners.size());
        }
        finally {
        	engine.cancelCase(caseid);
        	
        	engine.unloadSpecification("Timer.xml");
        }
	}

	public void testCancelCase() throws YStateException, YPersistenceException, YDataStateException, YSchemaBuildingException, JDOMException, IOException {
		StringProducer spx = StringProducerYAWL.getInstance();
		File f = spx.getTranslatedFile("Timer.xml", true);
		
		YEngineInterface engine = EngineFactory.getTransactionalEngine();
		
		LinkedList errors = new LinkedList();
		engine.addSpecifications(f, false, errors);	
		
		String caseid_string = engine.launchCase("test", "Timer.xml", null, null);
		YIdentifier caseid= engine.getCaseID(caseid_string);
		
        List<YNetRunner> runners = engine.getDao().retrieveByRestriction(YNetRunner.class, 
        		new LogicalRestriction(
        				new PropertyRestriction( "archived", Comparison.EQUAL, false),
        				Operation.AND,
        				new PropertyRestriction( "basicCaseId", Comparison.EQUAL, caseid_string)
        				)
   		);    	
    	
		/*
		 * Check that no runners was added to the database
		 */
        try {
        	assertEquals("wrong number of runners", 1, runners.size());
        }
		finally {
			if( runners != null && runners.get( 0 ) != null ) {
				YNetRunner runner = runners.get(0);
				
				engine.cancelCase(caseid);			
				engine.unloadSpecification("Timer.xml");
			}
		}
	}
	
	/*
	 * Ensure that when a workitem is completed
	 * its data is mapped back to YCaseData to make it 
	 * available to the next workitem
	 * */
	/*
	public void testDataMappingStorage() {
	}
	*/
	
	public void testStartCaseMultipleInstanceDecomp() throws YPersistenceException, JDOMException, IOException, YStateException, YDataStateException, YSchemaBuildingException {
		StringProducer spx = StringProducerYAWL.getInstance();
		File f = spx.getTranslatedFile("YAWL_Specification1.xml", true);
		YEngineInterface engine = EngineFactory.getTransactionalEngine();
		engine.addSpecifications(f, false, new LinkedList());


		String caseid_string = engine.launchCase("test", "YAWL_Specification1.xml", null, null);

		YIdentifier caseid = engine.getCaseID(caseid_string);

		engine.cancelCase(caseid);
		
		engine.unloadSpecification("YAWL_Specification1.xml");
	}

	public void testCancelWithBusyTasks() throws YPersistenceException, JDOMException, IOException, YStateException, YDataStateException, YSchemaBuildingException, YQueryException {
		StringProducer spx = StringProducerYAWL.getInstance();
		File f = spx.getTranslatedFile("Timer.xml", true);
		
		YEngineInterface engine = EngineFactory.getTransactionalEngine();
		
		LinkedList errors = new LinkedList();
		engine.addSpecifications(f, false, errors);	
		
		String caseid_string = engine.launchCase("test", "Timer.xml", null, null);
		YIdentifier caseid= engine.getCaseID(caseid_string);
		
//		YWorkItem workItem = engine.getWorkItem(caseid.toString() + ":a");
		engine.startWorkItem(caseid.toString() + ":a", "tore");
		
		engine.cancelCase(caseid);

        List<YNetRunner> runners = engine.getDao().retrieveByRestriction(YNetRunner.class, 
        		new LogicalRestriction(
        				new PropertyRestriction( "archived", Comparison.EQUAL, false),
        				Operation.AND,
        				new PropertyRestriction( "basicCaseId", Comparison.EQUAL, caseid_string)
        				)
        );    	
    	
        try {
        	assertEquals("should be no runners", 0, runners.size());
        }
		finally { 
			engine.unloadSpecification("Timer.xml");
		}
	}
	
	/*
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
	*/
}
