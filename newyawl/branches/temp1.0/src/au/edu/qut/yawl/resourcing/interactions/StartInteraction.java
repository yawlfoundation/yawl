/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are commited to improving
 * workflow technology.
 */

package au.edu.qut.yawl.resourcing.interactions;

import org.jdom.Element;
import org.jdom.Namespace;

/**
 *  This class describes the requirements of a task at the start phase of
 *  allocating resources.
 *
 *  @author Michael Adams
 *  BPM Group, QUT Australia
 *  m3.adams@qut.edu.au
 *  v0.1, 02/08/2007
 */

public class StartInteraction extends AbstractInteraction {

    public StartInteraction(int initiator) {
        super(initiator) ;
    }

    public StartInteraction() { super(); }

    public StartInteraction(String ownerTaskID) { super(ownerTaskID) ; }


    public void parse(Element e, Namespace nsYawl) {
        if (e != null) parseInitiator(e, nsYawl) ;
    }


    public String toXML() {
        StringBuilder xml = new StringBuilder("<start>");
        xml.append("<initiator>").append(getInitiatorString()).append("</initiator>");
        xml.append("</start>");
        return xml.toString();
    }

}
