/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

package au.edu.qut.yawl.persistence.engine;

import au.edu.qut.yawl.engine.AbstractEngine;
import au.edu.qut.yawl.engine.EngineFactory;
import au.edu.qut.yawl.engine.YEngineInterface;
import au.edu.qut.yawl.persistence.managed.DataContext;
import junit.framework.TestCase;
import au.edu.qut.yawl.elements.YAWLServiceReference;


public class TestServiceInterface extends TestCase {

	public TestServiceInterface(String arg0) {
		super(arg0);
	}

	protected void setUp() throws Exception {
		super.setUp();

	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	

	public void testAddService() {
		try {	
			YEngineInterface engine = (YEngineInterface) EngineFactory.getTransactionalEngine();
			
			YAWLServiceReference ref = new YAWLServiceReference("http://test.test/testservice",null);

			int i = engine.getYAWLServices().size();
			engine.addYawlService(ref);			
			int j = engine.getYAWLServices().size();
			
			assert(i==(j+1));				
			
		} catch (Exception e) {
			fail("Exception thrown when adding a service");
		}
	}

	public void testRemoveService() {
		try {	
			YEngineInterface engine = (YEngineInterface) EngineFactory.getTransactionalEngine();
			
			YAWLServiceReference ref = new YAWLServiceReference("http://test.test/testservice",null);

			int i = engine.getYAWLServices().size();
			engine.removeYawlService("http://test.test/testservice");			
			int j = engine.getYAWLServices().size();
			
			assert(i==(j-1));				
			
		} catch (Exception e) {
			fail("Exception thrown when removing a service");
		}
	}
	
	public void testServiceAddAgain() {
		try {	
			YEngineInterface engine = (YEngineInterface) EngineFactory.getTransactionalEngine();
			
			YAWLServiceReference ref = new YAWLServiceReference("http://test.test/testservice",null);

			int i = engine.getYAWLServices().size();
			engine.addYawlService(ref);			
			int j = engine.getYAWLServices().size();
			
			assert(i==(j+1));				
			
		} catch (Exception e) {
			fail("Exception thrown when re-adding a service");
		}
	}
}
