package au.edu.qut.yawl.resourcing.filters;

import java.util.Set;

/**
 * Created by IntelliJ IDEA. User: Default Date: 14/08/2007 Time: 14:52:31 To change this
 * template use File | Settings | File Templates.
 */
public class GenericFilter extends AbstractFilter {

    public GenericFilter(String name) { super(name) ; }

    public GenericFilter() {}


    public Set performFilter(Set resources) { return null; }

    public Set getParamKeys() { return null; }
}
