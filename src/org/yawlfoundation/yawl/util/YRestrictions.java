package org.yawlfoundation.yawl.util;

/**
 *
 * @author Michael Adams
 * @date 17/4/2026
 */
public class YRestrictions {

    public static YCriterion eq(String field, Object value) {
        return ((root, builder) ->
                builder.equal(root.get(field), value));
    }
}
