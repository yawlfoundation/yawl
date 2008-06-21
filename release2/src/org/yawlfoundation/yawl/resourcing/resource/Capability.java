/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are committed to improving
 * workflow technology.
 */

package org.yawlfoundation.yawl.resourcing.resource;

import org.jdom.Element;
import org.yawlfoundation.yawl.util.StringUtil;

/**
 * Represents a capability that may be held by a resource.
 *
 *  @author Michael Adams
 *  v0.1, 03/08/2007
 */

public class Capability extends AbstractResourceAttribute implements Comparable {

    private String _capability ;


    public Capability() { super() ;}

    public Capability(String capability, String description) {
        super();
        _capability = capability;
        _description = description;
    }

    public Capability(String capability, String description, boolean persisting) {
        this(capability, description) ;
        _persisting = persisting ;
    }

    public String getCapability() { return _capability; }

    public void setCapability(String capability) {
        _capability = capability;
        updateThis();
    }

    public boolean equals(Object o) {
        return (o instanceof Capability) && ((Capability) o).getID().equals(_id);
    }

    public int compareTo(Object o) {
        if ((o == null) || (! (o instanceof Capability))) return 1;
        return this.getCapability().compareTo(((Capability) o).getCapability());
    }

   
    public String toXML() {
        StringBuilder xml = new StringBuilder() ;
        xml.append(String.format("<capability id=\"%s\">", _id)) ;
        xml.append(StringUtil.wrap(StringUtil.xmlEncode(_capability), "name"));
        xml.append(StringUtil.wrap(StringUtil.xmlEncode(_description), "description"));
        xml.append(StringUtil.wrap(StringUtil.xmlEncode(_notes), "notes"));
        xml.append("</capability>");
        return xml.toString() ;
    }

    public void reconstitute(Element e) {
        super.reconstitute(e);
        setCapability(StringUtil.xmlDecode(e.getChildText("name")));
    }
}
