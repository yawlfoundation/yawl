package org.yawlfoundation.yawl.elements;

import org.yawlfoundation.yawl.elements.data.YVariable;
import org.yawlfoundation.yawl.elements.data.YParameter;
import org.yawlfoundation.yawl.elements.state.YIdentifier;
import org.yawlfoundation.yawl.exceptions.*;
import org.yawlfoundation.yawl.unmarshal.YMarshal;
import org.yawlfoundation.yawl.util.YVerificationHandler;
import org.yawlfoundation.yawl.util.YVerificationMessage;
import org.yawlfoundation.yawl.util.StringUtil;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.yawlfoundation.yawl.engine.YNetData;
import org.yawlfoundation.yawl.schema.YSchemaVersion;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * Author: Lachlan Aldred
 * Date: 17/04/2003
 * Time: 15:52:30
 * 
 */
public class TestYExternalTask extends TestCase{
    private YCondition _aCondition;
    private YTask _validTask;
    private YTask _invalidTask;
    private YTask _needsPredicateString;
    private YTask _needsNoPredicateString;
    private YTask _invalidTask2;


    public TestYExternalTask(String name)
    {
        super(name);
    }


    public void setUp() throws YPersistenceException {
        YSpecification spec = new YSpecification("");
        spec.setVersion(YSchemaVersion.Beta2);
        YNet deadNet = new YNet("aNetName", spec);
        YVariable v = new YVariable(null);
        v.setName("stubList");
        v.setUntyped(true);
        v.setInitialValue("<stub/><stub/><stub/>");
        YNetData casedata = new YNetData();
        deadNet.initializeDataStore(null, casedata);
        deadNet.setLocalVariable(v);
        deadNet.initialise(null);
        _aCondition = new YCondition("c1", deadNet);
        _validTask = new YAtomicTask("et1", YTask._AND, YTask._OR, deadNet);
        YFlow f = new YFlow(_aCondition, _validTask);
        _aCondition.addPostset(f);
        Map map = new HashMap();
        map.put("stub","/data/stubList");
        f = new YFlow(_validTask, _aCondition);
        f.setIsDefaultFlow(true);
        f.setXpathPredicate("random()");
        _validTask.addPostset(f);
        _validTask.setUpMultipleInstanceAttributes("3","3","3", "static");

        YDecomposition descriptor = new YAWLServiceGateway("Wash floor", spec);
        _validTask.setDecompositionPrototype(descriptor);
        _invalidTask = new YAtomicTask("et2", 21, 32, null);
        _invalidTask.setUpMultipleInstanceAttributes("0","-1","-1","dfsdfsdf");
        _validTask.setDataMappingsForTaskStarting(map);
        _validTask.setMultiInstanceInputDataMappings("stub", "for $d in /stubList/* return $d");
        _invalidTask2 = new YAtomicTask("et3", YTask._AND, YTask._XOR, null);
        f = new YFlow(_invalidTask2, _aCondition);
        _invalidTask2.addPostset(f);
        f= new YFlow(_aCondition, _invalidTask2);
        _invalidTask2.addPreset(f);
        _needsPredicateString = new YAtomicTask("et3", YAtomicTask._XOR, YAtomicTask._OR, deadNet);
        f= new YFlow(_needsPredicateString, _aCondition);
        _needsPredicateString.addPostset(f);
        f = new YFlow(_aCondition, _needsPredicateString);
        _needsPredicateString.addPreset(f);
        _needsNoPredicateString = new YAtomicTask("et4", YAtomicTask._AND, YAtomicTask._AND, deadNet);
        f = new YFlow(_needsNoPredicateString, _aCondition);
        f.setXpathPredicate("not valid xpath");
        _needsNoPredicateString.addPostset(f);
        f = new YFlow(_aCondition, _needsNoPredicateString);
        _needsNoPredicateString.addPreset(f);

    }


    public void testValidStuff(){
        assertTrue(_validTask.getPostsetElement("c1").equals(_aCondition));
        assertTrue(_aCondition.getPostsetElement("et1").equals(_validTask));
        assertTrue(_validTask.getPresetElement("c1").equals(_aCondition));
        assertTrue(_aCondition.getPresetElement("et1").equals(_validTask));
        YVerificationHandler handler = new YVerificationHandler();
        _validTask.verify(handler);
        if (handler.hasMessages()) {
            for (YVerificationMessage msg : handler.getMessages()) {
                System.out.println(msg.getMessage());
            }
            fail(handler.getMessages().get(0).getMessage());
        }
        _validTask.getRemoveSet();
    }

    public void testBadQueries(){
        YSpecification spec = new YSpecification("spec1");
        YNet net = new YNet("a", spec);
        YVariable var = new YVariable(net);
        var.setName("localVar");
        net.setLocalVariable(var);
        YTask task = new YAtomicTask("1", YTask._AND, YTask._AND, net);
        YAWLServiceGateway ysg = new YAWLServiceGateway("b", spec);
        YParameter p = new YParameter(ysg, YParameter._INPUT_PARAM_TYPE);
        p.setName("fred");
        YParameter q = new YParameter(ysg, YParameter._OUTPUT_PARAM_TYPE);
        q.setName("fred");
        ysg.addInputParameter(p);
        ysg.addOutputParameter(q);
        task.addPreset(new YFlow(task, task));

        task.setDecompositionPrototype(ysg);
        task.setDataBindingForInputParam("", "fred");
        task.setDataBindingForOutputExpression(null, "localVar");
        YVerificationHandler handler = new YVerificationHandler();
        task.verify(handler);
        assertTrue(handler.getMessages().size() == 2);
    }

    public void testBadQueries2(){
        YSpecification spec = new YSpecification("spec1");
        YNet net = new YNet("a", spec);
        YVariable var = new YVariable(net);
        var.setName("localVar");
        net.setLocalVariable(var);
        YTask task = new YAtomicTask("1", YTask._AND, YTask._AND, net);
        YAWLServiceGateway ysg = new YAWLServiceGateway("b", spec);
        YParameter p = new YParameter(ysg, YParameter._INPUT_PARAM_TYPE);
        p.setName("fred");
        YParameter q = new YParameter(ysg, YParameter._OUTPUT_PARAM_TYPE);
        q.setName("fred");
        ysg.addInputParameter(p);
        ysg.addOutputParameter(q);
        task.addPreset(new YFlow(task, task));

        task.setDecompositionPrototype(ysg);
        task.setDataBindingForInputParam("dflkjbdfsk;jnbesdlk;", "fred");
        task.setDataBindingForOutputExpression("><nn>", "localVar");
        YVerificationHandler handler = new YVerificationHandler();
        task.verify(handler);
        assertTrue(handler.getMessages().size() == 2);
        /*
        Error:AtomicTask:1(id= 1) the XQuery could be successfully parsed. [XQuery syntax error in "dflkjbdfsk;jnbesdlk;":
            Unexpected token ; beyond end of query]
        Error:AtomicTask:1(id= 1) the XQuery could be successfully parsed. [XQuery syntax error in "><":
            Unexpected token [>] in path expression]
        */
    }


    public void testInvalidVerify(){
        YVerificationHandler handler = new YVerificationHandler();
        _invalidTask.verify(handler);
        if(handler.getMessages().size() != 10){
            /*
                YAtomicTask:et2 This must have a net to be valid.
                YAtomicTask:et2 The postset size must be > 0
                YAtomicTask:et2 The preset size must be > 0
                YAtomicTask:et2 Incorrect value for split type
                YAtomicTask:et2 Incorrect value for join type
                YAtomicTask:et2 _minInstances > 1
                YAtomicTask:et2._minInstances > _maxInstances
                YAtomicTask:et2._maxInstances < 1
                YAtomicTask:et2._threshold < 1
                YAtomicTask:et2._creationMode does not equal 'static' or 'dynamic'
            */
            for (YVerificationMessage msg : handler.getMessages()) {
                System.out.println(msg.getMessage());
            }
            fail(handler.getMessages().get(0).getMessage() +
                    " num messages == " +  handler.getMessages().size());
        }
    }


    public void testNeedsPredicateVerify(){
        YVerificationHandler handler = new YVerificationHandler();
        _needsPredicateString.verify(handler);
        if(! handler.hasMessages()) {
            fail("Should have failed verification but passed.");
        }
    }


    public void testNeedsNoPredicateVerify(){
        YVerificationHandler handler = new YVerificationHandler();
        _needsNoPredicateString.verify(handler);
        if(! handler.hasMessages()) {
            fail("Should have failed verification but passed.");
        }
    }


    public void testThrowsAppropriateStateExceptions() throws YDataStateException, YQueryException, YPersistenceException, YSchemaBuildingException {
        Exception f = null;
        try{
            _validTask.t_fire(null);
        }catch(YAWLException e){
            f= e;
        }
        assertNotNull(f); f= null;
        _aCondition.add(null, new YIdentifier(null));
        assertTrue(_validTask.t_enabled(null));
        List childIdentifiers = null;
        try {
            childIdentifiers = _validTask.t_fire(null);
        } catch (YStateException e) {
            e.printStackTrace();
            fail();
        }
        assertTrue(childIdentifiers.size() == 3);
        try{
            _validTask.t_fire(null);
        }catch(YStateException e){
            f= e;
        }
        assertNotNull(f); f= null;
        try{
            Document d = new Document(new Element("data"));
            _validTask.t_complete(null, (YIdentifier)childIdentifiers.get(0),d);
        }catch(Exception e){
            f= e;
        }
        assertNotNull(f);f= null;
        try{
            _validTask.t_add(null, null, null);
        }catch(Exception e){
            f= e;
        }
        assertNotNull(f);
    }

    public void testToXML(){
        YAtomicTask t1 = new YAtomicTask("1", YAtomicTask._AND, YAtomicTask._XOR, null);
        YAtomicTask t2 = new YAtomicTask("2", YAtomicTask._AND, YAtomicTask._AND, null);
        YSpecification spec = new YSpecification("");
        spec.setVersion(YSchemaVersion.Beta2);
        t2.setDecompositionPrototype(new YAWLServiceGateway("blah", spec));

        YAtomicTask t3 = new YAtomicTask("3", YAtomicTask._AND, YAtomicTask._AND, null);
        YAtomicTask t4 = new YAtomicTask("4", YAtomicTask._AND, YAtomicTask._AND, null);

        YFlow f2 = new YFlow(t1, t2);
        f2.setXpathPredicate("true()");
        f2.setEvalOrdering(new Integer(1));

        YCondition implicit = new YCondition("imp", null);
        implicit.setImplicit(true);

        YFlow f3 = new YFlow(t1, t3);
        f3.setIsDefaultFlow(true);

        YFlow f4 = new YFlow(t1, t4);
        f4.setXpathPredicate("true()");
        f4.setEvalOrdering(new Integer(2));

        YFlow fImp = new YFlow(t2, implicit);
        t2.addPostset(fImp);
        YFlow f5 = new YFlow(implicit, t1);
        implicit.addPostset(f5);
        t1.setUpMultipleInstanceAttributes("3", "10", "3", "static");
        t1.setMultiInstanceInputDataMappings("stub", "for $d in /stubList/* return $d");
        t1.setDataBindingForInputParam("/data/stubList", "stub");
        List removesList = new ArrayList();
        removesList.add(t1);
        removesList.add(t2);
        removesList.add(t3);
        removesList.add(t4);
        removesList.add(implicit);
        t2.addRemovesTokensFrom(removesList);
        t2.setDataBindingForInputParam("/data/stub", "stub");

//        Map m = new HashMap();
//        m.put("dumy1", "dummy2");
//        t2.setDataMappingsForTaskStarting(m);
//        t2.setDataMappingsForTaskCompletion(m);
        t1.addPostset(f2);
        t1.addPostset(f3);
        t1.addPostset(f4);
        t1.addPreset(fImp);

        assertEquals(
        "<task id=\"1\" xsi:type=\"MultipleInstanceExternalTaskFactsType\" " +
        "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><flowsInto>" +
        "<nextElementRef id=\"3\"/><isDefaultFlow/></flowsInto><flowsInto>" +
        "<nextElementRef id=\"2\"/><predicate ordering=\"1\">true()</predicate>" +
        "</flowsInto><flowsInto><nextElementRef id=\"4\"/><predicate ordering=\"2\">" +
        "true()</predicate></flowsInto><join code=\"and\"/><split code=\"xor\"/>" +
        "<minimum>3</minimum><maximum>10</maximum><threshold>3</threshold>" +
        "<creationMode code=\"static\"/><miDataInput><expression query=\"/data/stubList\"/>" +
        "<splittingExpression query=\"for $d in /stubList/* return $d\"/>" +
        "<formalInputParam>stub</formalInputParam></miDataInput></task>",
        t1.toXML());

        assertEquals(
        "<task id=\"2\"><flowsInto><nextElementRef id=\"1\"/></flowsInto>" +
        "<flowsInto><nextElementRef id=\"1\"/></flowsInto><join code=\"and\"/>" +
        "<split code=\"and\"/><removesTokens id=\"1\"/><removesTokens id=\"2\"/>" +
        "<removesTokens id=\"3\"/><removesTokens id=\"4\"/><removesTokensFromFlow>" +
        "<flowSource id=\"2\"/><flowDestination id=\"1\"/></removesTokensFromFlow>" +
        "<startingMappings><mapping><expression query=\"/data/stub\"/><mapsTo>stub</mapsTo>" +
        "</mapping></startingMappings><decomposesTo id=\"blah\"/></task>",
        t2.toXML());
    }


    public void testToXML2(){
        YAtomicTask t1 = new YAtomicTask("1", YAtomicTask._AND, YAtomicTask._XOR, null);
        YAtomicTask t2 = new YAtomicTask("2", YAtomicTask._AND, YAtomicTask._AND, null);

        YSpecification spec = new YSpecification("");
        spec.setVersion(YSchemaVersion.Beta2);

        t2.setDecompositionPrototype(new YAWLServiceGateway("blah2", spec));
        YAtomicTask t3 = new YAtomicTask("3", YAtomicTask._AND, YAtomicTask._AND, null);
        YCondition c1 = new YCondition("c1", null);
        YCondition c2 = new YCondition("c2", null);

        YFlow f1 = new YFlow(t1, t2);
        YCondition implicit = new YCondition("imp", null);
        implicit.setImplicit(true);
        YFlow f2 = new YFlow(t1, t3);

        YFlow f4 = new YFlow(t2, implicit);
        t2.addPostset(f4);
        YFlow f5 = new YFlow(implicit, t1);
        implicit.addPostset(f5);
        t1.setUpMultipleInstanceAttributes("1", "3", "2", "static");
        List removesList = new ArrayList();
        removesList.add(t1);
        removesList.add(t2);
        removesList.add(t3);
        removesList.add(c1);
        removesList.add(c2);
        removesList.add(implicit);
        t2.addRemovesTokensFrom(removesList);
        t1.addPostset(f1);
        t1.addPostset(f2);
        t1.addPreset(f4);
        assertEquals(
                "<task id=\"2\"><flowsInto><nextElementRef id=\"1\"/></flowsInto>" +
                "<flowsInto><nextElementRef id=\"1\"/></flowsInto><join code=\"and\"/>" +
                "<split code=\"and\"/><removesTokens id=\"1\"/><removesTokens id=\"2\"/>" +
                "<removesTokens id=\"3\"/><removesTokens id=\"c1\"/>" +
                "<removesTokens id=\"c2\"/><removesTokensFromFlow><flowSource id=\"2\"/>" +
                "<flowDestination id=\"1\"/></removesTokensFromFlow><decomposesTo " +
                "id=\"blah2\"/></task>", t2.toXML());

    }

    public void testInvalidMIAttributeVerify() throws YSchemaBuildingException, YSyntaxException, JDOMException, IOException {
        URL fileURL = getClass().getResource("BadMI.xml");
		File yawlXMLFile = new File(fileURL.getFile());
        YSpecification specification = null;

        specification = YMarshal.
                        unmarshalSpecifications(StringUtil.fileToString(yawlXMLFile.getAbsolutePath())).get(0);
        YVerificationHandler handler = new YVerificationHandler();
        specification.verify(handler);
        String verificationResult = handler.getMessages().get(0).getMessage();
        String expectedResult =
                "\nError:A output parameter is used twice.  " +
                "The task (id=3) uses the same parameter through " +
                "its multi-instance output and its regular output.";

        if(!verificationResult.equals(expectedResult)){
            System.out.println();
            System.out.println();
            fail("[expectedResult]" + expectedResult +
                    "\n[verificationResult]" + verificationResult);
        }
    }

    public void testSelfCancellation() throws YStateException, YQueryException, YDataStateException, YSchemaBuildingException, YPersistenceException {
        YSpecification spec = new YSpecification("");
        spec.setVersion(YSchemaVersion.Beta2);
        YNet n = new YNet("", spec);
        YTask t1 = new YAtomicTask("t1", 95, 95, n);
        YCondition c1, c2;
        c1 = new YCondition("c2", null);
        t1.addPreset(new YFlow(c1, t1));
        c2 = new YCondition("c2", null);
        t1.addPostset(new YFlow(t1, c2));
        List l1 = new ArrayList();
        l1.add(t1);
        t1.addRemovesTokensFrom(l1);
        c1.add(null, new YIdentifier(null));

        List kids = null;
        try {
            kids = t1.t_fire(null);
        } catch (YDataStateException e) {
            e.printStackTrace();
            fail();
        }
        assertTrue(kids.size() == 1);
        YIdentifier kid;
        kid = (YIdentifier)kids.iterator().next();
        t1.t_start(null, kid);
        try {
            t1.t_complete(null, 
                    kid,
                    new Document(new Element("data")));
        } catch (YDataStateException e) {
            e.printStackTrace();
        }
        assertTrue(c2.containsIdentifier());
    }


    public static void main(String args[]) {
        TestRunner runner = new TestRunner();
        runner.doRun(suite());
        System.exit(0);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(TestYExternalTask.class);
        return suite;
    }
}
