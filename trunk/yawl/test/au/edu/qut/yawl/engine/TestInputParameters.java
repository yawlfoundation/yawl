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
import java.io.StringReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.elements.state.YIdentifier;
import au.edu.qut.yawl.engine.domain.YCaseData;
import au.edu.qut.yawl.engine.domain.YWorkItem;
import au.edu.qut.yawl.engine.domain.YWorkItemRepository;
import au.edu.qut.yawl.exceptions.YDataStateException;
import au.edu.qut.yawl.exceptions.YDataValidationException;
import au.edu.qut.yawl.exceptions.YPersistenceException;
import au.edu.qut.yawl.exceptions.YSchemaBuildingException;
import au.edu.qut.yawl.exceptions.YStateException;
import au.edu.qut.yawl.exceptions.YSyntaxException;
import au.edu.qut.yawl.util.YMessagePrinter;
import au.edu.qut.yawl.util.YVerificationMessage;

/**
 * Tests starting cases that have input parameters.
 * 
 * @author Nathan Rose
 */
public class TestInputParameters extends TestCase {
	private YIdentifier _idForTopNet;
    private YWorkItemRepository _workItemRepository = YWorkItemRepository.getInstance();
    private AbstractEngine _engine;
    private File _spec1File;
    private File _spec2File;

    public TestInputParameters(String name) {
        super(name);
    }

    public void setUp() throws YSchemaBuildingException, YSyntaxException, YPersistenceException,
    		JDOMException, IOException {
        _spec1File = getFile( "TestInputParameters1.xml" );
        _spec2File = getFile( "TestInputParameters2.xml" );
        _engine =  EngineFactory.createYEngine();
        EngineClearer.clear(_engine);
    }
    
    private static File getFile( String fileName ) {
    	URL fileURL = TestInputParameters.class.getResource( fileName );
    	return new File( fileURL.getFile() );
    }
    
    private YSpecification readSpecification(File specFile) throws YPersistenceException, JDOMException, IOException {
    	List<String> specIDs;
    	List<YVerificationMessage> errors = new LinkedList<YVerificationMessage>();
    	
    	// load the spec
    	specIDs = _engine.addSpecifications( specFile, false, errors );
    	assertNotNull( YMessagePrinter.getMessageString( errors ), specIDs );
    	assertTrue( YMessagePrinter.getMessageString( errors ),
    			YVerificationMessage.containsNoErrors( errors ) );
    	return _engine.getSpecification( specIDs.get( 0 ) );
    }
    
    /**
     * Tests starting a case with input parameters where the proper data is given.
     */
    public void testInputParameters1() throws JDOMException, IOException,
    		YPersistenceException, YStateException, YSchemaBuildingException, YDataStateException {
		// variables
    	Set<YWorkItem> workItems;
    	Iterator<YWorkItem> iter;
    	YWorkItem item;
    	List<YNetRunner> netRunners = new Vector<YNetRunner>();
    	List<YIdentifier> ids = new LinkedList<YIdentifier>();
    	String data;
    	
    	// load the specification
    	YSpecification spec = readSpecification( _spec1File );
    	
    	// create a proper data XML string
    	data = "<OverseeMusic>" +
    		"<aParam>value</aParam>" +
    		"<param2>value2</param2>" +
    		"<param3>value3</param3>" +
    		"</OverseeMusic>";
    	
    	// start the case with the input parameters
    	_idForTopNet = _engine.startCase(null, spec.getID(), data, null);
    	YNetRunner topNetRunner = _workItemRepository.getNetRunner(_idForTopNet);
    	
    	YCaseData caseData = topNetRunner.getCasedata();
    	String xml = caseData.getData();
    	
    	SAXBuilder builder = new SAXBuilder();
		Document d = null;
        try {
            d = builder.build( new StringReader( xml ) );
        }
        catch(IOException exc) {
        	exc.printStackTrace();
        	fail(exc.toString());
        }
		catch( JDOMException exc ) {
			exc.printStackTrace();
			fail(exc.toString());
		}
		
		Element root = d.getRootElement();
		
		Map<String, String> dataValues = new HashMap<String, String>();
		
		for( int index = 0; index < root.getContentSize(); index++ ) {
			Element e = (Element) root.getContent( index );
			dataValues.put( e.getName(), e.getContent( 0 ).getValue() );
		}
		
		assertTrue( "" + dataValues.size(), dataValues.size() == 3 );
		assertTrue( dataValues.get( "aParam" ), "value".equals( dataValues.get( "aParam" ) ) );
		assertTrue( dataValues.get( "param2" ), "value2".equals( dataValues.get( "param2" ) ) );
		assertTrue( dataValues.get( "param3" ), "value3".equals( dataValues.get( "param3" ) ) );
    }
    
    /**
     * Tests starting a case that requires input parameters where the input data is malformed.
     */
    public void testInputParameters1b() throws JDOMException, IOException,
			YPersistenceException, YStateException, YSchemaBuildingException, YDataStateException {
		// variables
		Set<YWorkItem> workItems;
		Iterator<YWorkItem> iter;
		YWorkItem item;
		List<YNetRunner> netRunners = new Vector<YNetRunner>();
		List<YIdentifier> ids = new LinkedList<YIdentifier>();
		String data;
		
		// load the specification
		YSpecification spec = readSpecification( _spec1File );
		
		// create a malformed data XML string
		data = "<OverseeMusic>" +
			"<aParam>value</aParam>" +
			"<param2>value2<param2>" +
			"<param3>value3</param3>" +
			"</OverseeMusic>";
		
		// start the case with the input parameters
		try {
			_idForTopNet = _engine.startCase(null, spec.getID(), data, null);
			fail( "An exception should have been thrown." );
		}
		catch( YStateException e ) {
			// proper exception was thrown
		}
    }
    
    /**
     * Tests starting a case that requires input parameters where the root element of the
     * input data is &lt;data&gt; instead of &lt;<i>DecompositionID</i>&gt; (in this case
     * it should be &lt;OverseeMusic&gt;).
     */
    public void testInputParameters1c() throws JDOMException, IOException,
			YPersistenceException, YStateException, YSchemaBuildingException, YDataStateException {
		// variables
		Set<YWorkItem> workItems;
		Iterator<YWorkItem> iter;
		YWorkItem item;
		List<YNetRunner> netRunners = new Vector<YNetRunner>();
		List<YIdentifier> ids = new LinkedList<YIdentifier>();
		String data;
		
		// load the specification
		YSpecification spec = readSpecification( _spec1File );
		
		// create a malformed data XML string
		data = "<data>" +
			"<aParam>value</aParam>" +
			"<param2>value2</param2>" +
			"<param3>value3</param3>" +
			"</data>";
		
		// start the case with the input parameters
		try {
			_idForTopNet = _engine.startCase(null, spec.getID(), data, null);
			fail( "An exception should have been thrown." );
		}
		catch( YDataValidationException e ) {
			// proper exception was thrown
		}
	}
    
    /**
     * Tests starting a case that requires input parameters but a mandatory parameter is left out.
     */
    public void testInputParameters1d() throws JDOMException, IOException,
			YPersistenceException, YStateException, YSchemaBuildingException, YDataStateException {
		// variables
		Set<YWorkItem> workItems;
		Iterator<YWorkItem> iter;
		YWorkItem item;
		List<YNetRunner> netRunners = new Vector<YNetRunner>();
		List<YIdentifier> ids = new LinkedList<YIdentifier>();
		String data;
		
		// load the specification
		YSpecification spec = readSpecification( _spec1File );
		
		// create a proper data XML string
		data = "<OverseeMusic>" +
			"<aParam>value</aParam>" +
			"<param3>value3</param3>" +
			"</OverseeMusic>";
		
		// start the case with the input parameters
		try {
			_idForTopNet = _engine.startCase(null, spec.getID(), data, null);
			fail( "An exception should have been thrown." );
		}
		catch( IllegalArgumentException e ) {
			// proper exception was thrown.
		}
	}
    
    /**
     * Tests starting a case with input parameters where an extra parameter is given.
     */
    public void testInputParameters1e() throws JDOMException, IOException,
    		YPersistenceException, YStateException, YSchemaBuildingException, YDataStateException {
		// variables
    	Set<YWorkItem> workItems;
    	Iterator<YWorkItem> iter;
    	YWorkItem item;
    	List<YNetRunner> netRunners = new Vector<YNetRunner>();
    	List<YIdentifier> ids = new LinkedList<YIdentifier>();
    	String data;
    	
    	// load the specification
    	YSpecification spec = readSpecification( _spec1File );
    	
    	// create a proper data XML string
    	data = "<OverseeMusic>" +
    		"<aParam>value</aParam>" +
    		"<param2>value2</param2>" +
    		"<param3>value3</param3>" +
    		"<param4>value4</param4>" +
    		"</OverseeMusic>";
    	
    	// start the case with the input parameters
    	try {
    		_idForTopNet = _engine.startCase(null, spec.getID(), data, null);
    		fail( "An exception should have been thrown." );
    	}
    	catch( YDataValidationException e ) {
    		// proper exception was thrown.
    	}
    }
    
    /**
     * Tests starting a case that has no input parameters and giving it input data.
     */
    public void testInputParameters2() throws JDOMException, IOException,
    		YPersistenceException, YStateException, YSchemaBuildingException, YDataStateException {
		// variables
    	Set<YWorkItem> workItems;
    	Iterator<YWorkItem> iter;
    	YWorkItem item;
    	List<YNetRunner> netRunners = new Vector<YNetRunner>();
    	List<YIdentifier> ids = new LinkedList<YIdentifier>();
    	String data;
    	
    	// load the specification
    	YSpecification spec = readSpecification( _spec2File );
    	
    	// create a data XML string
    	data = "<OverseeMusic>" +
    		"<aParam>value</aParam>" +
    		"<param2>value2</param2>" +
    		"<param3>value3</param3>" +
    		"</OverseeMusic>";
    	
    	// start the case with the input parameters
    	try {
    		_idForTopNet = _engine.startCase(null, spec.getID(), data, null);
    		fail( "An exception should have been thrown." );
    	}
    	catch( YDataValidationException e ) {
    		// proper exception was thrown.
    	}
    }
    
    /**
     * Tests starting a case that has no input parameters and giving it an empty input data element.
     */
    public void testInputParameters2b() throws JDOMException, IOException,
    		YPersistenceException, YStateException, YSchemaBuildingException, YDataStateException {
		// variables
    	Set<YWorkItem> workItems;
    	Iterator<YWorkItem> iter;
    	YWorkItem item;
    	List<YNetRunner> netRunners = new Vector<YNetRunner>();
    	List<YIdentifier> ids = new LinkedList<YIdentifier>();
    	String data;
    	
    	// load the specification
    	YSpecification spec = readSpecification( _spec2File );
    	
    	// create a data XML string
    	data = "<OverseeMusic>" +
    		"</OverseeMusic>";
    	
    	// start the case with the blank input element
    	_idForTopNet = _engine.startCase(null, spec.getID(), data, null);
    	YNetRunner topNetRunner = _workItemRepository.getNetRunner(_idForTopNet);
    	
    	YCaseData caseData = topNetRunner.getCasedata();
    	String xml = caseData.getData();
    	
    	SAXBuilder builder = new SAXBuilder();
		Document d = null;
        try {
            d = builder.build( new StringReader( xml ) );
        }
        catch(IOException exc) {
        	exc.printStackTrace();
        	fail(exc.toString());
        }
		catch( JDOMException exc ) {
			exc.printStackTrace();
			fail(exc.toString());
		}
		
		Element root = d.getRootElement();
		
		Map<String, String> dataValues = new HashMap<String, String>();
		
		for( int index = 0; index < root.getContentSize(); index++ ) {
			Element e = (Element) root.getContent( index );
			dataValues.put( e.getName(), e.getContent( 0 ).getValue() );
		}
		
		assertTrue( "" + dataValues.size(), dataValues.size() == 0 );
    }
    
    public static void main(String args[]) {
    	TestInputParameters test = new TestInputParameters("");
    	try {
    		test.setUp();
    		test.testInputParameters1();
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
        suite.addTestSuite(TestInputParameters.class);
        return suite;
    }
}
