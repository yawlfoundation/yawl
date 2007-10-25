package au.edu.qut.yawl.resourcing;

import junit.framework.TestCase;

import au.edu.qut.yawl.util.JDOMConversionTools;
import org.jdom.Document;

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
        Document doc = JDOMConversionTools.stringToDocument(xml);
        JDOMConversionTools.documentToFile(doc, "c:/temp/selectors.xml");
    }
}
