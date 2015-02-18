package org.yawlfoundation.yawl.elements.predicate;

import org.yawlfoundation.yawl.elements.YDecomposition;
import org.yawlfoundation.yawl.elements.state.YIdentifier;

/**
 * @author Michael Adams
 * @date 5/12/12
 */
public interface PredicateEvaluator {

    boolean accept(String predicate);

    String substituteDefaults(String predicate);

    String replace(YDecomposition decomposition, String predicate, YIdentifier token);

}
