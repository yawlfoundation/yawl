/*
 * Copyright (c) 2004-2010 The YAWL Foundation. All rights reserved.
 * The YAWL Foundation is a collaboration of individuals and
 * organisations who are committed to improving workflow technology.
 *
 * This file is part of YAWL. YAWL is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation.
 *
 * YAWL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with YAWL. If not, see <http://www.gnu.org/licenses/>.
 */

package org.yawlfoundation.yawl.resourcing.resource;

import org.jdom.Element;
import org.yawlfoundation.yawl.util.JDOMUtil;
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

    public Capability(Element e) {
        super();
        reconstitute(e);
    }

    public String getCapability() { return _capability; }

    public void setCapability(String capability) {
        _capability = capability;
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
        xml.append(StringUtil.wrapEscaped(_capability, "name"));
        xml.append(StringUtil.wrapEscaped(_description, "description"));
        xml.append(StringUtil.wrapEscaped(_notes, "notes"));
        xml.append("</capability>");
        return xml.toString() ;
    }

    public void reconstitute(Element e) {
        super.reconstitute(e);
        setCapability(JDOMUtil.decodeEscapes(e.getChildText("name")));
    }
}
