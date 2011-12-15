/*
 * Copyright (c) 2004-2011 The YAWL Foundation. All rights reserved.
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

/**
 * @author Michael Adams
 * @date 12/12/11
 */
public enum RuleType {
    CasePreconstraint,
    CasePostconstaint,
    ItemPreconstraint,
    ItemPostconstaint,
    ItemAbort,
    ItemTimeout,
    ItemResourceUnavailable,
    ItemConstraintViolation,
    CaseExternalTrigger,
    ItemExternalTrigger,
    ItemSelection;             // 'pseudo' exception type


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
       switch (this) {
           case CasePreconstraint       : return "Pre-case constraint violation" ;
           case CasePostconstaint       : return "Post-case constraint violation";
           case ItemPreconstraint       : return "Workitem pre-constraint violation";
           case ItemPostconstaint       : return "Workitem post-constraint violation";
           case ItemAbort               : return "Workitem abort";
           case ItemTimeout             : return "Workitem timeout";
           case ItemResourceUnavailable : return "Resource Unavailable";
           case ItemConstraintViolation : return "Workitem constraint violation";
           case CaseExternalTrigger     : return "Case-level external trigger";
           case ItemExternalTrigger     : return "Workitem-level external trigger";
           case ItemSelection           : return "Selection";
       }
       return null;
    }

    public String toString() {
        switch(this) {
            case CasePreconstraint       : return "PreCaseConstraint" ;
            case CasePostconstaint       : return "PostCaseConstraint";
            case ItemPreconstraint       : return "ItemPreConstraint";
            case ItemPostconstaint       : return "ItemPostConstraint";
            case ItemAbort               : return "ItemAbort";
            case ItemTimeout             : return "ItemTimeout";
            case ItemResourceUnavailable : return "ResourceUnavailable";
            case ItemConstraintViolation : return "ConstraintViolation";
            case CaseExternalTrigger     : return "CaseExternal";
            case ItemExternalTrigger     : return "ItemExternal";
            case ItemSelection           : return "Selection";
        }
        return null;
    }

}
