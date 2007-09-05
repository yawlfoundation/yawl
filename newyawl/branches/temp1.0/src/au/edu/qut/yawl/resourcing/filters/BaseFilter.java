package au.edu.qut.yawl.resourcing.filters;

import java.util.Set;

/**
 * Created by IntelliJ IDEA. User: Default Date: 14/08/2007 Time: 14:52:31 To change this
 * template use File | Settings | File Templates.
 */
public class BaseFilter extends ResourceFilter {

    public BaseFilter(String name) { super(name) ; }


    public Set performFilter(Set resources) { return null; }

    public Set getParamKeys() { return null; }
}
