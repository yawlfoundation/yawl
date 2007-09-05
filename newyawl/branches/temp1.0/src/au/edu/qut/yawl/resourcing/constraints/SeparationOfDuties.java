package au.edu.qut.yawl.resourcing.constraints;

import au.edu.qut.yawl.resourcing.constraints.ResourceConstraint;

import java.util.*;

/**
 * Created by IntelliJ IDEA. User: Default Date: 9/07/2007 Time: 15:57:38 To change this
 * template use File | Settings | File Templates.
 */
public class SeparationOfDuties extends ResourceConstraint {


    public SeparationOfDuties() { super(); }

    public SeparationOfDuties(String name) {
        super(name) ;
        setDescription("The Separation of Duties constraint blah blah.");
    }

    public SeparationOfDuties(String name, HashMap params) {
        super(name, params) ;
        setDescription("The Separation of Duties constraint blah blah.");       
    }


    public Set performConstraint(Set l) { return null ;}

    
    public Set getParamKeys() {
        _keys = new HashSet() ;
        _keys.add("familiarTask");
        return _keys ;
    }

}
