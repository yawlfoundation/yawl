package org.yawlfoundation.yawl.elements.pdoj;

import org.yawlfoundation.yawl.elements.YExternalNetElement;
import org.yawlfoundation.yawl.elements.state.YIdentifier;

/**
 * Implementers determine if an OR-join is enabled, according to their particular semantics
 * @author Michael Adams
 * @date 09/10/2026
 */
public interface OrJoinEvaluator {

    boolean isOrJoinEnabled(YExternalNetElement orJoin, YIdentifier caseID);
}
