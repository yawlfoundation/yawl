/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.engine;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.jdom.JDOMException;

import au.edu.qut.yawl.exceptions.YPersistenceException;
import au.edu.qut.yawl.exceptions.YSchemaBuildingException;
import au.edu.qut.yawl.exceptions.YSyntaxException;
import au.edu.qut.yawl.util.YMessagePrinter;
import au.edu.qut.yawl.util.YVerificationMessage;

/**
 * Checks loading specifications into the engine
 * (bad specs, multiple at once, etc).
 * 
 * @author Nathan Rose
 */
public class TestAddSpecifications extends TestCase {
    private AbstractEngine _engine;
    private File _spec1File;
    private File _spec2File;

    public TestAddSpecifications(String name) {
        super(name);
    }

    public void setUp() throws YSchemaBuildingException, YSyntaxException, YPersistenceException,
    		JDOMException, IOException {
        _spec1File = getFile( "TestAddSpecifications1.xml" );
        _spec2File = getFile( "TestAddSpecifications2.xml" );
        _engine =  EngineFactory.createYEngine();
        EngineClearer.clear(_engine);
    }
    
    private static File getFile( String fileName ) {
    	URL fileURL = TestAddSpecifications.class.getResource( fileName );
    	return new File( fileURL.getFile() );
    }
    
    /**
     * Tests adding a single specification that has a bad tag in it.
     */
    public void testAddSpecifications1() throws YPersistenceException, JDOMException, IOException {
    	List<String> ids;
    	List<YVerificationMessage> errors = new LinkedList<YVerificationMessage>();
    	
    	ids = _engine.addSpecifications( _spec1File, false, errors );

    	assertNotNull( ids );
    	assertTrue( "" + ids.size(), ids.size() == 0 );
    	assertFalse( YMessagePrinter.getMessageString( errors ),
    			YVerificationMessage.containsNoErrors( errors ) );
    }
    
    /**
     * Tests adding a specification set that contains multiple specifications.
     */
    public void testAddSpecifications2() throws YPersistenceException, JDOMException, IOException {
    	List<String> ids;
    	List<YVerificationMessage> errors = new LinkedList<YVerificationMessage>();
    	
    	ids = _engine.addSpecifications( _spec2File, false, errors );
    	assertNotNull( ids );
    	assertTrue( "" + ids.size(), ids.size() < 4 );
    	assertFalse( YMessagePrinter.getMessageString( errors ),
    			YVerificationMessage.containsNoErrors( errors ) );
    	
    }
    
    public static void main(String args[]) {
    	TestAddSpecifications test = new TestAddSpecifications("");
    	try {
    		test.setUp();
    		test.testAddSpecifications1();
    		System.out.println( "success" );
    	}
    	catch( Exception e ) {
    		e.printStackTrace();
    	}
//        TestRunner runner = new TestRunner();
//        runner.doRun(suite());
//        System.exit(0);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(TestAddSpecifications.class);
        return suite;
    }
}
