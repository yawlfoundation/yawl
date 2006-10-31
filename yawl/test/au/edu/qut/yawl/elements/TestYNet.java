/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.elements;

import au.edu.qut.yawl.elements.state.TestYMarking;
import au.edu.qut.yawl.elements.state.YIdentifier;
import au.edu.qut.yawl.elements.YCondition;
import au.edu.qut.yawl.elements.YTask;
import au.edu.qut.yawl.engine.TestEngineAgainstImproperCompletionOfASubnet;
import au.edu.qut.yawl.engine.YNetRunner;
import au.edu.qut.yawl.engine.domain.YWorkItemRepository;
import au.edu.qut.yawl.unmarshal.YMarshal;
import au.edu.qut.yawl.util.YMessagePrinter;
import au.edu.qut.yawl.util.YVerificationMessage;
import au.edu.qut.yawl.exceptions.YSyntaxException;
import au.edu.qut.yawl.exceptions.YSchemaBuildingException;
import au.edu.qut.yawl.exceptions.YPersistenceException;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jdom.JDOMException;

/**
 * @author aldredl
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class TestYNet extends TestCase {
    private YNet _goodNet;
    private YNet _badNet;
    private YNet _copy;
    private YNet _loopedNet;
    private YIdentifier _id1, _id2, _id3, _id4, _id5, _id6, _id7, _id8;
    private YSpecification _badSpecification;
    private YSpecification _weirdSpecification;


    /**
     * Constructor for NetElementTest.
     * @param name
     */
    public TestYNet(String name) {
        super(name);
    }


    public void setUp() throws YSchemaBuildingException, YSyntaxException, JDOMException, IOException, YPersistenceException {
        File file1 = new File(getClass().getResource("GoodNetSpecification.xml").getFile());
        File file2 = new File(getClass().getResource("BadNetSpecification.xml").getFile());
        YSpecification specification1 = null;

        specification1 = (YSpecification) YMarshal.unmarshalSpecifications(file1.getAbsolutePath()).get(0);


        _badSpecification = (YSpecification) YMarshal.unmarshalSpecifications(file2.getAbsolutePath()).get(0);

        _goodNet = specification1.getRootNet();
        _badNet = _badSpecification.getRootNet();
        _copy = null;
        _copy = (YNet) this._goodNet.clone();
        URL fileURL = TestYMarking.class.getResource("YAWLOrJoinTestSpecificationLongLoops.xml");
        File yawlXMLFile = new File(fileURL.getFile());
        YSpecification specification = null;
        specification = (YSpecification) YMarshal.
                        unmarshalSpecifications(yawlXMLFile.getAbsolutePath()).get(0);
        _loopedNet = specification.getRootNet();
        _id1 = new YIdentifier();
        _id2 = new YIdentifier();
        _id3 = new YIdentifier();
        _id4 = new YIdentifier();
        _id5 = new YIdentifier();
        _id6 = new YIdentifier();
        _id7 = new YIdentifier();
        _id8 = new YIdentifier();
        ((YCondition) _loopedNet.getNetElement("c{d_f}")).add(_id1);
        ((YCondition) _loopedNet.getNetElement("c{d_f}")).add(_id2);
        ((YCondition) _loopedNet.getNetElement("c{d_f}")).add(_id4);
        ((YCondition) _loopedNet.getNetElement("c{d_f}")).add(_id5);
        ((YCondition) _loopedNet.getNetElement("c{d_f}")).add(_id6);

        ((YCondition) _loopedNet.getNetElement("cC")).add(_id1);
        ((YCondition) _loopedNet.getNetElement("cC")).add(_id3);
        ((YCondition) _loopedNet.getNetElement("cC")).add(_id8);
        
        ((YCondition) _loopedNet.getNetElement("cA")).add(_id2);
        ((YCondition) _loopedNet.getNetElement("cA")).add(_id8);
        ((YCondition) _loopedNet.getNetElement("cA")).add(_id7);


        ((YCondition) _loopedNet.getNetElement("i-top")).add(_id4);
        ((YCondition) _loopedNet.getNetElement("c{b_w}")).add(_id5);
        ((YTask) _loopedNet.getNetElement("d")).setContainingIdentifier(_id6);


        ((YCondition) _loopedNet.getNetElement("cB")).add(_id7);
        ((YCondition) _loopedNet.getNetElement("cB")).add(_id8);
        
        ((YCondition) _loopedNet.getNetElement("c{q_f}")).add(_id7);
        ((YCondition) _loopedNet.getNetElement("c{q_f}")).add(_id8);
        
        
        YNetRunner netRunner = new YNetRunner();
        netRunner.setNet(_loopedNet);
        netRunner.setCaseID(_id1);
        netRunner.setId(new Long(1));
        YNetRunner.saveNetRunner(netRunner, null);
        netRunner = new YNetRunner();
        netRunner.setNet(_loopedNet);
        netRunner.setCaseID(_id2);
        netRunner.setId(new Long(2));
        YNetRunner.saveNetRunner(netRunner, null);
        netRunner = new YNetRunner();
        netRunner.setNet(_loopedNet);
        netRunner.setCaseID(_id3);
        netRunner.setId(new Long(3));

        YNetRunner.saveNetRunner(netRunner, null);
        netRunner = new YNetRunner();
        netRunner.setNet(_loopedNet);
        netRunner.setCaseID(_id4);
        netRunner.setId(new Long(4));

        YNetRunner.saveNetRunner(netRunner, null);
        netRunner = new YNetRunner();
        netRunner.setNet(_loopedNet);
        netRunner.setCaseID(_id5);
        netRunner.setId(new Long(5));

        YNetRunner.saveNetRunner(netRunner, null);
        netRunner = new YNetRunner();
        netRunner.setNet(_loopedNet);
        netRunner.setCaseID(_id6);
        netRunner.setId(new Long(6));

        YNetRunner.saveNetRunner(netRunner, null);
        netRunner = new YNetRunner();
        netRunner.setNet(_loopedNet);
        netRunner.setCaseID(_id7);
        netRunner.setId(new Long(7));

        YNetRunner.saveNetRunner(netRunner, null);
        netRunner = new YNetRunner();
        netRunner.setNet(_loopedNet);
        netRunner.setCaseID(_id8);
        netRunner.setId(new Long(8));

        YNetRunner.saveNetRunner(netRunner, null);
        

        
//        _id8.addLocation((YCondition)_loopedNet.getNetElement("c{YAtomicTask:a, YAtomicTask:d}"));
        File file3 = new File(TestEngineAgainstImproperCompletionOfASubnet.class.getResource(
                "ImproperCompletion.xml").getFile());
        try {
            _weirdSpecification = (YSpecification) YMarshal.unmarshalSpecifications(
                    file3.getAbsolutePath()).get(0);
        } catch (YSyntaxException e) {
            e.printStackTrace();
        }
    }


    public void testGoodNetVerify() {
        List messages = _goodNet.verify();
        //there's three missing splits stuffs
        if (messages.size() > 3) {
            YMessagePrinter.printMessages(messages);
            fail(((YVerificationMessage) messages.get(0)).getMessage() + " num msg = "+ messages.size());
        }
    }


    public void testBadNetVerify() {
        List messages = _badSpecification.verify();
        if (messages.size() != 5) {
            YMessagePrinter.printMessages(messages);
            /*
            InputCondition:i-leaf-c preset must be empty: [YAtomicTask:h-leaf-c]
            YAtomicTask:h-leaf-c [error] any flow into an InputCondition (InputCondition:i-leaf-c) is not allowed.
            YCondition:c1-top is not on a directed path from i to o.
            YCompositeTask:c-top is not on a directed path from i to o.
            YCondition:c2-top is not on a directed path from i to o.
            */
            ;
            fail("BadNet should have produced 5 error messages, but didn't. msgs == " + YMessagePrinter.getMessageString(messages));
        }
    }


    public void testCloneBasics() {
        YInputCondition inputGoodNet = this._goodNet.getInputCondition();
        YInputCondition inputCopy = _copy.getInputCondition();
        assertNotSame(this._goodNet.getNetElements(), _copy.getNetElements());
        Collection<YExternalNetElement> originalNetElements = _goodNet.getNetElements();
        Iterator<YExternalNetElement> iter = originalNetElements.iterator();
        while (iter.hasNext()) {
            String nextElementID = (String) iter.next().getID();
            assertNotNull(_goodNet.getNetElement(nextElementID));
            assertNotNull(_copy.getNetElement(nextElementID));
            assertNotSame(_goodNet.getNetElement(nextElementID),
            		_copy.getNetElement(nextElementID));
        }
        assertNotSame(inputGoodNet, inputCopy);
    }


    public void testCloneVerify() {
        List messages = _copy.verify();
        //there's three missing splits stuffs
        if (messages.size() > 3) {
            fail(YMessagePrinter.getMessageString(messages));
        }
    }


    public void testORJoinEnabled() {
        //XPathSaxonUser an orjoin with two tokens before the or join and a token that may arrive soon.
        assertFalse(_loopedNet.orJoinEnabled((YTask) _loopedNet.getNetElement("f"), _id1));
        //XPathSaxonUser a marking with a token before the orjoin and a token hidden in front of annother
        //orjoin
        assertTrue(_loopedNet.orJoinEnabled((YTask) _loopedNet.getNetElement("f"), _id2));
        //XPathSaxonUser an orjoin with no tokens in its preset
        assertFalse(_loopedNet.orJoinEnabled((YTask) _loopedNet.getNetElement("f"), _id3));
        //XPathSaxonUser an orjoin with a loop in the marking tree
        assertFalse(_loopedNet.orJoinEnabled((YTask) _loopedNet.getNetElement("f"), _id4));
        //XPathSaxonUser orjoin with a deadlock
        assertTrue(_loopedNet.orJoinEnabled((YTask) _loopedNet.getNetElement("f"), _id5));
        //XPathSaxonUser busy task
        assertTrue(_loopedNet.orJoinEnabled((YTask) _loopedNet.getNetElement("f"), _id6));
        //XPathSaxonUser something complex
        assertFalse(_loopedNet.orJoinEnabled((YTask) _loopedNet.getNetElement("f"), _id7));
        //assert that despite exploring the execution tree it hasn't changed the state of the net.
        assertTrue(_id7.getLocations().contains(_loopedNet.getNetElement("cA")));
        assertTrue(_id7.getLocations().contains(_loopedNet.getNetElement("cB")));

        assertTrue(_id7.getLocations().contains(_loopedNet.getNetElement("c{q_f}")));
        assertFalse(_loopedNet.orJoinEnabled((YTask) _loopedNet.getNetElement("f"), _id8));
    }


    public void testCloneWithNewDataModel() throws YSchemaBuildingException, YSyntaxException, JDOMException, IOException, YPersistenceException {
        File specificationFile = new File(YMarshal.class.getResource("MakeRecordings.xml").getFile());
        List specifications = null;
        specifications = YMarshal.unmarshalSpecifications(specificationFile.getAbsolutePath());
        YSpecification originalSpec = (YSpecification) specifications.iterator().next();
        YNet originalNet = originalSpec.getRootNet();
        YNet clonedNet = null;
        clonedNet = (YNet) originalNet.clone();
//System.out.println("originalSpec = " + originalSpec.toXML(false));
        List messages = originalNet.verify();
        if (messages.size() > 0) {
            fail(YMessagePrinter.getMessageString(messages));
        }
        assertTrue(originalNet.verify().size() == 0);
        if (clonedNet.verify().size() != 0) {
            fail(YMessagePrinter.getMessageString(clonedNet.verify()));
        }
//System.out.println("cloneSpec =    " + cloneSpec.toXML());
        assertEquals(originalNet.toXML(), clonedNet.toXML());
    }


    public void testDataStructureAgainstWierdSpecification(){
        YNet weirdRootClone = (YNet) _weirdSpecification.getRootNet().clone();
        try {
            YNet weirdleafDClone = (YNet) ((YTask)weirdRootClone.getNetElement("d-top"))
                    .getDecompositionPrototype().clone();

        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }


    public static void main(String args[]) {
        TestRunner runner = new TestRunner();
        runner.doRun(suite());
        System.exit(0);
    }
    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(TestYNet.class);
        return suite;
    }
}
