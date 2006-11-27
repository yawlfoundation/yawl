package au.edu.qut.yawl.deployment;


import junit.framework.TestCase;
import au.edu.qut.yawl.deployment.*;
import au.edu.qut.yawl.engine.*;
import au.edu.qut.yawl.persistence.StringProducer;
import au.edu.qut.yawl.persistence.StringProducerYAWL;
import java.io.File;
import java.util.LinkedList;
import java.util.Set;
import java.util.Iterator;
import au.edu.qut.yawl.elements.YAWLServiceReference;
import au.edu.qut.yawl.persistence.managed.DataContext;

public class TestAutoDeployment extends TestCase {

	
    public void testSimpleJarAdd() {
        try {

            YEngine _engine =  EngineFactory.createYEngine();
            
     	

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

			Thread.sleep(10000);
			
        	
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }
    
}
