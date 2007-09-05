package au.edu.qut.yawl.resourcing.constraints;


import au.edu.qut.yawl.resourcing.AbstractSelector;

import java.util.*;

import org.jdom.Element;

/**
 *
 * As well as a meaningful class name, extending classes need to provide values for:
 *    _name --> a name for the constraint
 *    _description --> a user-oriented description of the constraint
 *    _keys --> a set of parameter names needed when applying the constraint 
 */

public abstract class ResourceConstraint extends AbstractSelector {

    /** Constructors */

    public ResourceConstraint() { super(); }

    public ResourceConstraint(String name) {
        _name = name ;
    }

    public ResourceConstraint(String name, HashMap params) {
        _name = name ;
        _params = params ;
    }

    public ResourceConstraint(String name, HashMap params, String desc) {
        _name = name ;
        _params = params ;
        _description = desc ;
    }

    public ResourceConstraint(String name, HashMap params, String desc, Set keys) {
        _name = name ;
        _params = params ;
        _description = desc ;
        _keys = keys ;
    }


    public String toXML() {
        StringBuilder result = new StringBuilder("<constraint>");
        result.append(super.toXML());
        result.append("</constraint>");
        return result.toString();
    }

    public static ResourceConstraint unmarshal(Element elConstraint) {
        ResourceConstraint constraint =
                ConstraintFactory.getInstance(elConstraint.getChildText("name")) ;
        Element eParams = elConstraint.getChild("params");
        if (eParams != null)
            constraint.setParams(unmarshalParams(eParams));
        return constraint ;
    }


    /** abstract methods */

    public abstract Set performConstraint(Set resources) ;

}
