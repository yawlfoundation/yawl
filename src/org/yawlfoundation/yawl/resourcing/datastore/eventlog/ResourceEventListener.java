package org.yawlfoundation.yawl.resourcing.datastore.eventlog;

/**
 * @author Michael Adams
 * @date 26/2/17
 */
public interface ResourceEventListener {

    void eventOccurred(ResourceEvent event);
}
