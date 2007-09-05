package au.edu.qut.yawl.resourcing.constraints;

import au.edu.qut.yawl.resourcing.constraints.ResourceConstraint;

import java.util.*;

/**
 * Created by IntelliJ IDEA. User: Default Date: 9/07/2007 Time: 16:01:05 To change this
 * template use File | Settings | File Templates.
 */
public class PiledExecution extends ResourceConstraint {

    public PiledExecution() { super(); }

    public PiledExecution(String name) {
        super(name) ;
        setDescription("The Piled Execution constraint blah blah...");
    }

    public PiledExecution(String name, HashMap params) {
        super(name, params) ;
        setDescription("The Piled Execution constraint blah blah...");       
    }

    public Set performConstraint(Set l) { return null ; }

    public Set getParamKeys() {return null ; }

}
