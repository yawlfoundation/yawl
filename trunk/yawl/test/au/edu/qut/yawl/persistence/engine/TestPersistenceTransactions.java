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

import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.elements.state.YIdentifier;
import au.edu.qut.yawl.engine.EngineFactory;
import au.edu.qut.yawl.engine.YEngine;
import au.edu.qut.yawl.engine.YEngineInterface;
import au.edu.qut.yawl.engine.YNetRunner;
import au.edu.qut.yawl.engine.domain.YWorkItem;
import au.edu.qut.yawl.exceptions.YDataStateException;
import au.edu.qut.yawl.exceptions.YPersistenceException;
import au.edu.qut.yawl.exceptions.YSchemaBuildingException;
import au.edu.qut.yawl.exceptions.YStateException;
import au.edu.qut.yawl.persistence.AbstractTransactionalTestCase;
import au.edu.qut.yawl.persistence.StringProducer;
import au.edu.qut.yawl.persistence.StringProducerYAWL;
import au.edu.qut.yawl.persistence.dao.restrictions.LogicalRestriction;
import au.edu.qut.yawl.persistence.dao.restrictions.PropertyRestriction;
import au.edu.qut.yawl.persistence.dao.restrictions.Unrestricted;
import au.edu.qut.yawl.persistence.dao.restrictions.LogicalRestriction.Operation;
import au.edu.qut.yawl.persistence.dao.restrictions.PropertyRestriction.Comparison;

public class TestPersistenceTransactions extends AbstractTransactionalTestCase {
	
	YSpecification testSpec;
	YEngine engine;
	public TestPersistenceTransactions() {
		super();
	}
	
	
	
	public void testTransaction() throws YPersistenceException, JDOMException, IOException, YStateException, YDataStateException, YSchemaBuildingException {
		engine = EngineFactory.createYEngine();
		System.out.println("Testing transactions");
		StringProducer spx = StringProducerYAWL.getInstance();
		File f = spx.getTranslatedFile("SingleTask.xml", true);
		
		EngineFactory.createYEngine(true);
		YEngineInterface engine = (YEngineInterface) EngineFactory.getTransactionalEngine();

		LinkedList errors = new LinkedList();
		engine.addSpecifications(f, false, errors);	

		String caseid_string = engine.launchCase("test", "singletask", null, null);

		System.out.println(engine.getStateForCase(caseid_string));

        List<YNetRunner> runners = engine.getDao().retrieveByRestriction(
        		YNetRunner.class,
        		new LogicalRestriction(
        				new PropertyRestriction( "archived", Comparison.EQUAL, false),
        				Operation.AND,
        				new PropertyRestriction( "YNetID", Comparison.EQUAL, "singletask" ) ) );
        
        /*
         * When we cancel a case, should we
         * delete all its workItems???
         * */
		List items = engine.getDao().retrieveByRestriction(YWorkItem.class, new Unrestricted());

		//System.out.println(runners.size());
		//System.out.println(items.size());
		
		assertTrue("" + runners.size(), runners.size()==1);
		//assertTrue(items.size()==1);

		YIdentifier caseid = engine.getCaseID(caseid_string);
		
		engine.cancelCase(caseid);
		engine.unloadSpecification("singletask");
		
		System.out.println(engine.getStateForCase(caseid_string));
	}

}
