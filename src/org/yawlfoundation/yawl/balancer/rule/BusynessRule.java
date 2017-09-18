package org.yawlfoundation.yawl.balancer.rule;

/**
 * @author Michael Adams
 * @date 14/9/17
 */
public interface BusynessRule {

    void add(double value);

    double get();
}
