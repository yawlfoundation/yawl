package org.yawlfoundation.yawl.resourcing;

import junit.framework.TestCase;

import org.yawlfoundation.yawl.util.JDOMUtil;
import org.jdom2.Document;

/**
 *
 * @author Michael Adams
 * Date: 01/08/2007
 *
 */
public class TestGetSelectors extends TestCase {

    public void testGetSelectors() {

        ResourceManager rm = ResourceManager.getInstance();
        String xml = rm.getAllSelectors() ;
        Document doc = JDOMUtil.stringToDocument(xml);
        JDOMUtil.documentToFile(doc, "c:/temp/selectors.xml");
    }
}
