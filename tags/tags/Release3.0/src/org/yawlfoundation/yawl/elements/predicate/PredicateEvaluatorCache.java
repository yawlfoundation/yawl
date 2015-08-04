package org.yawlfoundation.yawl.elements.predicate;

import java.util.Set;

/**
 * @author Michael Adams
 * @date 5/12/12
 */
public class PredicateEvaluatorCache {

    private static Set<PredicateEvaluator> _evaluators;

    public static PredicateEvaluator getEvaluator(String predicate) {
        try {
            if (_evaluators == null) {
                _evaluators = PredicateEvaluatorFactory.getInstances();
            }
            for (PredicateEvaluator evaluator : _evaluators) {
                if (evaluator.accept(predicate)) {
                    return evaluator;
                }
            }
        } catch (Exception e) {
            // fall through to null
        }
        return null;
    }

}
