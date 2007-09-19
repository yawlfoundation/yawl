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

import java.util.*;

import org.jdom.Element;

/**
 * The base class for all constraints. As well as a meaningful class name, extending
 * classes need to provide values for:
 *    _name --> a name for the constraints
 *    _description --> a user-oriented description of the constraints
 *    _params --> a set of parameter needed when applying the constraints (the param value
 *                should be set to null when initialised - see PiledExecution class for an
 *                example.
 *
 *  @author Michael Adams
 *  BPM Group, QUT Australia
 *  m3.adams@qut.edu.au
 *  v0.1, 03/08/2007
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
        _name = name ;
        _params = params ;
        _description = desc ;
    }


    public String toXML() {
        StringBuilder result = new StringBuilder("<constraint>");
        result.append(super.toXML());
        result.append("</constraint>");
        return result.toString();
    }

    public static AbstractConstraint unmarshal(Element elConstraint) {
        AbstractConstraint constraint =
                ConstraintFactory.getInstance(elConstraint.getChildText("name")) ;
        Element eParams = elConstraint.getChild("params");
        if (eParams != null)
            constraint.setParams(unmarshalParams(eParams));
        return constraint ;
    }


    /** abstract methods */

    public abstract Set<Participant> performConstraint(Set<Participant> resources) ;

}
