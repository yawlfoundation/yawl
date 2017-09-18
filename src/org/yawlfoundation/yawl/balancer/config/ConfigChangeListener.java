package org.yawlfoundation.yawl.balancer.config;

import java.util.Map;

/**
 * @author Michael Adams
 * @date 15/9/17
 */
public interface ConfigChangeListener {

    void configChanged(Map<String, String> newValues);
    
}
