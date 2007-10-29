package au.edu.qut.yawl.resourcing.constraints;

import au.edu.qut.yawl.resourcing.resource.Participant;
import org.jdom.Element;

import java.util.Set;

/**
 * Created by IntelliJ IDEA. User: Default Date: 14/08/2007 Time: 14:50:48 To change this
 * template use File | Settings | File Templates.
 */
public class GenericConstraint extends AbstractConstraint {

    public GenericConstraint(String name) {
        super(name);
    }

    public GenericConstraint() {}


    public Set<Participant> performConstraint(Set<Participant> resources) { return null; }


}
