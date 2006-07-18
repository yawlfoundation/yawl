/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.persistence;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

import org.jdom.JDOMException;

import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.exceptions.YSchemaBuildingException;
import au.edu.qut.yawl.unmarshal.YMarshal;

/**
 * 
 * @author Lachlan Aldred
 * Date: 23/02/2004
 * Time: 17:23:58
 * 
 */
public class TestDataParsing extends TestCase {
    private YSpecification _badSpecification;

    public TestDataParsing(String name) {
        super(name);
    }

    public void setUp() {
    }


    public void testSchemaCatching() throws JDOMException, IOException, YSchemaBuildingException {
        Exception d = null;
        try {
            File file1 = new File(getClass().getResource("duplicateDataSpecification.xml").getFile());
//the previous tests:
//
            _badSpecification = (YSpecification) YMarshal.unmarshalSpecifications(
                    file1.getAbsolutePath()).get(0);
            _badSpecification.verify();
        } catch (Exception e) {
            d = e;
            e.printStackTrace();
        }
        assertNotNull(d);

//        This is an old file formatted in a way we're not really supporting under new... 
        //        assertTrue(d instanceof YSyntaxException );
    }
}
