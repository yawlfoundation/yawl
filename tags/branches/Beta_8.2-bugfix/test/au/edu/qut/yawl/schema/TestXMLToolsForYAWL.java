/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.schema;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.net.URL;
import java.io.IOException;

import org.jdom.output.XMLOutputter;
import org.jdom.output.Format;
import org.jdom.input.SAXBuilder;
import org.jdom.JDOMException;
import org.jdom.Document;
import org.eclipse.xsd.XSDSchema;
import au.edu.qut.yawl.exceptions.YSyntaxException;
import au.edu.qut.yawl.exceptions.YSchemaBuildingException;

/**
 /**
 * 
 * @author Lachlan Aldred
 * Date: 15/10/2004
 * Time: 17:34:29
 * 
 */
public class TestXMLToolsForYAWL extends TestCase {
    private XMLToolsForYAWL _xmlToolsForYAWL;

    public TestXMLToolsForYAWL(String name) {
        super(name);
    }

    public void setUp() throws JDOMException, IOException, YSchemaBuildingException, YSyntaxException {
        _xmlToolsForYAWL = new XMLToolsForYAWL();
        URL url = getClass().getResource("SelfContainedPerson.xsd");
        SAXBuilder builder = new SAXBuilder();
        Document document = builder.build(url);
        XMLOutputter output = new XMLOutputter(Format.getCompactFormat());
        _xmlToolsForYAWL.setPrimarySchema(output.outputString(document));
    }


    public void testSelfContainedRefs() throws YSchemaBuildingException {
        List instructions = new ArrayList();
        Instruction i;
        i = new ElementReuseInstruction("number");
        instructions.add(i);
        i = new ElementReuseInstruction("address");
        instructions.add(i);
        i = new ElementCreationInstruction("person", "PersonType", false);
        instructions.add(i);
        i = new ElementCreationInstruction("person2", "PersonType", false);
        instructions.add(i);
        i = new ElementCreationInstruction("myState", "StateType", false);
        instructions.add(i);
        i = new ElementCreationInstruction("yourState", "StateType", false);
        instructions.add(i);


        Instruction[] instructionsArr;
        instructionsArr = (Instruction[]) instructions.toArray(
                new Instruction[instructions.size()]);
        String schemaStr2 = _xmlToolsForYAWL.createYAWLSchema(instructionsArr, "data");
        //todo test contents to check if schemaStr2 is self contained.
    }

    public void testIfWeCanBuildASelfContainedRefElement() throws YSchemaBuildingException {
        List instructions = new ArrayList();
        Instruction i;
        i = new ElementReuseInstruction("number");
        instructions.add(i);
        i = new ElementReuseInstruction("address");
        instructions.add(i);
        i = new ElementCreationInstruction("person", "PersonType", false);
        instructions.add(i);
        i = new ElementCreationInstruction("person2", "PersonType", false);
        instructions.add(i);
        i = new ElementCreationInstruction("myState", "StateType", false);
        instructions.add(i);
        i = new ElementCreationInstruction("yourState", "StateType", false);
        instructions.add(i);
        Instruction[] instructionsArr;
        instructionsArr = (Instruction[]) instructions.toArray(
                new Instruction[instructions.size()]);
        String schemaStr = _xmlToolsForYAWL.createYAWLSchema(instructionsArr, "data");
        if (schemaStr.indexOf("xsd:element ref=\"number\"") != -1) {
            fail("We need to build self contained elements from element references." +
                    "\n\tSchema STRING = " +
                    "\n" + schemaStr);
        }

    }


    public static void main(String args[]) {
        TestRunner runner = new TestRunner();
        runner.doRun(suite());
        System.exit(0);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(TestXMLToolsForYAWL.class);
        return suite;
    }
}
