/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.elements;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.jdom.Document;
import org.jdom.Element;

import au.edu.qut.yawl.elements.data.YParameter;
import au.edu.qut.yawl.elements.data.YVariable;
import au.edu.qut.yawl.elements.state.YIdentifier;
import au.edu.qut.yawl.engine.AbstractEngine;
import au.edu.qut.yawl.engine.EngineClearer;
import au.edu.qut.yawl.engine.EngineFactory;
import au.edu.qut.yawl.engine.TestYNetRunner;
import au.edu.qut.yawl.engine.YNetRunner;
import au.edu.qut.yawl.engine.domain.YCaseData;
import au.edu.qut.yawl.engine.domain.YWorkItem;
import au.edu.qut.yawl.engine.domain.YWorkItemID;
import au.edu.qut.yawl.engine.domain.YWorkItemRepository;
import au.edu.qut.yawl.exceptions.YDataStateException;
import au.edu.qut.yawl.exceptions.YPersistenceException;
import au.edu.qut.yawl.exceptions.YQueryException;
import au.edu.qut.yawl.exceptions.YSchemaBuildingException;
import au.edu.qut.yawl.exceptions.YStateException;
import au.edu.qut.yawl.exceptions.YSyntaxException;
import au.edu.qut.yawl.unmarshal.YMarshal;
import au.edu.qut.yawl.util.SpecReader;
import au.edu.qut.yawl.util.YMessagePrinter;
import au.edu.qut.yawl.util.YVerificationMessage;


/**
 * 
 * Author: Lachlan Aldred
 * Date: 28/04/2003
 * Time: 11:12:39
 * 
 */
public class TestYAtomicTask extends TestCase {
	private YAtomicTask _atomicTask1;
    private YDecomposition _ydecomp;
    private String _activityID = "Activity1";
    private YAtomicTask _atomicTask2;
    private YCondition _c1;

    /**
	 * Constructor for TaskTest
	 */
	public TestYAtomicTask(String name) {
		super(name);
	}


    public void setUp() throws YPersistenceException {
        YSpecification spec = new YSpecification("");
        spec.setBetaVersion(YSpecification._Beta2);
        YNet deadNet = new YNet("aNet", spec);
        YVariable v = new YVariable(null);
        v.setName("stubList");
        v.setUntyped(true);
        v.setInitialValue("<stub/><stub/><stub/>");
        deadNet.setLocalVariable(v);
        YCaseData casedata = new YCaseData();
        deadNet.initializeDataStore(casedata);
        deadNet.initialise();
        _atomicTask1 = new YAtomicTask("AT1", YAtomicTask._AND, YAtomicTask._AND, deadNet);
        _atomicTask1.setDecompositionPrototype(new YAWLServiceGateway(_activityID, spec));
        _atomicTask1.setUpMultipleInstanceAttributes(
                "3", "5", "3", YMultiInstanceAttributes._creationModeStatic);
        Map map = new HashMap();
        map.put("stub","/data/stubList");
        _atomicTask1.setMultiInstanceInputDataMappings("stub", "for $d in /stubList/* return $d");
        _atomicTask1.setDataMappingsForTaskStarting(map);
        _ydecomp = _atomicTask1.getDecompositionPrototype();
        YParameter p = new YParameter(null, YParameter._INPUT_PARAM_TYPE);
        p.setName("stub");
        p.setUntyped(true);
        _ydecomp.setInputParam(p);
        YAtomicTask before = new YAtomicTask("before", YAtomicTask._OR, YAtomicTask._AND, deadNet);
        YAtomicTask after = new YAtomicTask("after", YAtomicTask._OR, YAtomicTask._AND, deadNet);
        _c1 = new YCondition("c1", "c1", deadNet);
        YFlow f = new YFlow(_c1, _atomicTask1);
        _atomicTask1.setPreset(f);
        f = new YFlow(_atomicTask1, _c1);
        _atomicTask1.setPostset(f);
        _atomicTask2 = new YAtomicTask("at2", YAtomicTask._AND, YAtomicTask._AND, deadNet);
        f = new YFlow(before, _atomicTask2);
        _atomicTask2.setPreset(f);
        f = new YFlow(_atomicTask2, after);
        _atomicTask2.setPostset(f);
    }


    public void testFullAtomicTask() {
        List messages = _atomicTask1.verify();
        if(messages.size() > 0) {
            YMessagePrinter.printMessages(messages);
            System.out.println("(YVerificationMessage)messages.get(0)).class.getName( = " + messages.get(0).getClass().getName());
            fail(((YVerificationMessage)messages.get(0)).getMessage() + " " + messages.size());
        }
    }


    public void testEmptyAtomicTask() {
        List messages = _atomicTask2.verify();
        if(messages.size() > 0) {
            YMessagePrinter.printMessages(messages);
            fail(((YVerificationMessage)messages.get(0)).getMessage() + " num-messages: " + messages.size());
        }
    }


    public void testFireAtomicTask() throws YStateException, YDataStateException, YQueryException, YSchemaBuildingException, YPersistenceException {
    	YIdentifier id1 = new YIdentifier();
    	YIdentifier.saveIdentifier( id1 );
        _c1.add(id1);
        List l = null;
        l = _atomicTask1.t_fire();

        Iterator i = l.iterator();
        while(i.hasNext() && _atomicTask1.t_isBusy()){
            YIdentifier id = (YIdentifier) i.next();
            _atomicTask1.t_start(id);
            Document d = new Document(new Element("data"));
            _atomicTask1.t_complete(id, d);
        }
        assertFalse(_atomicTask1.t_isBusy());
    }
    
    public void testTaskBusyBeforeRunning() {
    	if(_atomicTask1.t_isBusy())
    		fail( "Task is busy before any instances have been created" );
    }
    
    public void testCancelTaskWhileRunning() throws YPersistenceException, YStateException, YDataStateException, YQueryException, YSchemaBuildingException {
    	_atomicTask1.getParent().getParent().setBetaVersion(YSpecification._Beta7_1);
    	
    	YIdentifier id1 = new YIdentifier();
    	YIdentifier.saveIdentifier( id1 );
        _c1.add(id1);
        List l = null;
        l = _atomicTask1.t_fire();
        
        Iterator i = l.iterator();
        while(i.hasNext() && _atomicTask1.t_isBusy()){
            YIdentifier id = (YIdentifier) i.next();
            _atomicTask1.t_start(id);
            _atomicTask1.cancel();
        }
        // for branch coverage, call cancel again (even though it's already cancelled)
        _atomicTask1.cancel();
        assertFalse(_atomicTask1.t_isBusy());
    }
    
    /**
     * First version of this test cancels a task that's in the work item repository
     */
    public void testCancelTaskInWorkItemRepositoryA() throws YPersistenceException, YStateException, YDataStateException, YQueryException, YSchemaBuildingException {
    	_atomicTask1.getParent().getParent().setBetaVersion(YSpecification._Beta7_1);
    	
    	YIdentifier id = new YIdentifier();
    	YIdentifier.saveIdentifier( id );
    	YWorkItemRepository repos = YWorkItemRepository.getInstance();
    	repos.clear();
    	
    	YWorkItem item = new YWorkItem(_atomicTask1.getParent().getParent().getID(),
    			new YWorkItemID(id, _atomicTask1.getID()), false, false);
    	
    	_c1.add(id);
        List l = null;
        l = _atomicTask1.t_fire();
        
        Iterator i = l.iterator();
        while(i.hasNext() && _atomicTask1.t_isBusy()){
            YIdentifier yid = (YIdentifier) i.next();
            _atomicTask1.t_start(yid);
            Document d = new Document(new Element("data"));
            _atomicTask1.cancel();
        }
        assertFalse(_atomicTask1.t_isBusy());
    }
    
    /**
     * Tests the cancellation of a task that's in the repository and that's tied to an actual YAWL service.
     * @throws Exception 
     */
    public void testCancelTaskInWorkItemRepositoryB() throws Exception {
    	String fileName = "TestCancelYawlServiceSpec.xml";
    	boolean isXmlFileInPackage = true;
    	
    	YSpecification spec = null;
    	
    	YNet root = null;
    	
		spec = SpecReader.readSpecification( fileName, isXmlFileInPackage, TestYAtomicTask.class );
		List<YDecomposition> decomps = spec.getDecompositions();
		
		System.out.println(decomps.size());
		for(int index = 0; index < decomps.size(); index++) {
			YDecomposition temp = decomps.get(index);
			if( "OverseeMusic".equals( temp.getId() ) ) {
				assert temp instanceof YNet : "decomposition 'OverseeMusic' should be a net!";
				root = (YNet) temp;
			}
		}
		
		if( root == null ) {
			fail( fileName + " should have a net called 'OverseeMusic'" );
		}
		
		YAtomicTask task = (YAtomicTask) root.getInputCondition().getPostsetElements().get(0);
		
		YIdentifier id = new YIdentifier();
		YIdentifier.saveIdentifier( id );
    	YWorkItemRepository repos = YWorkItemRepository.getInstance();
    	repos.clear();
    	
    	YWorkItem item = new YWorkItem(task.getParent().getParent().getID(),
    			new YWorkItemID(id, task.getID()), false, false);
    	
    	root.getInputCondition().add(id);
        List l = null;
        l = task.t_fire();
        
        Iterator i = l.iterator();
        while(i.hasNext() && task.t_isBusy()){
            YIdentifier yid = (YIdentifier) i.next();
            task.t_start(yid);
            Document d = new Document(new Element("data"));
            task.cancel();
        }
        assertFalse(task.t_isBusy());
    }
    
    /**
     * Used to test the prepareEnablementData() function in YAtomicTask.<br>
     * TODO need to test having it throw different kinds of exceptions
     * @throws Exception 
     */
    public void testEnablementMappingsSpec() throws Exception {
    	String fileName = "EnablementMappingsSpec.xml";
    	boolean isXmlFileInPackage = true;
    	
    	YSpecification spec = null;
    	YNet root = null;
    	List children = null;
    	
		spec = SpecReader.readSpecification( fileName, isXmlFileInPackage, TestYAtomicTask.class );
		spec.verify();
		List<YDecomposition> decomps = spec.getDecompositions();
		
		for(int index = 0; index < decomps.size(); index++) {
			YDecomposition temp = decomps.get(index);
			if( "Root".equals( temp.getId() ) ) {
				assert temp instanceof YNet : "decomposition 'Root' should be a net!";
				root = (YNet) temp;
			}
		}
		
		if( root == null ) {
			fail( fileName + " should have a net called 'Root'" );
		}
		
		AbstractEngine engine2 = EngineFactory.createYEngine();
        EngineClearer.clear(engine2);
        engine2.loadSpecification(spec);
        String idString = engine2.launchCase(null, spec.getID(), null, null);
        YNetRunner netRunner1 = TestYNetRunner.getYNetRunner(engine2, new YIdentifier(idString));
        Document d = new Document();
        d.setRootElement(new Element("data"));
		
		children = netRunner1.attemptToFireAtomicTask("SignOff_3");
		assertFalse(spec.getRootNet().getInputCondition().containsIdentifier());
		
		netRunner1.continueIfPossible();
    }
    
    /**
     * Tests converting the enablement mappings spec to XML.
     * @throws Exception 
     */
    public void testEnablementMappingsSpecXML() throws Exception {
    	String fileName = "EnablementMappingsSpec.xml";
    	boolean isXmlFileInPackage = true;
    	
    	YSpecification spec = null;
    	
		spec = SpecReader.readSpecification( fileName, isXmlFileInPackage, TestYAtomicTask.class );
		
		String xml = spec.toXML();
		
		xml = "<specificationSet xmlns=\"http://www.yawl.fit.qut.edu.au/\" " +
			"xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
			"version=\"Beta 7.1\" xsi:schemaLocation=\"http://www.yawl.fit.qut.edu.au/" +
			" d:/yawl/schema/YAWL_SchemaBeta7.1.xsd\">" + xml + "</specificationSet>";
		
		YSpecification spec2 = (YSpecification) YMarshal.unmarshalSpecifications(
				xml, "testEnablementMappingsSpecXML" ).get( 0 );
		
		// test some things to see if they're the same
		// (would be better if there was a .equals for YSpecification that tested
		// for example equality)
		assertTrue( spec.getDocumentation().equals( spec2.getDocumentation() ) );
		assertTrue( spec.getID().equals( spec2.getID() ) );
		assertTrue( spec.getDecompositions().size() == spec2.getDecompositions().size() );
		assertTrue( spec.getMetaData().toXML().equals( spec2.getMetaData().toXML() ) );
		assertTrue( spec.getRootNet().toXML().equals( spec2.getRootNet().toXML() ) );
		assertTrue( spec.toXML().equals( spec2.toXML() ) );
    }
    
    /**
     * Tests having enablement mappings with a task that decomposes to null (ie: doesn't
     * decompose to anything). Uses an XML file where the task does have a decomposition,
     * then modifies it in the java code.
     * @throws Exception 
     */
    public void testEnablementMappingsSpecB() throws Exception {
    	String fileName = "EnablementMappingsSpec.xml";
    	boolean isXmlFileInPackage = true;
    	
    	YSpecification spec = null;
    	YNet root = null;
    	List children = null;
    	
		spec = SpecReader.readSpecification( fileName, isXmlFileInPackage, TestYAtomicTask.class );
		spec.verify();
		List<YDecomposition> decomps = spec.getDecompositions();
		
		for(int index = 0; index < decomps.size(); index++) {
			YDecomposition temp = decomps.get(index);
			if( "Root".equals( temp.getId() ) ) {
				assert temp instanceof YNet : "decomposition 'Root' should be a net!";
				root = (YNet) temp;
			}
		}
		
		if( root == null ) {
			fail( fileName + " should have a net called 'Root'" );
		}
		
		YAtomicTask task = (YAtomicTask) root.getInputCondition().getPostsetElements().get(0);
		
		task.setDecompositionPrototype(null);
		
		AbstractEngine engine2 = EngineFactory.createYEngine();
        EngineClearer.clear(engine2);
        engine2.loadSpecification(spec);
        String idString = engine2.launchCase(null, spec.getID(), null, null);
        YNetRunner netRunner1 = TestYNetRunner.getYNetRunner(engine2, new YIdentifier(idString));
        Document d = new Document();
        d.setRootElement(new Element("data"));
		
		children = netRunner1.attemptToFireAtomicTask("SignOff_3");
		assertFalse(spec.getRootNet().getInputCondition().containsIdentifier());
		
		netRunner1.continueIfPossible();
    }
    
    /**
     * Tests enablement parameters that have an element name but no name. Compare
     * the xml file lines 41 through 44 with lines 45 through 50. Note that this
     * fails under the newer versions (when it checks the schema) but works with
     * older YAWL versions.
     * @throws Exception 
     */
    public void testEnablementMappingsSpecC() throws Exception {
    	String fileName = "EnablementMappingsSpecC.xml";
    	boolean isXmlFileInPackage = true;
    	
    	YSpecification spec = null;
    	YNet root = null;
    	List children = null;
    	
		spec = SpecReader.readSpecification( fileName, isXmlFileInPackage, TestYAtomicTask.class );
		spec.setBetaVersion(YSpecification._Beta2);
		spec.verify();
		List<YDecomposition> decomps = spec.getDecompositions();
		
		for(int index = 0; index < decomps.size(); index++) {
			YDecomposition temp = decomps.get(index);
			if( "Root".equals( temp.getId() ) ) {
				assert temp instanceof YNet : "decomposition 'Root' should be a net!";
				root = (YNet) temp;
			}
		}
		
		if( root == null ) {
			fail( fileName + " should have a net called 'Root'" );
		}
		
		AbstractEngine engine2 = EngineFactory.createYEngine();
        EngineClearer.clear(engine2);
        engine2.loadSpecification(spec);
        String idString = engine2.launchCase(null, spec.getID(), null, null);
        YNetRunner netRunner1 = TestYNetRunner.getYNetRunner(engine2, new YIdentifier(idString));
        Document d = new Document();
        d.setRootElement(new Element("data"));
		
		children = netRunner1.attemptToFireAtomicTask("SignOff_3");
		assertFalse("Spec input condition contains identifier",
				spec.getRootNet().getInputCondition().containsIdentifier());
		boolean contains = false;
		for( YExternalNetElement element : netRunner1.getBusyTasks() ) {
			if( element.getID().equals( "SignOff_3" ) ) {
				contains = true;
			}
		}
		assertTrue("Task 'SignOff_3' busy:" + netRunner1.getBusyTasks(), contains);
    }
    
    /**
     * Checks that a specification with a syntax error causes the verify function to
     * return verification errors, but not throw an exception. I don't remember
     * exactly what the syntax error was, but I believe it is on line 38 of the xml:<br>
     * <code>
     * &lt;element name="YawlResourceAllocationQuery"/&gt;
     * </code>
     * <br>which should be:<br>
     * <code>
     * &lt;name&gt;YawlResourceAllocationQuery&lt;/name&gt;
     * </code>
     */
    public void testEnablementMappingsSpecD() {
    	String fileName = "EnablementMappingsSpecD.xml";
    	boolean isXmlFileInPackage = true;
    	
    	YSpecification spec = null;
    	YNet root = null;
    	List children = null;
    	
    	try {
    		spec = SpecReader.readSpecification( fileName, isXmlFileInPackage, TestYAtomicTask.class );
    		fail( "Spec " + fileName + " was read successfully, but should have failed." );
    	}
    	catch(YSyntaxException e) {
    		// the proper exception was thrown
    	}
    	catch(Exception e) {
    		// some other exception was thrown, so it's a problem
    		StringWriter sw = new StringWriter();
    		sw.write( e.toString() + "\n" );
    		e.printStackTrace(new PrintWriter(sw));
    		fail( sw.toString() );
    	}
    }
    
    /**
     * Tests verification of a spec containing no root net, and where the enablement
     * mappings of a task and the enablement parameters of the task's decomposition
     * don't match up.
     * @throws Exception 
     */
    public void testEnablementMappingsSpecE() throws Exception {
    	String fileName = "EnablementMappingsSpecE.xml";
    	boolean isXmlFileInPackage = true;
    	
    	YSpecification spec = null;
    	YNet root = null;
    	List children = null;
    	
		spec = SpecReader.readSpecification( fileName, isXmlFileInPackage, TestYAtomicTask.class );
		List<YVerificationMessage> messages = spec.verify();
		
		boolean errorMsgExists = false;
		
		for( YVerificationMessage message : messages ) {
			if( YVerificationMessage.ERROR_STATUS.equals( message.getStatus() ) ) {
				errorMsgExists = true;
			}
		}
		
		assertTrue( "Spec validation should return an error message", errorMsgExists );
    }
    
    /**
     * Provides further branch coverage in YAtomicTask.prepareEnablementData
     * @throws Exception 
     */
    public void testEnablementMappingsSpecF() throws Exception {
    	String fileName = "EnablementMappingsSpecF.xml";
    	boolean isXmlFileInPackage = true;
    	
    	YSpecification spec = null;
    	YNet root = null;
    	List children = null;
    	
		spec = SpecReader.readSpecification( fileName, isXmlFileInPackage, TestYAtomicTask.class );
		spec.verify();
		List<YDecomposition> decomps = spec.getDecompositions();
		
		for(int index = 0; index < decomps.size(); index++) {
			YDecomposition temp = decomps.get(index);
			if( "Root".equals( temp.getId() ) ) {
				assert temp instanceof YNet : "decomposition 'Root' should be a net!";
				root = (YNet) temp;
			}
		}
		
		if( root == null ) {
			fail( fileName + " should have a net called 'Root'" );
		}
		
		AbstractEngine engine2 = EngineFactory.createYEngine();
        EngineClearer.clear(engine2);
        engine2.loadSpecification(spec);
        String idString = engine2.launchCase(null, spec.getID(), null, null);
        YNetRunner netRunner1 = TestYNetRunner.getYNetRunner(engine2, new YIdentifier(idString));
        Document d = new Document();
        d.setRootElement(new Element("data"));
		
		children = netRunner1.attemptToFireAtomicTask("SignOff_3");
		assertFalse(spec.getRootNet().getInputCondition().containsIdentifier());
		
		netRunner1.continueIfPossible();
    }
    
    /**
     * Tests converting the enablement mappings spec F to XML.
     * @throws Exception 
     */
    public void testEnablementMappingsSpecXML_F() throws Exception {
    	String fileName = "EnablementMappingsSpecF.xml";
    	boolean isXmlFileInPackage = true;
    	
    	YSpecification spec = null;
    	
		spec = SpecReader.readSpecification( fileName, isXmlFileInPackage, TestYAtomicTask.class );
		
		String xml = spec.toXML();
		
		xml = "<specificationSet xmlns=\"http://www.yawl.fit.qut.edu.au/\" " +
			"xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
			"version=\"Beta 7.1\" xsi:schemaLocation=\"http://www.yawl.fit.qut.edu.au/" +
			" d:/yawl/schema/YAWL_SchemaBeta7.1.xsd\">" + xml + "</specificationSet>";
		
		YSpecification spec2 = (YSpecification) YMarshal.unmarshalSpecifications(
				xml, "testEnablementMappingsSpecF_XML" ).get( 0 );
		
		// test some things to see if they're the same
		// (would be better if there was a .equals for YSpecification that tested
		// for example equality)
		assertTrue( spec.getDocumentation().equals( spec2.getDocumentation() ) );
		assertTrue( spec.getID().equals( spec2.getID() ) );
		assertTrue( spec.getDecompositions().size() == spec2.getDecompositions().size() );
		assertTrue( spec.getMetaData().toXML().equals( spec2.getMetaData().toXML() ) );
		assertTrue( spec.getRootNet().toXML().equals( spec2.getRootNet().toXML() ) );
		assertTrue( spec.toXML().equals( spec2.toXML() ) );
    }
    
    /**
     * Tests that an atomic task that does not have a decomposition causes the verify
     * function to return verification errors, but not throw any exceptions.
     * @throws Exception 
     */
    public void testTaskWithoutDecompositionSpec() throws Exception {
    	String fileName = "TaskWithoutDecompositionSpec.xml";
    	boolean isXmlFileInPackage = true;
    	
    	List<YVerificationMessage> messages = readAndVerifySpec( fileName, isXmlFileInPackage );
    	
    	// the specification should contain an error
    	boolean containsError = false;
    	for( YVerificationMessage message : messages ) {
    		if( YVerificationMessage.ERROR_STATUS.equals( message.getStatus() ) ) {
    			containsError = true;
    		}
    	}
    	
    	assertTrue( fileName + " verified OK, should have gotten an error", containsError );
    }
    
    /**
     * Tests that an atomic task that decomposes to a Net causes the verify function to
     * return verification errors, but not throw any exceptions.
     * @throws Exception 
     */
    public void testTaskDecomposesToNetSpec() throws Exception {
    	String fileName = "TaskDecomposesToNetSpec.xml";
    	boolean isXmlFileInPackage = true;
    	
    	YSpecification spec = null;
    	
    	YNet root = null;
    	YNet otherNet = null;
    	
    	List<YVerificationMessage> messages = new LinkedList<YVerificationMessage>();
    	
		spec = SpecReader.readSpecification( fileName, isXmlFileInPackage, TestYAtomicTask.class );
		List<YDecomposition> decomps = spec.getDecompositions();
		
		for(int index = 0; index < decomps.size(); index++) {
			YDecomposition temp = decomps.get(index);
			if( "OverseeMusic".equals( temp.getId() ) ) {
				assert temp instanceof YNet : "decomposition 'OverseeMusic' should be a net!";
				root = (YNet) temp;
			}
			else if( "leaf-c".equals( temp.getId() ) ) {
				assert temp instanceof YNet : "decomposition 'leaf-c' should be a net!";
				otherNet = (YNet) temp;
			}
		}
		
		if( root == null || otherNet == null ) {
			fail( fileName + " should have nets called 'OverseeMusic' and 'leaf-c'" + root + " " + otherNet );
		}
		
		YAtomicTask task = (YAtomicTask) root.getInputCondition().getPostsetElements().get(0);
		
		task.setDecompositionPrototype(otherNet);
		
		messages = verifySpec( spec, fileName );
    	
    	// the specification should contain an error
    	boolean containsError = false;
    	for( YVerificationMessage message : messages ) {
    		if( YVerificationMessage.ERROR_STATUS.equals( message.getStatus() ) ) {
    			containsError = true;
    		}
    	}
    	
    	assertTrue( fileName + " verified OK, should have gotten an error", containsError );
    }
    
    /**
     * Helper function that reads the specification with the given filename and
     * attempts to verify it and return the verification messages.
     * @throws Exception 
     */
    private List<YVerificationMessage> readAndVerifySpec( String fileName, boolean isXmlFileInPackage ) throws Exception {
    	List<YVerificationMessage> messages = new LinkedList<YVerificationMessage>();
    	
		YSpecification spec = SpecReader.readSpecification( fileName, isXmlFileInPackage, TestYAtomicTask.class );
		messages = verifySpec( spec, fileName );
    	
    	return messages;
    }
    
    /**
     * Helper function that attempts to verify the given specification and return the
     * verification messages.
     */
    private List<YVerificationMessage> verifySpec( YSpecification spec, String specName ) throws Exception {
    	List<YVerificationMessage> messages = new LinkedList<YVerificationMessage>();
    	
    	messages = spec.verify();
    	System.out.println( specName + " has " +
    			(( messages == null ) ? 0 : messages.size()) +
    			" verification messages:" );
    	for( int index = 0; messages != null && index < messages.size(); index++ ) {
    		System.out.println( (index + 1) + ":" + messages.get( index ).getMessage() );
    	}
    	
    	return messages;
    }
    
    /**
     * Makes stepping through a single test in the debugger easier.
     */
    public static void main(String[] args) {
    	try {
    		TestYAtomicTask test = new TestYAtomicTask("test1");
    		test.setUp();
    		test.testEnablementMappingsSpecF();
    		System.out.println("success");
    	}
    	catch( Throwable t ) {
    		System.err.println( t.toString() );
    		t.printStackTrace();
    	}
    }
}
