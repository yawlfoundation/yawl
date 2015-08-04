package org.yawlfoundation.yawl.util;

import org.yawlfoundation.yawl.elements.data.YParameter;
import org.yawlfoundation.yawl.resourcing.allocators.AbstractAllocator;
import org.yawlfoundation.yawl.resourcing.codelets.AbstractCodelet;
import org.yawlfoundation.yawl.resourcing.constraints.AbstractConstraint;
import org.yawlfoundation.yawl.resourcing.datastore.orgdata.DataSource;
import org.yawlfoundation.yawl.resourcing.filters.AbstractFilter;
import org.yawlfoundation.yawl.resourcing.jsf.dynform.dynattributes.AbstractDynAttribute;

import java.util.*;

/**
 * @author Michael Adams
 * @date 19/04/2014
 */
public abstract class AbstractPluginFactory {

    private static String _externalPaths;

    protected AbstractPluginFactory() { }


    public static void setExternalPaths(String externalPaths) {
        _externalPaths = externalPaths;
    }


    protected static <T> Map<String, Class<T>> load(Class<T> clazz) {
        return new YPluginLoader(_externalPaths).loadAsMap(clazz);
    }


    protected static <T> T loadInstance(Class<T> clazz, String instanceName) {
        return new YPluginLoader(_externalPaths).getInstance(clazz, instanceName);
    }


    protected static <T> T newInstance(Class<T> clazz) {
        try {
            return clazz.newInstance();
        }
        catch (Throwable t) {
            return null;
        }
    }


    protected static <T> Set<T> toInstanceSet(Collection<Class<T>> clazzSet) {
        Set<T> instanceSet = new HashSet<T>();
        for (Class<T> clazz : clazzSet) {
            try {
                instanceSet.add(clazz.newInstance());
            }
            catch (Throwable t) {
                // do nothing
            }
        }
        return instanceSet;
    }

}
