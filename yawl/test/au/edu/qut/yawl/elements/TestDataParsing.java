/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.elements;

import au.edu.qut.yawl.exceptions.YSyntaxException;
import au.edu.qut.yawl.exceptions.YSchemaBuildingException;
import au.edu.qut.yawl.exceptions.YPersistenceException;
import au.edu.qut.yawl.unmarshal.YMarshal;
import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;

import org.jdom.JDOMException;

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


    public void testSchemaCatching() throws JDOMException, IOException, YSchemaBuildingException, YPersistenceException {
        Exception d = null;
        try {
            File file1 = new File(getClass().getResource("duplicateDataSpecification.xml").getFile());
            _badSpecification = (YSpecification) YMarshal.unmarshalSpecifications(
                    file1.getAbsolutePath()).get(0);
        } catch (YSyntaxException e) {
            d = e;
        }
        assertTrue(d != null);
    }
}
