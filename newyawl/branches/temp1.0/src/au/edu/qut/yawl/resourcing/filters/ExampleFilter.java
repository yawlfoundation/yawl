package au.edu.qut.yawl.resourcing.filters;

import java.util.*;

/**
 * Created by IntelliJ IDEA. User: Default Date: 10/07/2007 Time: 13:40:12 To change this
 * template use File | Settings | File Templates.
 */

public class ExampleFilter extends ResourceFilter {

    public ExampleFilter(String name) {
        super(name) ;
        setDescription("The Example filter blah blah.");
    }

    public ExampleFilter(String name, HashMap params) {
        super(name, params) ;
        setDescription("The Example filter blah blah.");
    }

    public ExampleFilter() {super();}


    public Set performFilter(Set l) { return null ;}

    public Set getParamKeys() {return null ; }

}
