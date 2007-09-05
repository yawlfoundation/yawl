package au.edu.qut.yawl.resourcing.interactions;

import org.jdom.Element;

/**
 * Created by IntelliJ IDEA. User: Default Date: 9/07/2007 Time: 15:09:42 To change this
 * template use File | Settings | File Templates.
 */
public class StartInteraction extends AbstractInteraction {

    public StartInteraction(int initiator) {
        super(initiator) ;
    }

    public StartInteraction() { super(); }
    

    public void parse(Element e) {
        if (e != null) parseInitiator(e) ;
    }


    public String toXML() {
        StringBuilder xml = new StringBuilder("<start>");
        xml.append("<initiator>").append(getInitiatorString()).append("</initiator>");
        xml.append("</start>");
        return xml.toString();
    }

}
