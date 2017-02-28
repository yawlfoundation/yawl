package org.yawlfoundation.yawl.resourcing.datastore.eventlog;

import org.yawlfoundation.yawl.engine.YSpecificationID;

/**
 * @author Michael Adams
 * @date 26/2/17
 */
public interface ResourceEventListener {

    void eventOccurred(YSpecificationID specID, ResourceEvent event);
}
