/*
 * Copyright (c) 2004-2020 The YAWL Foundation. All rights reserved.
 * The YAWL Foundation is a collaboration of individuals and
 * organisations who are committed to improving workflow technology.
 *
 * This file is part of YAWL. YAWL is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation.
 *
 * YAWL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with YAWL. If not, see <http://www.gnu.org/licenses/>.
 */

package org.yawlfoundation.yawl.elements.predicate;

import org.yawlfoundation.yawl.elements.YDecomposition;
import org.yawlfoundation.yawl.elements.state.YIdentifier;

import java.util.Set;

/**
 * @author Michael Adams
 * @date 5/12/12
 */
public class PredicateEvaluatorCache {

    private static Set<PredicateEvaluator> _evaluators;



    public static String process(YDecomposition decomposition, String predicate,
                                    YIdentifier token) {
        PredicateEvaluator evaluator = getEvaluator(predicate);
        while (evaluator != null) {
            predicate = evaluator.replace(decomposition, predicate, token);
            evaluator = getEvaluator(predicate);
        }
        return predicate;
    }


    public static String substitute(String predicate) {
        PredicateEvaluator evaluator = getEvaluator(predicate);
        while (evaluator != null) {
            predicate = evaluator.substituteDefaults(predicate);
            evaluator = getEvaluator(predicate);
        }
        return predicate;
    }


    public static boolean accept(String predicate) {
        return getEvaluator(predicate) != null;
    }


    private static PredicateEvaluator getEvaluator(String predicate) {
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
