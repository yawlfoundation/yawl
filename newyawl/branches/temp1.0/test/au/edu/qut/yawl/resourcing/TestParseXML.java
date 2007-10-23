package au.edu.qut.yawl.resourcing;

import junit.framework.TestCase;
import org.jdom.Element;
import org.jdom.Document;
import au.edu.qut.yawl.util.JDOMConversionTools;

/**
 *
 * @author Michael Adams
 * Date: 01/08/2007
 *
 */
public class TestParseXML extends TestCase {

    public void testparseXML(){

        Document doc = JDOMConversionTools.fileToDocument("c:/temp/resourcing2.xml") ;
        Element resElem = doc.getRootElement();

        ResourceMap rMap = new ResourceMap("null") ;
        rMap.parse(resElem);

        String s = rMap.getOfferInteraction().toXML();
        System.out.println(s) ;

        s = rMap.getAllocateInteraction().toXML();
        System.out.println(s) ;

        s = rMap.getStartInteraction().toXML();
        System.out.println(s) ;

        s = rMap.getTaskPrivileges().toXML();
        System.out.println(s) ;
    }
}
