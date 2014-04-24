package org.yawlfoundation.yawl.resourcing.util;

import org.yawlfoundation.yawl.elements.data.YParameter;
import org.yawlfoundation.yawl.resourcing.allocators.AbstractAllocator;
import org.yawlfoundation.yawl.resourcing.codelets.AbstractCodelet;
import org.yawlfoundation.yawl.resourcing.constraints.AbstractConstraint;
import org.yawlfoundation.yawl.resourcing.datastore.orgdata.DataSource;
import org.yawlfoundation.yawl.resourcing.filters.AbstractFilter;
import org.yawlfoundation.yawl.resourcing.jsf.dynform.dynattributes.AbstractDynAttribute;
import org.yawlfoundation.yawl.resourcing.jsf.dynform.dynattributes.DynAttributeFactory;
import org.yawlfoundation.yawl.util.AbstractPluginFactory;
import org.yawlfoundation.yawl.util.StringUtil;
import org.yawlfoundation.yawl.util.YPluginLoader;

import java.util.*;

/**
 * @author Michael Adams
 * @date 19/04/2014
 */
public class PluginFactory extends AbstractPluginFactory {

    private static Map<String, Class<AbstractFilter>> _filters;
    private static Map<String, Class<AbstractConstraint>> _constraints;
    private static Map<String, Class<AbstractAllocator>> _allocators;
    private static Map<String, Class<AbstractCodelet>> _codelets;

    private final static String BASE_PACKAGE = "org.yawlfoundation.yawl.resourcing.";

    private PluginFactory() { }


    public static Set<AbstractConstraint> getConstraints() {
        return toInstanceSet(getConstraintMap().values());
    }


    public static Set<AbstractFilter> getFilters() {
        return toInstanceSet(getFilterMap().values());
    }


    public static Set<AbstractAllocator> getAllocators() {
        return toInstanceSet(getAllocatorMap().values());
    }


    public static Set<AbstractCodelet> getCodelets() {
        return toInstanceSet(getCodeletMap().values());
    }

    public static Set<AbstractDynAttribute> getDynAttributes() {
        return toInstanceSet(load(AbstractDynAttribute.class).values());
    }


    public static AbstractConstraint newConstraintInstance(String name) {
        name = qualify(name, "constraints.");
        Class<AbstractConstraint> constraint = getConstraintMap().get(name);
        return (constraint != null) ? newInstance(constraint) :
                loadInstance(AbstractConstraint.class, name);
    }


    public static AbstractFilter newFilterInstance(String name) {
        name = qualify(name, "filters.");
        Class<AbstractFilter> filter = getFilterMap().get(name);
        return (filter != null) ? newInstance(filter) :
                loadInstance(AbstractFilter.class, name);
    }


    public static AbstractAllocator newAllocatorInstance(String name) {
        name = qualify(name, "allocators.");
        Class<AbstractAllocator> allocator = getAllocatorMap().get(name);
        return (allocator != null) ? newInstance(allocator) :
                loadInstance(AbstractAllocator.class, name);
    }


    public static AbstractCodelet newCodeletInstance(String name) {
        name = qualify(name, "codelets.");
        Class<AbstractCodelet> codelet = getCodeletMap().get(name);
        return (codelet != null) ? newInstance(codelet) :
                loadInstance(AbstractCodelet.class, name);
    }


    public static DataSource newDataSourceInstance(String name) {
        return loadInstance(DataSource.class, qualify(name, "datastore.orgdata."));
    }



    public static String getConstraintsAsXML() {
        StringBuilder result = new StringBuilder("<constraints>");
        for (AbstractConstraint constraint : getConstraints()) {
            result.append(constraint.getInformation("constraint"));
        }
        result.append("</constraints>");
        return result.toString();
    }

    public static String getFiltersAsXML() {
        StringBuilder result = new StringBuilder("<filters>");
        for (AbstractFilter filter : getFilters()) {
            result.append(filter.getInformation("filter"));
        }
        result.append("</filters>");
        return result.toString();
    }

    public static String getAllocatorsAsXML() {
        StringBuilder result = new StringBuilder("<allocators>");
        for (AbstractAllocator allocator : getAllocators()) {
            result.append(allocator.getInformation("allocator"));
        }
        result.append("</allocators>");
        return result.toString();
    }

    public static String getAllSelectors() {
        StringBuilder xml = new StringBuilder("<selectors>");
        xml.append(getConstraintsAsXML());
        xml.append(getFiltersAsXML());
        xml.append(getAllocatorsAsXML());
        xml.append("</selectors>");
        return xml.toString();
    }


    public static String getCodeletsAsXML() {
        StringBuilder result = new StringBuilder("<codelets>");
        for (AbstractCodelet codelet : getCodelets()) result.append(codelet.toXML());
        result.append("</codelets>");
        return result.toString();

    }

    public static List<YParameter> getCodeletParameters(String codeletName) {
        AbstractCodelet codelet = newCodeletInstance(codeletName);
        return (codelet != null) ? codelet.getRequiredParams() : null;
    }


    public static String getCodeletParametersAsXML(String codeletName) {
        AbstractCodelet codelet = newCodeletInstance(codeletName);
        return (codelet != null) ? codelet.getRequiredParamsToXML() :
                StringUtil.wrap("Could not locate codelet: '" + codeletName + "'.", "failure");
    }


    /****************************************************************************/

    private static Map<String, Class<AbstractCodelet>> getCodeletMap() {
        if (_codelets == null) {
            _codelets = load(AbstractCodelet.class);
        }
        return _codelets;
    }

    private static Map<String, Class<AbstractConstraint>> getConstraintMap() {
        if (_constraints == null) {
            _constraints = load(AbstractConstraint.class);
        }
        return _constraints;
    }

    private static Map<String, Class<AbstractFilter>> getFilterMap() {
        if (_filters == null) {
            _filters = load(AbstractFilter.class);
        }
        return _filters;
    }

    private static Map<String, Class<AbstractAllocator>> getAllocatorMap() {
        if (_allocators == null) {
            _allocators = load(AbstractAllocator.class);
        }
        return _allocators;
    }

    private static String qualify(String className, String pkg) {
        return (! (className == null || className.contains("."))) ?
            BASE_PACKAGE + pkg + className : className;
    }

}
