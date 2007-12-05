/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are commited to improving
 * workflow technology.
 */

package au.edu.qut.yawl.resourcing.resource;

import org.jdom.Element;

/**
 * Represents a capability that may be held by a resource.
 *
 *  @author Michael Adams
 *  BPM Group, QUT Australia
 *  m3.adams@qut.edu.au
 *  v0.1, 03/08/2007
 */

public class Capability extends AbstractResourceAttribute {

    private String _capability ;


    public Capability() { super() ;}

    public Capability(String capability, String description) {
        super();
        _capability = capability;
        _description = description;
    }


    public String getCapability() { return _capability; }

    public void setCapability(String capability) { _capability = capability; }

    public String getSummaryXML() {
        StringBuilder xml = new StringBuilder() ;
        xml.append(String.format("<capability id=\"%s\">", _id)) ;
        xml.append(wrap(_capability, "name"));
        xml.append(wrap(_description, "description"));
        xml.append("</capability>");
        return xml.toString() ;
    }

    public void reconstitute(Element e) {
        super.reconstitute(e);
        setCapability(e.getChildText("name"));
    }
}
