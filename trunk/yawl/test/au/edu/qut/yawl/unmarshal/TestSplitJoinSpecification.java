/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package au.edu.qut.yawl.unmarshal;

import au.edu.qut.yawl.elements.YNet;
import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.exceptions.YSyntaxException;
import au.edu.qut.yawl.exceptions.YSchemaBuildingException;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import java.io.*;
import java.util.List;

/**
 * @author Nathan Rose
 */
public class TestSplitJoinSpecification extends TestCase {
    private YSpecification _originalSpec;
    private YSpecification _copy;
    private String _originalXMLString;
    private String _copyXMLString;
    
    private static final String TEMP_FILENAME = "tempTestFile.xml";
    
    private static final String SPEC_FILENAME = "SplitJoinSpecification.xml";

    public TestSplitJoinSpecification( String name ) {
        super( name );
    }

    public void setUp() throws YSyntaxException, IOException, YSchemaBuildingException, JDOMException {
        File specificationFile = new File( getClass().getResource( SPEC_FILENAME ).getFile() );
        List<YSpecification> specifications = YMarshal.unmarshalSpecifications( specificationFile.getAbsolutePath() );
        
        _originalSpec = specifications.iterator().next();
        
        String marshalledSpecs = YMarshal.marshal( specifications );
        
        File derivedSpecFile = new File( specificationFile.getParent() + File.separator + TEMP_FILENAME );
        FileWriter fw = new FileWriter( derivedSpecFile );
        fw.write(marshalledSpecs);
        fw.flush();
        fw.close();
        specifications = YMarshal.unmarshalSpecifications( derivedSpecFile.getAbsolutePath() );
        _copy = specifications.iterator().next();
        
        derivedSpecFile.delete();
        
        _originalXMLString = YMarshal.marshal( _originalSpec );
        _copyXMLString = YMarshal.marshal( _copy );
    }



/*
    public void testBothValid() {
        List errorMessages = _originalSpec.verify();
        if (errorMessages.size() != 0) {
            fail(YMessagePrinter.getMessageString(errorMessages));
        }
        errorMessages = _copy.verify();
        if (errorMessages.size() != 0) {
            fail(YMessagePrinter.getMessageString(errorMessages));
        }
    }

*/
    public void testBothEqual() {
        assertEquals(_originalSpec.getID(), _copy.getID());
        YNet origNet = _originalSpec.getRootNet();
        YNet copyNet = _copy.getRootNet();
        assertEquals(origNet.getInputCondition().toXML(), copyNet.getInputCondition().toXML());
        assertEquals(origNet.getOutputCondition().toXML(), copyNet.getOutputCondition().toXML());
        assertEquals(origNet.getLocalVariables().toString(), copyNet.getLocalVariables().toString());
//System.out.println("\n\norigXML\n" + _originalXMLString);
//System.out.println("\n\n_copyXMLString\n" + _copyXMLString);
        assertEquals(_originalXMLString, _copyXMLString);
    }


    public void testLineByLine() {
        File testFile = new File(YMarshal.class.getResource("MakeRecordings.xml").getFile());
//System.out.println("origXML\n" + _originalXMLString);
//YNet r = _originalSpec.getRootNet();
//System.out.println("r.hashCode() = " + r.hashCode());
//System.out.println("r.getInputCondition() = " + r.getInputCondition().toXML());
//Map ne = r.getNetElements();
//System.out.println("r.getNetElements() = " + ne);
//YTask t = ((YTask) r.getNetElement("record"));
//System.out.println("t.toXML() = " + t.toXML(false));
//YMultiInstanceAttributes mi = t.getMultiInstanceAttributes();
//System.out.println("this should be 1: " + mi.getMinInstances());
//System.out.println("this should be 1: " + mi.getMaxInstances());
//System.out.println("this should be 1: " + mi.getThreshold());
        SAXBuilder builder = new SAXBuilder();
        Document doc = null;
        try {
            doc = builder.build(new StringReader(_originalXMLString));
        } catch (JDOMException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        XMLOutputter outputter = new XMLOutputter(Format.getCompactFormat());
        String groomedOriginalXMLString = outputter.outputString(doc);
        groomedOriginalXMLString = groomedOriginalXMLString.replaceAll(" />", "/>");
        try {
            BufferedReader reader = new BufferedReader(new FileReader(testFile));
            String line = null;
            for (int i = 1; (line = reader.readLine()) != null; i++) {
                line = line.trim();
                if (groomedOriginalXMLString.indexOf(line) == -1) {
System.out.println(line);
System.out.println("_originalXMLString = " + groomedOriginalXMLString);
                    fail("\n[Error]:line[" + i + "]" + testFile.getName() + " \"" +
                            line + "\" not found in XML-isation");
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void main(String args[]) {
        TestRunner runner = new TestRunner();
        runner.doRun(suite());
        System.exit(0);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(TestSplitJoinSpecification.class);
        return suite;
    }
}
