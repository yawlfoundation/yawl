/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

package au.edu.qut.yawl.persistence.engine;

import au.edu.qut.yawl.elements.YAWLServiceReference;
import au.edu.qut.yawl.engine.EngineFactory;
import au.edu.qut.yawl.engine.YEngineInterface;
import au.edu.qut.yawl.exceptions.YPersistenceException;
import au.edu.qut.yawl.persistence.AbstractTransactionalTestCase;


public class TestServiceInterface extends AbstractTransactionalTestCase {
	public TestServiceInterface(String arg0) {
		super(arg0);
	}

	public void testServiceReferenceLifecycle() throws YPersistenceException {
		YEngineInterface engine = (YEngineInterface) EngineFactory.getTransactionalEngine();
		
		YAWLServiceReference ref = new YAWLServiceReference("http://test.test/testservice" +
                System.currentTimeMillis());

		int before = engine.getYAWLServices().size();
		engine.addYawlService(ref);
		int after = engine.getYAWLServices().size();
		
		assertTrue(before + "  " + after, after==(before+1));
        
        engine.removeYawlService(ref.getURI());
        
        int afterremove = engine.getYAWLServices().size();
        
        assertTrue(before + "  " + after + "  " + afterremove, afterremove==(after-1));
        
        ref.setEnabled( true );
        engine.addYawlService(ref);
        
        int end = engine.getYAWLServices().size();
        
        assertTrue(before + "  " + after + "  " + afterremove + "  " + end, end==(afterremove+1));
        
        engine.removeYawlService(ref.getURI());
	}
}
