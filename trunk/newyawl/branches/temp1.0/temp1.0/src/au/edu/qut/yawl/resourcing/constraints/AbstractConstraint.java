/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are commited to improving
 * workflow technology.
 */

package au.edu.qut.yawl.resourcing.constraints;

import au.edu.qut.yawl.resourcing.AbstractSelector;
import au.edu.qut.yawl.resourcing.resource.Participant;
import au.edu.qut.yawl.worklist.model.WorkItemRecord;

import org.jdom.Element;

import java.util.HashMap;
import java.util.Set;

/**
 * The base class for all constraints.
 *
 *  Create Date: 03/08/2007. Last Date: 12/11/2007
 *
 *  @author Michael Adams (BPM Group, QUT Australia)
 *  @version 1.0
 */

public abstract class AbstractConstraint extends AbstractSelector {

    /** Constructors */

    public AbstractConstraint() { super(); }           // for reflection

    public AbstractConstraint(String name) {
        super(name) ;
    }

    public AbstractConstraint(String name, HashMap<String,String> params) {
       super(name, params) ;
    }

    public AbstractConstraint(String name, String description) {
       super(name, description) ;
    }


    public AbstractConstraint(String name, String desc, HashMap<String,String> params) {
       super(name, params) ;
        _description = desc ;
    }

    /*******************************************************************************/

    /**
     * Generates the XML required for the specification file
     * @return an XML'd String containing the member values of an instantiation of this
     *         class
     */
    public String toXML() {
        StringBuilder result = new StringBuilder("<constraint>");
        result.append(super.toXML());
        result.append("</constraint>");
        return result.toString();
    }

    /**
     * Creates a runtime instance of this class from an XML description of it
     * @param elConstraint a JDOM Element describing the class
     * @return the instantiated object
     */
    public static AbstractConstraint unmarshal(Element elConstraint) {
        AbstractConstraint constraint =
                ConstraintFactory.getInstance(elConstraint.getChildText("name")) ;
        Element eParams = elConstraint.getChild("params");
        if (eParams != null)
            constraint.setParams(unmarshalParams(eParams));
        return constraint ;
    }

    /*******************************************************************************/

    // ABSTRACT METHOD - to be implemented by extending classes //

    /**
     * Performs a constraint against the distribution set passed
     * @param resources the distribution set of participants
     * @param wir the workitem being resourced
     * @return the constrined distribution set
     */
    public abstract Set<Participant> performConstraint(Set<Participant> resources,
                                                       WorkItemRecord wir) ;

    /*******************************************************************************/

}
