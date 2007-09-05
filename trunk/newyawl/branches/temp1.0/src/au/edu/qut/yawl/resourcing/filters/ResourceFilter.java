package au.edu.qut.yawl.resourcing.filters;

import au.edu.qut.yawl.resourcing.AbstractSelector;

import java.util.*;

import org.jdom.Element;

/**
     *
     * A well as a meaningful class name, extending classes need to provide values for:
     *    _name --> a name for the constraint
     *    _description --> a user-oriented description of the constraint
     *    _keys --> a set of parameter names needed when applying the constraint
     */
public abstract class ResourceFilter extends AbstractSelector {

        public static final int ORGANISATION_TYPE = 0;
        public static final int CAPABILITY_TYPE = 1 ;
        public static final int HISTORY_TYPE = 2 ;

        protected int _filterType ;

        /** Constructors */

        public ResourceFilter() {}                           // for reflection

        public ResourceFilter(String name) {
            _name = name ;
        }

        public ResourceFilter(String name, HashMap params) {
            _name = name ;
            _params = params ;
        }

        public ResourceFilter(String name, HashMap params, String desc) {
            _name = name ;
            _params = params ;
            _description = desc ;
        }

        public ResourceFilter(String name, HashMap params, String desc, Set keys) {
            _name = name ;
            _params = params ;
            _description = desc ;
            _keys = keys ;
        }



        public int getFilterType() { return _filterType; }

        public void setFilterType(int fType) { _filterType = fType ; }

        public String toXML() {
            StringBuilder result = new StringBuilder("<filter>");
            result.append(super.toXML());
            result.append("</filter>");
            return result.toString();
        }


        public static ResourceFilter unmarshal(Element elFilter) {
            ResourceFilter filter =
                FilterFactory.getInstance(elFilter.getChildText("name")) ;
            Element eParams = elFilter.getChild("params");
            if (eParams != null)
                filter.setParams(unmarshalParams(eParams));
            return filter ;
        }


        /** abstract method */

        public abstract Set performFilter(Set resources) ;

    }

