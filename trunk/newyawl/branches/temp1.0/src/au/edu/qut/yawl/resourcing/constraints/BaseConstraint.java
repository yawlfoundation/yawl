package au.edu.qut.yawl.resourcing.constraints;

import java.util.Set;

/**
 * Created by IntelliJ IDEA. User: Default Date: 14/08/2007 Time: 14:50:48 To change this
 * template use File | Settings | File Templates.
 */
public class BaseConstraint extends ResourceConstraint {

    public BaseConstraint(String name) {
        super(name);
    }


    public Set performConstraint(Set resources) { return null; }

    public Set getParamKeys() { return null; }
}
