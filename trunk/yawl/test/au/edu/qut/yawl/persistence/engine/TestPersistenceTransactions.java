package au.edu.qut.yawl.persistence.engine;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;
import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.elements.state.YIdentifier;
import au.edu.qut.yawl.engine.AbstractEngine;
import au.edu.qut.yawl.engine.EngineFactory;
import au.edu.qut.yawl.engine.YEngineInterface;
import au.edu.qut.yawl.engine.YNetRunner;
import au.edu.qut.yawl.engine.domain.YWorkItem;
import au.edu.qut.yawl.persistence.StringProducer;
import au.edu.qut.yawl.persistence.StringProducerYAWL;
import au.edu.qut.yawl.persistence.managed.DataContext;

public class TestPersistenceTransactions extends TestCase {
	
	YSpecification testSpec;
	
	public TestPersistenceTransactions(String arg0) {
		super(arg0);
	}

	protected void setUp() throws Exception {
		super.setUp();

	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testTransaction() {
		try {
		
			System.out.println("Testing transactions");
			StringProducer spx = StringProducerYAWL.getInstance();
			File f = spx.getTranslatedFile("SingleTask.xml", true);
			
			YEngineInterface engine = (YEngineInterface) EngineFactory.getTransactionalEngine();
			DataContext context = AbstractEngine.getDataContext();			

			LinkedList errors = new LinkedList();
			engine.addSpecifications(f, false, errors);	

			String caseid_string = engine.launchCase("test", "singletask", null, null);
					
			List runners = context.retrieveAll(YNetRunner.class, null);    	
			List items = context.retrieveAll(YWorkItem.class, null);    	

			System.out.println(runners.size());
			System.out.println(items.size());
			
			assertTrue(runners.size()==1);
			assertTrue(items.size()==1);

			YIdentifier caseid = engine.getCaseID(caseid_string);
			
			engine.cancelCase(caseid);
			engine.unloadSpecification("singletask");
			//EngineClearer.clear(engine);
					
			
			
			
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
    		sw.write( e.toString() + "\n" );
    		e.printStackTrace(new PrintWriter(sw));
    		fail( sw.toString() );
		}
	}

}
