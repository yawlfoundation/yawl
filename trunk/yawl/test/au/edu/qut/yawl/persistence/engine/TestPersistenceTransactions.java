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

import junit.framework.TestCase;
import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.elements.state.YIdentifier;
import au.edu.qut.yawl.engine.AbstractEngine;
import au.edu.qut.yawl.engine.EngineClearer;
import au.edu.qut.yawl.engine.EngineFactory;
import au.edu.qut.yawl.engine.YEngineInterface;
import au.edu.qut.yawl.engine.YNetRunner;
import au.edu.qut.yawl.engine.domain.YWorkItem;
import au.edu.qut.yawl.exceptions.YDataStateException;
import au.edu.qut.yawl.exceptions.YPersistenceException;
import au.edu.qut.yawl.exceptions.YSchemaBuildingException;
import au.edu.qut.yawl.exceptions.YStateException;
import au.edu.qut.yawl.persistence.StringProducer;
import au.edu.qut.yawl.persistence.StringProducerYAWL;
import au.edu.qut.yawl.persistence.dao.restrictions.LogicalRestriction;
import au.edu.qut.yawl.persistence.dao.restrictions.PropertyRestriction;
import au.edu.qut.yawl.persistence.dao.restrictions.Unrestricted;
import au.edu.qut.yawl.persistence.dao.restrictions.LogicalRestriction.Operation;
import au.edu.qut.yawl.persistence.dao.restrictions.PropertyRestriction.Comparison;

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
	
	public void testTransaction() throws YPersistenceException, JDOMException, IOException, YStateException, YDataStateException, YSchemaBuildingException {
		System.out.println("Testing transactions");
		StringProducer spx = StringProducerYAWL.getInstance();
		File f = spx.getTranslatedFile("SingleTask.xml", true);
		
		YEngineInterface engine = (YEngineInterface) EngineFactory.getTransactionalEngine();
        EngineClearer.clear(engine);

		LinkedList errors = new LinkedList();
		engine.addSpecifications(f, false, errors);

		String caseid_string = engine.launchCase("test", "singletask", null, null);

		System.out.println(engine.getStateForCase(caseid_string));

        List<YNetRunner> runners = AbstractEngine.getDao().retrieveByRestriction(
                YNetRunner.class,
        		new LogicalRestriction(
                        new PropertyRestriction( "archived", Comparison.EQUAL, false),
                        Operation.AND,
                        new PropertyRestriction( "YNetID", Comparison.EQUAL, "singletask" ) ) );
        
        /*
         * When we cancel a case, should we
         * delete all its workItems???
         * */
		List items = AbstractEngine.getDao().retrieveByRestriction(YWorkItem.class, new Unrestricted());

		//System.out.println(runners.size());
		//System.out.println(items.size());
		
		assertTrue("" + runners.size(), runners.size()==1);
		//assertTrue(items.size()==1);

		YIdentifier caseid = engine.getCaseID(caseid_string);
		
		engine.cancelCase(caseid);
		engine.unloadSpecification("singletask");
		EngineClearer.clear(engine);
		
		
		System.out.println(engine.getStateForCase(caseid_string));
	}
}
