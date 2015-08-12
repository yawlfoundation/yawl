package org.yawlfoundation.yawl.resourcing;

import junit.framework.TestCase;

import org.yawlfoundation.yawl.resourcing.util.PluginFactory;
import org.yawlfoundation.yawl.util.JDOMUtil;
import org.jdom2.Document;
import org.yawlfoundation.yawl.util.YPluginLoader;

/**
 *
 * @author Michael Adams
 * Date: 01/08/2007
 *
 */
public class TestGetSelectors extends TestCase {

    public void testGetSelectors() {
         String xml = PluginFactory.getAllSelectors();
        Document doc = JDOMUtil.stringToDocument(xml);
        JDOMUtil.documentToFile(doc, "c:/temp/selectors.xml");
    }
}
