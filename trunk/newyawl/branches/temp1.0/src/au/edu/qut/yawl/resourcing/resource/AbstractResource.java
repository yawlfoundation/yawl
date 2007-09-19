/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are commited to improving
 * workflow technology.
 */

package au.edu.qut.yawl.resourcing.resource;

/**
 * An abstract class representing a resource entity.
 *
 *  @author Michael Adams
 *  BPM Group, QUT Australia
 *  m3.adams@qut.edu.au
 *  v0.1, 03/08/2007
 */

public abstract class AbstractResource {

    protected String _resourceID;
    protected boolean _isAvailable = true;
    protected String _description ;
    protected String _notes ;


    protected AbstractResource() {}


    public String getID() { return _resourceID; }

    public void setID(String id) { _resourceID = id ; }      


    public boolean isAvailable() { return _isAvailable;  }

    public void setAvailable(boolean available) { _isAvailable = available; }

    public String getNotes() { return _notes; }

    public void setNotes(String notes) {  _notes = notes; }

    public String getDescription() { return _description;  }

    public void setDescription(String desc) {_description = desc; }

    protected String wrap(String core, String tag) {
       return String.format("<%s>%s</%s>", tag, core, tag) ;
   }
    
}
