package au.edu.qut.yawl.elements;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;
import au.edu.qut.yawl.engine.EngineFactory;
import au.edu.qut.yawl.engine.YEngineInterface;
import au.edu.qut.yawl.persistence.AbstractTransactionalTestCase;
import au.edu.qut.yawl.persistence.dao.restrictions.PropertyRestriction;
import au.edu.qut.yawl.persistence.dao.restrictions.PropertyRestriction.Comparison;
import au.edu.qut.yawl.unmarshal.*;
import au.edu.qut.yawl.util.SpringTestConfiguration;
import au.edu.qut.yawl.util.SpringTestConfiguration.Configuration;

public class TestServiceGateway extends TestCase {


	protected void setUp() throws Exception {
		// TODO Auto-generated method stub
		super.setUp();
		SpringTestConfiguration.setupTestConfiguration(Configuration.DEFAULT);
	}


	protected void tearDown() throws Exception {
		// TODO Auto-generated method stub
		super.tearDown();
	}

	/**
	 * 
	 * Test storing and restoring enablementparameters
	 * The test should successfully retreive a specification
	 * which has a gatway which contains resource enablement
	 * parameters
	 * 
	 * */
	public void testEnablementParameters() {
	
		YEngineInterface _engine = null;
		
		try {
			
			_engine=EngineFactory.getTransactionalEngine();
		} catch (Exception e) {
			System.out.println("no engine availalable");
		}
		//create the specification with resources
		
		File specificationFile = new File("classes\\au\\edu\\qut\\yawl\\elements\\testres.xml");
		List<String> specs = null;	
		
        try  {
        	specs = _engine.addSpecifications(specificationFile, true, new ArrayList()); 
        	
        	
        } catch (Exception e) {
        	e.printStackTrace();
        	fail("Exception should not be thrown");
        }
        

        try  {
       	
        	
        	String xml = _engine.getProcessDefinition(specs.get(0));

        	
        	assertTrue("No enablement parameters loaded", xml.contains("<enablementParam>"));
        	
        } catch (Exception e) {
        	e.printStackTrace();
        	fail("Exception should not be thrown");
        }
        
		
	}
	
	
		
}
