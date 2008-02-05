/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are committed to improving
 * workflow technology.
 */

package org.yawlfoundation.yawl.resourcing.interactions;

import org.jdom.Element;
import org.jdom.Namespace;
import org.yawlfoundation.yawl.resourcing.interactions.ResourceParseException;

/**
 *  This class describes the requirements of a task at the start phase of
 *  allocating resources.
 *
 *  @author Michael Adams
 *  v0.1, 02/08/2007
 */

public class StartInteraction extends AbstractInteraction {

    public StartInteraction(int initiator) {
        super(initiator) ;
    }

    public StartInteraction() { super(); }

    public StartInteraction(String ownerTaskID) { super(ownerTaskID) ; }


    public void parse(Element e, Namespace nsYawl) throws ResourceParseException {
        if (e != null) parseInitiator(e, nsYawl) ;
    }


    public String toXML() {
        StringBuilder xml = new StringBuilder("<start>");
        xml.append("<initiator>").append(getInitiatorString()).append("</initiator>");
        xml.append("</start>");
        return xml.toString();
    }

}
