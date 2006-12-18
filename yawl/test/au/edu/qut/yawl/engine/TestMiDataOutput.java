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
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.jdom.Element;
import org.jdom.JDOMException;

import au.edu.qut.yawl.elements.YAtomicTask;
import au.edu.qut.yawl.elements.YNet;
import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.elements.data.YVariable;
import au.edu.qut.yawl.elements.state.YIdentifier;
import au.edu.qut.yawl.engine.domain.YWorkItem;
import au.edu.qut.yawl.engine.domain.YWorkItemRepository;
import au.edu.qut.yawl.exceptions.YDataQueryException;
import au.edu.qut.yawl.exceptions.YDataStateException;
import au.edu.qut.yawl.exceptions.YPersistenceException;
import au.edu.qut.yawl.exceptions.YQueryException;
import au.edu.qut.yawl.exceptions.YSchemaBuildingException;
import au.edu.qut.yawl.exceptions.YStateException;
import au.edu.qut.yawl.exceptions.YSyntaxException;
import au.edu.qut.yawl.util.YMessagePrinter;
import au.edu.qut.yawl.util.YVerificationMessage;

/**
 * Checks that a multiple instance atomic task can do data transfer.
 * 
 * @author Nathan Rose
 */
public class TestMiDataOutput extends TestCase {
	private YIdentifier _idForTopNet;
    private YWorkItemRepository _workItemRepository = YWorkItemRepository.getInstance();
    private static final int SLEEP_TIME = 100;
    private AbstractEngine _engine;
    private File _spec1File;
    private File _spec2File;
    private File _spec3File;
//    private YSpecification _specification;
//    private YSpecification _specification2;
//    private YSpecification _specification3;

    public TestMiDataOutput(String name) {
        super(name);
    }

    public void setUp() throws YSchemaBuildingException, YSyntaxException, YPersistenceException,
    		JDOMException, IOException {
        URL fileURL = getClass().getResource("TestMiDataOutput.xml");
        _spec1File = new File( fileURL.getFile() );
//        File yawlXMLFile = new File(fileURL.getFile());
//        _specification = null;
//        _specification = (YSpecification)
//        	YMarshal.unmarshalSpecifications(yawlXMLFile.getAbsolutePath()).get(0);
        fileURL = getClass().getResource("TestMiDataOutput2.xml");
        _spec2File = new File( fileURL.getFile() );
//        yawlXMLFile = new File(fileURL.getFile());
//        _specification2 = (YSpecification)
//        	YMarshal.unmarshalSpecifications(yawlXMLFile.getAbsolutePath()).get(0);
        fileURL = getClass().getResource("TestMiDataOutput3.xml");
        _spec3File = new File( fileURL.getFile() );
//        yawlXMLFile = new File(fileURL.getFile());
//        _specification3 = (YSpecification)
//        	YMarshal.unmarshalSpecifications(yawlXMLFile.getAbsolutePath()).get(0);
        _engine =  EngineFactory.createYEngine();
        EngineClearer.clear(_engine);
    }
    
    private static void sleep( long millis ) {
    	try {
    		Thread.sleep( millis );
    	}
    	catch( InterruptedException e ) {
    	}
    }
    
    public void testRunMultiInstance() {
    	try {
    		YSpecification spec = readSpecification( _spec1File );
    		runMultiInstance( spec );
    	}
    	catch( Exception e ) {
    		StringWriter sw = new StringWriter();
    		sw.write( e.toString() + "\n" );
    		e.printStackTrace(new PrintWriter(sw));
    		fail( sw.toString() );
    	}
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
    
    private void runMultiInstance(YSpecification specification) throws YStateException,
			YSchemaBuildingException, YDataStateException, YPersistenceException, YQueryException {
		// variables
    	Set<YWorkItem> workItems;
    	Iterator<YWorkItem> iter;
    	YWorkItem item;
    	List<YNetRunner> netRunners = new Vector<YNetRunner>();
    	List<YIdentifier> ids = new LinkedList<YIdentifier>();
    	
    	// start the specification (assuming it's already loaded)
    	_idForTopNet = _engine.startCase(null, specification.getID(), null, null);
    	YNetRunner topNetRunner = _workItemRepository.getNetRunner(_idForTopNet);
    	
    	// make sure there's 1 enabled item to start
    	workItems = _workItemRepository.getEnabledWorkItems();
    	assertTrue( workItems.size() == 1 );
    	
    	// make sure the work item is the correct one, and start it
    	item = workItems.iterator().next();
    	assertTrue( item.getTaskID(), item.getTaskID().equals( "record" ) );
    	_engine.startWorkItem( item, "admin" );
    	
    	sleep( SLEEP_TIME );
    	
    	// starting the work item causes children to be created....
    	workItems = item.getChildren();
    	iter = workItems.iterator();
    	
    	while( iter.hasNext() ) {
    		item = iter.next();
    		ids.add(item.getCaseID());
    		// one of the children will have a status of executing, but the others
    		// will only have a status of firing...
    		if( item.getStatus() != YWorkItem.Status.Executing ) {
    			// so start executing those that aren't already executing
    			_engine.startWorkItem( item, item.getDataString() );
    			sleep( SLEEP_TIME );
    		}
    		// and finish each one after it executes
    		_engine.completeWorkItem( item, item.getDataString(), false );
    	}
    	
    	workItems = _workItemRepository.getEnabledWorkItems();
    	assertTrue( "workItems.size()" + workItems.size(), workItems.size() == 1 );
    	
    	workItems = _workItemRepository.getExecutingWorkItems();
    	assertTrue( "workItems.size()" + workItems.size(), workItems.size() == 0 );
    	
    	workItems = _workItemRepository.getCompletedWorkItems();
    	assertTrue( "workItems.size()" + workItems.size(), workItems.size() == 0 );
    	
    	Set<Character> chars = new HashSet<Character>();
    	
    	for(YIdentifier id : ids) {
    		Element element = ((YAtomicTask)((YNet)specification.getDecomposition("OverseeMusic")).getNetElement("record")).getData(id);
    		System.out.println();
    		printxml(element);
    		// root is <Prepare>
    		assertNotNull(id.getId(), element);
    		assertNotNull(element.getChildren());
    		assertTrue(element.getChildren().size() > 0);
    		
    		// get child element <songLocal>
    		element = (Element)element.getChildren().get(0);
    		assertNotNull(element);
    		assertNotNull(element.getChildren());
    		assertTrue(element.getChildren().size() > 0);
    		
    		// get child element <songName>
    		element = (Element)element.getChildren().get(0);
    		assertNotNull(element);
    		assertNotNull(element.getText());
    		assertTrue(element.getText().length() == 6);
    		
    		// keep track of which song numbers we've found
    		chars.add(Character.valueOf(element.getText().toCharArray()[5]));
    	}
    	
    	// make sure all songs were found
    	assertTrue(chars.contains(Character.valueOf('1')));
    	assertTrue(chars.contains(Character.valueOf('2')));
    	assertTrue(chars.contains(Character.valueOf('3')));
    	assertTrue(chars.contains(Character.valueOf('4')));
    	// and that only those songs were found
    	assertTrue(chars.size() == 4);
    	
    	Element element = topNetRunner._net.getInternalDataDocument().getRootElement();
    	for(YVariable var : topNetRunner._net.getLocalVariables())
    		System.out.println(var.getName() + " : " + var.getElementName() + " : " + var.getDocumentation());
    	System.out.println();
    	printxml(element);
    	assertNotNull(element);
    	assertNotNull(element.getChildren());
    	// root should have two variables
    	assertTrue(element.getChildren().size() == 2);
    	
    	// first variable should be songlist
    	String name = ((Element)element.getChildren().get(0)).getName();
    	assertTrue(name, name.equals("songlist"));
    	
    	// second should be songList_output
    	element = (Element) element.getChildren().get(1);
    	assertTrue(element.getName(), element.getName().equals("songList_output"));
    	
    	assertTrue("" + element.getChildren().size(), element.getChildren().size() == 4);
    	chars.clear();
    	for(Element e : (List<Element>) element.getChildren()) {
    		assertNotNull(e);
    		assertNotNull(e.getText());
    		assertTrue(e.getText().length() == 6);
    		
    		// keep track of which song numbers we've found
    		chars.add(Character.valueOf(e.getText().toCharArray()[5]));
    	}

    	// make sure all songs were found
    	assertTrue(chars.contains(Character.valueOf('1')));
    	assertTrue(chars.contains(Character.valueOf('2')));
    	assertTrue(chars.contains(Character.valueOf('3')));
    	assertTrue(chars.contains(Character.valueOf('4')));
    	// and that only those songs were found
    	assertTrue(chars.size() == 4);
	}
    
    /**
     * This spec fails in beta2 because the root node of the data is <tt>&lt;data&gt;</tt>,
     * not <tt>&lt;Prepare&gt;</tt> (the ID of the web service gateway) and no data is
     * returned by the query:<br>
     * <tt>for $d in /Prepare/songLocal return
     * &lt;song&gt;{$d/songName}{$d/songSpecification}&lt;/song&gt;</tt>
     */
    public void testRunMultiInstanceBeta2() {
    	try {
//    		_specification.setBetaVersion(YSpecification._Beta2);
    		YSpecification spec = readSpecification( _spec1File );
    		spec.setBetaVersion(YSpecification._Beta2);
        	runMultiInstance( spec );
//    		testRunMultiInstance();
    		fail("The proper exception was not thrown.");
    	}
    	catch(YDataQueryException e) {
    		// the proper exception was thrown.
//    		System.out.println(e);
//    		System.out.println("query:" + e.getQueryString());
//    		System.out.println("data:");
//    		printxml(e.getData());
//    		System.out.println("source:");
//    		System.out.println(e.getSource());
//    		System.out.println(e);
//    		e.printStackTrace();
    	}
    	catch( Exception e ) {
    		StringWriter sw = new StringWriter();
    		sw.write( e.toString() + "\n" );
    		e.printStackTrace(new PrintWriter(sw));
    		fail( sw.toString() );
    	}
    }
    
    public void testRunMultiInstance2Beta2() {
    	try {
	    	YSpecification spec = readSpecification( _spec2File );
	    	spec.setBetaVersion( YSpecification._Beta2 );
	    	runMultiInstance2( spec );
    	}
    	catch( Exception e ) {
    		StringWriter sw = new StringWriter();
    		sw.write( e.toString() + "\n" );
    		e.printStackTrace(new PrintWriter(sw));
    		fail( sw.toString() );
    	}
    }
    
    public void testRunMultiInstance2() {
    	try {
    		YSpecification spec = readSpecification( _spec2File );
    		runMultiInstance2( spec );
    	}
    	catch( Exception e ) {
    		StringWriter sw = new StringWriter();
    		sw.write( e.toString() + "\n" );
    		e.printStackTrace(new PrintWriter(sw));
    		fail( sw.toString() );
    	}
    }
    
    private void runMultiInstance2(YSpecification specification2) {
    	try {
			// variables
			Set<YWorkItem> workItems;
			Iterator<YWorkItem> iter;
			YWorkItem item;
			List<YIdentifier> ids = new LinkedList<YIdentifier>();

			// start the spec (assuming it's already loaded)
			_idForTopNet = _engine.startCase( null, specification2.getID(), null, null );
			YNetRunner topNetRunner = _workItemRepository.getNetRunner( _idForTopNet );

			// make sure there's 1 enabled item to start
			workItems = _workItemRepository.getEnabledWorkItems();
			assertTrue( workItems.size() == 1 );

			// make sure the work item is the correct one, and start it
			item = workItems.iterator().next();
			assertTrue( item.getTaskID(), item.getTaskID().equals( "record" ) );
			_engine.startWorkItem( item, "admin" );

			sleep( SLEEP_TIME );

			// starting the work item causes children to be created....
			workItems = item.getChildren();
			iter = workItems.iterator();

			while( iter.hasNext() ) {
				item = iter.next();
				ids.add( item.getCaseID() );
				// one of the children will have a status of executing, but the others
				// will only have a status of firing...
				if( !item.getStatus().equals( YWorkItem.Status.Executing ) ) {
					// so start executing those that aren't already executing
					_engine.startWorkItem( item, item.getDataString() );
					sleep( SLEEP_TIME );
				}
				// and finish each one after it executes
				_engine.completeWorkItem( item, item.getDataString(), false );
			}
			fail("An exception should have been thrown");
		}
    	catch(YDataStateException e) {
    		// the proper exception was thrown
    	}
    	catch( Exception e ) {
    		StringWriter sw = new StringWriter();
    		sw.write( e.toString() + "\n" );
    		e.printStackTrace(new PrintWriter(sw));
    		fail( sw.toString() );
    	}
    }
    
    public void testRunMultiInstance3() {
    	try {
    		YSpecification spec = readSpecification( _spec3File );
    		runMultiInstance( spec );
    	}
    	catch( Exception e ) {
    		StringWriter sw = new StringWriter();
    		sw.write( e.toString() + "\n" );
    		e.printStackTrace(new PrintWriter(sw));
    		fail( sw.toString() );
    	}
    }
    
    public void testRunMultiInstance3Beta2() {
    	try {
    		YSpecification spec = readSpecification( _spec3File );
    		spec.setBetaVersion( YSpecification._Beta2 );
//    		_specification3.setBetaVersion( YSpecification._Beta2 );
//    	try {
    		runMultiInstance( spec );
    		fail("An exception should have been thrown");
    	}
    	catch(YDataQueryException e) {
    		// proper exception thrown
    	}
    	catch( Exception e ) {
    		StringWriter sw = new StringWriter();
    		sw.write( e.toString() + "\n" );
    		e.printStackTrace(new PrintWriter(sw));
    		fail( sw.toString() );
    	}
    }
    
    public void testRunMultiInstance3BadQuery1() {
    	try {
    		YSpecification spec = readSpecification( _spec3File );
//    		YAtomicTask task = (YAtomicTask) _specification3._rootNet.getNetElement("record");
    		YAtomicTask task = (YAtomicTask) spec.getRootNet().getNetElement("record");
    		task.getMultiInstanceAttributes().setMinInstancesQuery("if ( count(/OverseeMusic/songlist/*) &gt; 0) then ( count(/OverseeMusic/songlist/*) ) else 1");
//    	try {
    		runMultiInstance( spec );
    		fail("An exception should have been thrown");
    	}
    	catch(YQueryException e) {
    		// proper exception thrown
    	}
    	catch( Exception e ) {
    		StringWriter sw = new StringWriter();
    		sw.write( e.toString() + "\n" );
    		e.printStackTrace(new PrintWriter(sw));
    		fail( sw.toString() );
    	}
    }
    
    public void testRunMultiInstance3BadQuery1Beta2() {
    	try {
    		YSpecification spec = readSpecification( _spec3File );
//    		YAtomicTask task = (YAtomicTask) _specification3._rootNet.getNetElement("record");
    		YAtomicTask task = (YAtomicTask) spec.getRootNet().getNetElement("record");
    		task.getMultiInstanceAttributes().setMinInstancesQuery("if ( count(/OverseeMusic/songlist/*) &gt; 0) then ( count(/OverseeMusic/songlist/*) ) else 1");
    		spec.setBetaVersion( YSpecification._Beta2 );
//    	try {
    		runMultiInstance( spec );
    		fail("An exception should have been thrown");
    	}
    	catch(YQueryException e) {
    		// proper exception thrown
    	}
    	catch( Exception e ) {
    		StringWriter sw = new StringWriter();
    		sw.write( e.toString() + "\n" );
    		e.printStackTrace(new PrintWriter(sw));
    		fail( sw.toString() );
    	}
    }
    
    public void testRunMultiInstance3BadQuery2() {
    	try {
    		YSpecification spec = readSpecification( _spec3File );
//    		YAtomicTask task = (YAtomicTask) _specification3._rootNet.getNetElement("record");
    		YAtomicTask task = (YAtomicTask) spec.getRootNet().getNetElement("record");
    		task.getMultiInstanceAttributes().setMaxInstancesQuery("if ( count(/OverseeMusic/songlist/*) &gt; 0) then ( count(/OverseeMusic/songlist/*) ) else 10");
//    	try {
    		runMultiInstance( spec );
    		fail("An exception should have been thrown");
    	}
    	catch(YQueryException e) {
    		// proper exception thrown
    	}
    	catch( Exception e ) {
    		StringWriter sw = new StringWriter();
    		sw.write( e.toString() + "\n" );
    		e.printStackTrace(new PrintWriter(sw));
    		fail( sw.toString() );
    	}
    }
    
    public void testRunMultiInstance3BadQuery2Beta2() {
    	try {
    		YSpecification spec = readSpecification( _spec3File );
//    		YAtomicTask task = (YAtomicTask) _specification3._rootNet.getNetElement("record");
    		YAtomicTask task = (YAtomicTask) spec.getRootNet().getNetElement("record");
    		task.getMultiInstanceAttributes().setMaxInstancesQuery("if ( count(/OverseeMusic/songlist/*) &gt; 0) then ( count(/OverseeMusic/songlist/*) ) else 10");
    		spec.setBetaVersion( YSpecification._Beta2 );
//    	try {
    		runMultiInstance( spec );
    		fail("An exception should have been thrown");
    	}
    	catch(YQueryException e) {
    		// proper exception thrown
    	}
    	catch( Exception e ) {
    		StringWriter sw = new StringWriter();
    		sw.write( e.toString() + "\n" );
    		e.printStackTrace(new PrintWriter(sw));
    		fail( sw.toString() );
    	}
    }
    
    public void testRunMultiInstance3BadQuery3() {
    	try {
    		YSpecification spec = readSpecification( _spec3File );
//    		YAtomicTask task = (YAtomicTask) _specification3._rootNet.getNetElement("record");
    		YAtomicTask task = (YAtomicTask) spec.getRootNet().getNetElement("record");
    		task.getMultiInstanceAttributes().setThresholdQuery("if ( count(/OverseeMusic/songlist/*) &gt; 0) then ( count(/OverseeMusic/songlist/*) ) else 4");
//    	try {
    		runMultiInstance( spec );
    		fail("An exception should have been thrown");
    	}
    	catch(YStateException e) {
    		// proper exception thrown
    	}
    	catch( Exception e ) {
    		StringWriter sw = new StringWriter();
    		sw.write( e.toString() + "\n" );
    		e.printStackTrace(new PrintWriter(sw));
    		fail( sw.toString() );
    	}
    }
    
    public void testRunMultiInstance3BadQuery3Beta2() {
    	try {
    		YSpecification spec = readSpecification( _spec3File );
//    		YAtomicTask task = (YAtomicTask) _specification3._rootNet.getNetElement("record");
    		YAtomicTask task = (YAtomicTask) spec.getRootNet().getNetElement("record");
    		task.getMultiInstanceAttributes().setThresholdQuery("if ( count(/OverseeMusic/songlist/*) &gt; 0) then ( count(/OverseeMusic/songlist/*) ) else 4");
    		spec.setBetaVersion( YSpecification._Beta2 );
//    	try {
    		runMultiInstance( spec );
    		fail("An exception should have been thrown");
    	}
    	catch(YDataQueryException e) {
    		// proper exception thrown
    	}
    	catch( Exception e ) {
    		StringWriter sw = new StringWriter();
    		sw.write( e.toString() + "\n" );
    		e.printStackTrace(new PrintWriter(sw));
    		fail( sw.toString() );
    	}
    }
    
    private static void printxml(Element element) {
    	if(element == null)
    	{
    		System.out.println("null");
    		return;
    	}
    	System.out.println("<" + element.getName() + ">" + element.getTextNormalize());
    	for(Element child : (List<Element>)element.getChildren())
    		printxml(child);
    	System.out.println("</" + element.getName() + ">");
    }
    
    public static void main(String args[]) {
    	TestMiDataOutput test = new TestMiDataOutput("");
    	try {
    		test.setUp();
    		test.testRunMultiInstance2();
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
        suite.addTestSuite(TestMiDataOutput.class);
        return suite;
    }
}
