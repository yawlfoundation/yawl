/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are committed to improving
 * workflow technology.
 */

package org.yawlfoundation.yawl.resourcing.allocators;

import org.jdom.Element;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.resourcing.AbstractSelector;
import org.yawlfoundation.yawl.resourcing.datastore.persistence.Persister;
import org.yawlfoundation.yawl.resourcing.datastore.eventlog.EventLogger;
import org.yawlfoundation.yawl.resourcing.resource.Participant;

import java.util.HashMap;
import java.util.Set;
import java.util.List;

/**
 * The base class for all allocators.
 *
 * Create Date: 03/08/2007. Last Date: 09/11/2007
 *
 *  @author Michael Adams (BPM Group, QUT Australia)
 *  @version 2.0
 */

public abstract class AbstractAllocator extends AbstractSelector {

    /** Constructors */

    public AbstractAllocator() { super(); }
    

    public AbstractAllocator(String name) {
        super(name);
    }


    public AbstractAllocator(String name, HashMap<String,String> params) {
       super(name, params) ;
    }


    public AbstractAllocator(String name, String description) {
       super(name, description) ;
    }


    public AbstractAllocator(String name, String desc, HashMap<String,String> params) {
        _name = name ;
        _params = params ;
        _description = desc ;
    }

    /*******************************************************************************/

    /**
     * Generates the XML required for the specification file
     * @return an XML'd String containing the member values of an instantiation of this
     *         class
     */
    public String toXML() {
        StringBuilder result = new StringBuilder("<allocator>");
        result.append(super.toXML());
        result.append("</allocator>");
        return result.toString();
    }

    /**
     * Creates a runtime instance of this class from an XML description of it
     * @param elAllocator a JDOM Element describing the class
     * @return the instantiated object
     */
    public static AbstractAllocator unmarshal(Element elAllocator) {
        AbstractAllocator allocator =
                AllocatorFactory.getInstance(elAllocator.getChildText("name")) ;
        Element eParams = elAllocator.getChild("params");
        if (eParams != null)
            allocator.setParams(unmarshalParams(eParams));
        return allocator ;
    }


    /**
     * Gets a list of all resource log rows for a given specification + task + event
     * combination.
     * @param wir a workitem record which is an instance of the specification+task in
     * question.
     * @param event the type of event to get reocrds for
     * @return the matching list of ResourceEvent objects (each one a row of the log)
     */
    protected List getLoggedEvents(WorkItemRecord wir, EventLogger.event event) {
        Persister persister = Persister.getInstance() ;
        if (persister != null) {
            YSpecificationID specID = new YSpecificationID(wir);
            String keyField = (specID.getIdentifier() != null) ? "identifier" : "uri";
            String eventStr = event.name();
            String taskID = wir.getTaskIDForDisplay();
            return persister.selectWhere("ResourceEvent",
                  String.format("_event='%s' AND tbl._specID.%s='%s' AND " +
                                "tbl._specID.version.version='%s' AND tbl._taskID='%s'",
                                eventStr, keyField, specID.getKey(),
                                specID.getVersionAsString(), taskID)) ;
        }
        else return null;
    }


    /*******************************************************************************/

    // ABSTRACT METHOD - to be implemented by extending classes //

    /**
     * Performs an allocation using some strategy
     * @param resources the distribution set of participants
     * @param wir the work item to allocate
     * @return the Participant chosen by the allocation strategy
     */
    public abstract Participant performAllocation(Set<Participant> resources,
                                                  WorkItemRecord wir) ;

    /*******************************************************************************/


}
