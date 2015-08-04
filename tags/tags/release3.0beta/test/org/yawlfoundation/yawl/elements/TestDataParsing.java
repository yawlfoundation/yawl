package org.yawlfoundation.yawl.elements;

import junit.framework.TestCase;
import org.jdom2.JDOMException;
import org.yawlfoundation.yawl.exceptions.YSchemaBuildingException;
import org.yawlfoundation.yawl.exceptions.YSyntaxException;
import org.yawlfoundation.yawl.unmarshal.YMarshal;
import org.yawlfoundation.yawl.util.StringUtil;

import java.io.File;
import java.io.IOException;

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
            _badSpecification = (YSpecification) YMarshal.unmarshalSpecifications(StringUtil.fileToString(
                    file1.getAbsolutePath())).get(0);
        } catch (YSyntaxException e) {
            d = e;
        }
        assertTrue(d != null);
    }
}
