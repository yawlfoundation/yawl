/*
 * Copyright (c) 2004-2012 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.worklet.rdr;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Michael Adams
 * @date 12/12/11
 */
public enum RuleType {
    CasePreconstraint("PreCaseConstraint", "Pre-case constraint violation"),
    CasePostconstaint("PostCaseConstraint", "Post-case constraint violation"),
    ItemPreconstraint("ItemPreConstraint", "Workitem pre-constraint violation"),
    ItemPostconstaint("ItemPostConstraint", "Workitem post-constraint violation"),
    ItemAbort("ItemAbort", "Workitem abort"),
    ItemTimeout("ItemTimeout", "Workitem timeout"),
    ItemResourceUnavailable("ResourceUnavailable", "Resource unavailable"),
    ItemConstraintViolation("ConstraintViolation", "Workitem constraint violation"),
    CaseExternalTrigger("CaseExternal", "Case-level external trigger"),
    ItemExternalTrigger("ItemExternal", "Workitem-level external trigger"),
    ItemSelection("Selection", "Selection");             // 'pseudo' exception type

    private String shortForm;
    private String longForm;

    private static final Map<String, RuleType> fromStringMap =
            new HashMap<String, RuleType>();

    static {
        for (RuleType ruleType : values()) {
            fromStringMap.put(ruleType.toString(), ruleType);
        }
    }

    RuleType(String sForm, String lForm) {
        shortForm = sForm;
        longForm = lForm;
    }

    public boolean isCaseLevelType() {
        switch (this) {
            case CasePreconstraint:
            case CasePostconstaint:
            case CaseExternalTrigger:  return true;
        }
        return false;
    }


    public boolean isItemLevelType() {
        return ! isCaseLevelType();
    }


    /** returns true if the exception type passed occurs for an executing workitem */
    public boolean isExecutingItemType() {
        switch (this) {
            case ItemAbort:
            case ItemTimeout:
            case ItemConstraintViolation:
            case ItemExternalTrigger: return true;
        }
        return false;
    }


    public String toLongString() {
        return longForm;
    }


    public String toString() {
        return shortForm;
    }

    public static RuleType fromString(String s) {
            return (s != null) ? fromStringMap.get(s) : null;
        }


}
