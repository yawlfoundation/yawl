/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are committed to improving
 * workflow technology.
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

    private ResourceManager _resMgr = ResourceManager.getInstance() ;

    protected boolean _persisting ;

    protected AbstractResourceAttribute() {}

    protected void updateThis() {
        if (_persisting) _resMgr.updateResourceAttribute(this);
    }

    public void save() { _resMgr.updateResourceAttribute(this); }

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
        updateThis();
    }

    public String getDescription() { return _description;  }

    public void setDescription(String desc) {
        _description = desc;
        updateThis();
    }

    public void addResource(AbstractResource resource) {
        _resources.add(resource);
        updateThis();
    }

    public void removeResource(AbstractResource resource) {
        _resources.remove(resource);
        updateThis();
    }

    public boolean hasResource(AbstractResource resource) {
        return _resources.contains(resource);
    }

    public Set getResources() { return _resources ; }


    public void fromXML(String xml) {
        if (xml != null) reconstitute(JDOMUtil.stringToElement(xml));
    }

    
    public void reconstitute(Element e) {
        setID(e.getAttributeValue("id"));
        setDescription(e.getChildText("description"));
        setNotes(e.getChildText("notes"));
     }


    // hibernate mappings

    protected Set get_resources() { return _resources; }

    protected void set_resources(Set resources) {

        // add members one at a time to provide cast of Object to AbstractResource
        for (Object o : resources)
             _resources.add((AbstractResource) o);
    }
}
