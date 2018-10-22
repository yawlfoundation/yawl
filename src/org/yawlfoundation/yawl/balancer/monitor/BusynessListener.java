package org.yawlfoundation.yawl.balancer.monitor;

import org.yawlfoundation.yawl.balancer.instance.EngineInstance;

/**
 * @author Michael Adams
 * @date 18/10/18
 */
public interface BusynessListener {

    void busynessEvent(EngineInstance instance, double load);

}
