/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
 */


package org.yawlfoundation.yawl.elements;

import org.yawlfoundation.yawl.exceptions.YSyntaxException;
import org.yawlfoundation.yawl.exceptions.YSchemaBuildingException;
import org.yawlfoundation.yawl.unmarshal.YMarshal;
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


    public void testSchemaCatching() throws JDOMException, IOException, YSchemaBuildingException {
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
