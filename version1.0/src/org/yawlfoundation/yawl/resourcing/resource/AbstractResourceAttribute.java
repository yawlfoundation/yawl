/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are committed to improving
 * workflow technology.
 */

package org.yawlfoundation.yawl.resourcing.resource;

import org.jdom.Element;

import java.util.HashSet;
import java.util.Set;


/**
 * An abstract class representing a resource attribute.
 *
 * Extended by Role, Capability, Position and OrgGroup classes.
 *
 *  @author Michael Adams
 *  BPM Group, QUT Australia
 *  m3.adams@yawlfoundation.org
 *  v0.1, 21/08/2007
 */

public abstract class AbstractResourceAttribute {

    protected String _id;
    protected String _description ;
    protected String _notes ;

    // the set of resources that have this attribute
    protected HashSet<AbstractResource> _resources = new HashSet<AbstractResource>() ;


    protected AbstractResourceAttribute() {}


    public String getID() { return _id; }

    public void setID(String id) { _id = id ; }

    public String getNotes() { return _notes; }

    public void setNotes(String notes) {  _notes = notes; }

    public String getDescription() { return _description;  }

    public void setDescription(String desc) {_description = desc; }

    public void addResource(AbstractResource resource) { _resources.add(resource); }

    public void removeResource(AbstractResource resource) { _resources.remove(resource); }

    public boolean hasResource(AbstractResource resource) {
        return _resources.contains(resource);
    }

    public Set getResources() { return _resources ; }

    protected String wrap(String core, String tag) {
       return String.format("<%s>%s</%s>", tag, core, tag) ;
    }

    public void reconstitute(Element e) {
        setID(e.getAttributeValue("id"));
        setDescription(e.getChildText("description"));
     }


    // hibernate mappings

    protected Set get_resources() { return _resources; }

    protected void set_resources(Set resources) {

        // add members one at a time to provide cast of Object to AbstractResource
        for (Object o : resources)
             _resources.add((AbstractResource) o);
    }
}
