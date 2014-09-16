package org.yawlfoundation.yawl.elements;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.jdom2.JDOMException;
import org.yawlfoundation.yawl.elements.state.TestYMarking;
import org.yawlfoundation.yawl.elements.state.YIdentifier;
import org.yawlfoundation.yawl.engine.TestEngineAgainstImproperCompletionOfASubnet;
import org.yawlfoundation.yawl.exceptions.YPersistenceException;
import org.yawlfoundation.yawl.exceptions.YSchemaBuildingException;
import org.yawlfoundation.yawl.exceptions.YSyntaxException;
import org.yawlfoundation.yawl.unmarshal.YMarshal;
import org.yawlfoundation.yawl.util.StringUtil;
import org.yawlfoundation.yawl.util.YVerificationHandler;
import org.yawlfoundation.yawl.util.YVerificationMessage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;

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

    private YVerificationHandler handler = new YVerificationHandler();


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

        specification1 = (YSpecification) YMarshal.unmarshalSpecifications(StringUtil.fileToString(file1.getAbsolutePath())).get(0);


        _badSpecification = (YSpecification) YMarshal.unmarshalSpecifications(StringUtil.fileToString(file2.getAbsolutePath())).get(0);

        _goodNet = specification1.getRootNet();
        _badNet = _badSpecification.getRootNet();
        _copy = null;
        _copy = (YNet) this._goodNet.clone();
        URL fileURL = TestYMarking.class.getResource("YAWLOrJoinTestSpecificationLongLoops.xml");
        File yawlXMLFile = new File(fileURL.getFile());
        YSpecification specification = null;
        specification = (YSpecification) YMarshal.
                        unmarshalSpecifications(StringUtil.fileToString(yawlXMLFile.getAbsolutePath())).get(0);
        _loopedNet = specification.getRootNet();
        _id1 = new YIdentifier(null);

        _id1.addLocation(null, (YCondition) _loopedNet.getNetElement("c{d_f}"));
        _id1.addLocation(null, (YCondition) _loopedNet.getNetElement("c{d_f}"));
        _id1.addLocation(null, (YCondition) _loopedNet.getNetElement("cC"));
        _id2 = new YIdentifier(null);
        _id2.addLocation(null, (YCondition) _loopedNet.getNetElement("c{d_f}"));
        _id2.addLocation(null, (YCondition) _loopedNet.getNetElement("cA"));
        _id3 = new YIdentifier(null);
        _id3.addLocation(null, (YCondition) _loopedNet.getNetElement("cC"));
        _id4 = new YIdentifier(null);
        _id4.addLocation(null, (YCondition) _loopedNet.getNetElement("i-top"));
        _id4.addLocation(null, (YCondition) _loopedNet.getNetElement("c{d_f}"));
        _id5 = new YIdentifier(null);
        _id5.addLocation(null, (YCondition) _loopedNet.getNetElement("c{d_f}"));
        _id5.addLocation(null, (YCondition) _loopedNet.getNetElement("c{b_w}"));
        _id6 = new YIdentifier(null);
        _id6.addLocation(null, (YTask) _loopedNet.getNetElement("d"));
        _id6.addLocation(null, (YCondition) _loopedNet.getNetElement("c{d_f}"));
        _id7 = new YIdentifier(null);
        _id7.addLocation(null, (YCondition) _loopedNet.getNetElement("cA"));
        _id7.addLocation(null, (YCondition) _loopedNet.getNetElement("cB"));
        _id7.addLocation(null, (YCondition) _loopedNet.getNetElement("c{q_f}"));
        _id8 = new YIdentifier(null);
        _id8.addLocation(null, (YCondition) _loopedNet.getNetElement("cA"));
        _id8.addLocation(null, (YCondition) _loopedNet.getNetElement("cB"));
        _id8.addLocation(null, (YCondition) _loopedNet.getNetElement("cC"));
        _id8.addLocation(null, (YCondition) _loopedNet.getNetElement("c{q_f}"));
//        _id8.addLocation((YCondition)_loopedNet.getNetElement("c{YAtomicTask:a, YAtomicTask:d}"));
        File file3 = new File(TestEngineAgainstImproperCompletionOfASubnet.class.getResource(
                "ImproperCompletion.xml").getFile());
        try {
            _weirdSpecification = (YSpecification) YMarshal.unmarshalSpecifications(
                    StringUtil.fileToString(file3.getAbsolutePath())).get(0);
        } catch (YSyntaxException e) {
            e.printStackTrace();
        }
    }


    public void testGoodNetVerify() {
        handler.reset();
        _goodNet.verify(handler);
        //there's three missing splits stuffs
        if (handler.getMessageCount() > 3) {
            for (YVerificationMessage msg : handler.getMessages()) {
                System.out.println(msg);
            }
            fail(handler.getMessages().get(0).getMessage() + " num msg = "+ handler.getMessageCount());
        }
    }


    public void testBadNetVerify() {
        handler.reset();
        _badSpecification.verify(handler);
        if (handler.getMessageCount() != 5) {
            for (YVerificationMessage msg : handler.getMessages()) {
                System.out.println(msg);
            }
            /*
            InputCondition:i-leaf-c preset must be empty: [YAtomicTask:h-leaf-c]
            YAtomicTask:h-leaf-c [error] any flow into an InputCondition (InputCondition:i-leaf-c) is not allowed.
            YCondition:c1-top is not on a directed path from i to o.
            YCompositeTask:c-top is not on a directed path from i to o.
            YCondition:c2-top is not on a directed path from i to o.
            */
            fail("BadNet should have produced 5 error messages, but didn't");
        }
    }


    public void testCloneBasics() {
        YInputCondition inputGoodNet = this._goodNet.getInputCondition();
        YInputCondition inputCopy = _copy.getInputCondition();
        assertNotSame(this._goodNet.getNetElements(), _copy.getNetElements());
        Map originalNetElements = _goodNet.getNetElements();
        Map copiedNetElements = _copy.getNetElements();
        Iterator iter = originalNetElements.keySet().iterator();
        while (iter.hasNext()) {
            String nextElementID = (String) iter.next();
            assertNotNull(originalNetElements.get(nextElementID));
            assertNotNull(copiedNetElements.get(nextElementID));
            assertNotSame(originalNetElements.get(nextElementID),
                    copiedNetElements.get(nextElementID));
        }
        assertNotSame(inputGoodNet, inputCopy);
    }


    public void testCloneVerify() {
        handler.reset();
        _copy.verify(handler);
        //there's three missing splits stuffs
        if (handler.getMessageCount() > 3) {
            for (YVerificationMessage msg : handler.getMessages()) {
                System.out.println(msg);
            }
            fail(handler.getMessages().toString());
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


//    public void testCloneWithNewDataModel() throws YSchemaBuildingException, YSyntaxException, JDOMException, IOException {
//        File specificationFile = new File(YMarshal.class.getResource("MakeRecordings.xml").getFile());
//        List specifications = null;
//        specifications = YMarshal.unmarshalSpecifications(StringUtil.fileToString(specificationFile.getAbsolutePath()));
//        YSpecification originalSpec = (YSpecification) specifications.iterator().next();
//        YNet originalNet = originalSpec.getRootNet();
//        YNet clonedNet = null;
//        clonedNet = (YNet) originalNet.clone();
////System.out.println("originalSpec = " + originalSpec.toXML(false));
//        List messages = originalNet.verify();
//        if (messages.size() > 0) {
//            fail(YMessagePrinter.getMessageString(messages));
//        }
//        assertTrue(originalNet.verify().size() == 0);
//        if (clonedNet.verify().size() != 0) {
//            fail(YMessagePrinter.getMessageString(clonedNet.verify()));
//        }
////System.out.println("cloneSpec =    " + cloneSpec.toXML());
//        assertEquals(originalNet.toXML(), clonedNet.toXML());
//    }


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
