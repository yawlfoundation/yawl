/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

package au.edu.qut.yawl.deployment;


import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import junit.framework.TestCase;

import au.edu.qut.yawl.elements.YAWLServiceReference;
import au.edu.qut.yawl.engine.EngineFactory;
import au.edu.qut.yawl.engine.YEngine;
import au.edu.qut.yawl.persistence.AbstractTransactionalTestCase;
import au.edu.qut.yawl.persistence.StringProducer;
import au.edu.qut.yawl.persistence.StringProducerYAWL;

public class TestAutoDeployment extends AbstractTransactionalTestCase {

	
	
    public void testSimpleJarAdd() throws Exception {
        YEngine _engine =  EngineFactory.createYEngine(true);
        
 	

    	Set s = _engine.getYAWLServices();
    	
    	Iterator i = s.iterator();
    	while (i.hasNext()) {
    		YAWLServiceReference ref = (YAWLServiceReference) i.next();
    		System.out.println("*******************************");
    		System.out.println(ref.getURI());
    		System.out.println(ref.getDocumentation());
    		System.out.println("*******************************");
    	}
    	
		StringProducer spx = StringProducerYAWL.getInstance();
		File f = spx.getTranslatedFile("InternalTest2.xml", true);

		LinkedList errors = new LinkedList();

		
		_engine.addSpecifications(f, false, errors);	

		System.out.println("errors: " + errors.size());
		
		String caseid_string = _engine.launchCase("test", "InternalTest2.xml", null, null);
    }
    
}
