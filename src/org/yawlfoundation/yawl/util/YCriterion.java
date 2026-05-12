package org.yawlfoundation.yawl.util;

/**
 *
 * @author Michael Adams
 * @date 17/4/2026
 */
@FunctionalInterface
public interface YCriterion {
    jakarta.persistence.criteria.Predicate toPredicate(
            jakarta.persistence.criteria.Root<?> root,
            jakarta.persistence.criteria.CriteriaBuilder builder
    );
}
