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
import org.yawlfoundation.yawl.resourcing.ResourceManager;
import org.yawlfoundation.yawl.util.JDOMUtil;

import java.util.HashSet;
import java.util.Set;


/**
 * An abstract class representing a resource attribute.
 *
 * Extended by Role, Capability, Position and OrgGroup classes.
 *
 *  @author Michael Adams
 *  v0.1, 21/08/2007
 */

public abstract class AbstractResourceAttribute {

    protected String _id;
    protected String _description ;
    protected String _notes ;

    // the set of resources that have this attribute
    protected HashSet<AbstractResource> _resources = new HashSet<AbstractResource>() ;

    protected ResourceManager _resMgr = ResourceManager.getInstance() ;

    protected boolean _persisting ;

    protected AbstractResourceAttribute() {}


    public void save() { _resMgr.getOrgDataSet().updateResourceAttribute(this); }

    public void setPersisting(boolean persisting) {
        _persisting = persisting ;
    }

    public boolean isPersisting() { return _persisting; }

    public String getID() { return _id; }

    public void setID(String id) {
        _id = id ;
    }

    public String getNotes() { return _notes; }

    public void setNotes(String notes) {
        _notes = notes;
    }

    public String getDescription() { return _description;  }

    public void setDescription(String desc) {
        _description = desc;
    }

    public void addResource(AbstractResource resource) {
        _resources.add(resource);
    }

    public void removeResource(AbstractResource resource) {
        _resources.remove(resource);
    }

    public boolean hasResource(AbstractResource resource) {
        return _resources.contains(resource);
    }

    public Set<AbstractResource> getResources() { return _resources ; }


    public void fromXML(String xml) {
        if (xml != null) reconstitute(JDOMUtil.stringToElement(xml));
    }

    
    public void reconstitute(Element e) {
        setID(e.getAttributeValue("id"));
        setDescription(JDOMUtil.decodeEscapes(e.getChildText("description")));
        setNotes(JDOMUtil.decodeEscapes(e.getChildText("notes")));
     }


    public int hashCode() {
       return 31 + (_id != null? _id.hashCode() : 0);
    }

    // hibernate mappings

    protected Set get_resources() { return _resources; }

    protected void set_resources(Set resources) {

        // add members one at a time to provide cast of Object to AbstractResource
        for (Object o : resources)
             _resources.add((AbstractResource) o);
    }
}
