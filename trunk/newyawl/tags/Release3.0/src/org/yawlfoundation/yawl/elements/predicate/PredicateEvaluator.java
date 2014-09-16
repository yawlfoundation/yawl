package org.yawlfoundation.yawl.elements.predicate;

import org.yawlfoundation.yawl.elements.YDecomposition;
import org.yawlfoundation.yawl.elements.state.YIdentifier;

/**
 * @author Michael Adams
 * @date 5/12/12
 */
public interface PredicateEvaluator {

    public boolean accept(String predicate);

    public boolean evaluate(YDecomposition decomposition, String predicate, YIdentifier token);

}
