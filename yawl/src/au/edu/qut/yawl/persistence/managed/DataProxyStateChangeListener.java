/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package au.edu.qut.yawl.persistence.managed;

import java.beans.PropertyChangeListener;

public interface DataProxyStateChangeListener extends PropertyChangeListener {
    // constants for state changes
    /** Constant indicating the property changing is a component's name. */
    public static final String PROPERTY_NAME = "NAME";
    /** Constant indicating the property changing is a component's ID. */
    public static final String PROPERTY_ID = "ID";
    /** Constant indicating the property changing is a task's variables. */
    public static final String PROPERTY_TASK_VARIABLES = "TASK_VARIABLES";
    /** Constant indicating the property changing is the location of a task (or net element). */
    public static final String PROPERTY_TASK_BOUNDS = "TASK_BOUNDS";
    
    /** Called just before the proxy is detached. */
    void proxyDetaching(DataProxy proxy, Object data, DataProxy parent);
    /** Called immediately after the proxy is detached. */
    void proxyDetached(DataProxy proxy, Object data, DataProxy parent);
    /** Called just before the proxy is attached. */
    void proxyAttaching(DataProxy proxy, Object data, DataProxy parent);
    /** Called immediately after the proxy is attached. */
    void proxyAttached(DataProxy proxy, Object data, DataProxy parent);
}
