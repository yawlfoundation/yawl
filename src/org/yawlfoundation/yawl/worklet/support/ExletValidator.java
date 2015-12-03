package org.yawlfoundation.yawl.worklet.support;

import org.yawlfoundation.yawl.util.StringUtil;
import org.yawlfoundation.yawl.worklet.exception.ExletAction;
import org.yawlfoundation.yawl.worklet.exception.ExletTarget;
import org.yawlfoundation.yawl.worklet.rdr.RdrConclusion;
import org.yawlfoundation.yawl.worklet.rdr.RdrPrimitive;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author Michael Adams
 * @date 9/03/15
 */
public class ExletValidator {


    public List<ExletValidationError> validate(RdrConclusion conclusion,
                                               Set<String> workletList) {
        if (conclusion == null || conclusion.getCount() == 0) {
            return Collections.emptyList();                    // short circuit
        }
        List<ExletValidationError> errorList = new ArrayList<ExletValidationError>();
        for (int i=1; i <= conclusion.getCount(); i++) {       // index base is 1
            ExletValidationError evError = validatePrimitive(
                    conclusion.getPrimitive(i), workletList, i);
            if (evError != null) errorList.add(evError);
        }
        if (validateSelect(conclusion)) {
            validateSequence(conclusion, errorList);
        }
        else {
            errorList.add(new ExletValidationError(0, "A conclusion with a 'select' " +
                    "action may not contain any other action."));
        }
        return errorList;
    }


    private ExletValidationError validatePrimitive(RdrPrimitive primitive,
                                   Set<String> workletList, int index) {
        ExletAction action = primitive.getExletAction();
        ExletTarget target = primitive.getExletTarget();
        String msg = null;

        if (action.isInvalidAction()) {
            msg = "Invalid action specified [Item " + index + "]";
        }
        else if (action == ExletAction.Rollback) {
            msg = "Unsupported action: 'rollback' [Item " + index + "]";
        }

        // check 'compensate' and select have a valid worklet name
        else if (action.isWorkletAction()) {
            if (target != ExletTarget.Invalid) {
                msg = "Target '" + target.toString() + "' is invalid for action '" +
                        action.toString() + "' [Item " + index + "]";
            }
            else {
                String worklet = primitive.getTarget();
                if (StringUtil.isNullOrEmpty(worklet) ||            // no worklet name
                        worklet.equals(ExletAction.Invalid.toString())) {
                    msg = "Action '" + action.toString() + "' is missing a valid " +
                            "worklet target value [Item " + index + "]";
                }
                else {
                    for (String part : worklet.split(";")) {
                        if (!workletList.contains(part)) {
                            msg = "Unknown worklet identifier '" + worklet +
                                    "' [Item " + index + "]";
                        }
                    }
                }
            }
        }
        else if (action.isItemOnlyAction() && target != ExletTarget.Workitem) {
            msg = "Target '" + target.toString() + "' is invalid for action '" +
                    action.toString() + "' [Item " + index + "]";
        }

        // action is OK, is target valid?
        else if (target.isInvalidTarget()) {
            msg = "Invalid target specified [Item " + index + "]";
        }

        return msg != null ? new ExletValidationError(index, msg) : null;
    }


    // either all are select, or none are
    private boolean validateSelect(RdrConclusion conclusion) {
        boolean allAreSelect = true;
        boolean noneAreSelect = true;
        for (int i=1; i <= conclusion.getCount(); i++) {
            ExletAction action = conclusion.getPrimitive(i).getExletAction();
            allAreSelect = allAreSelect && action == ExletAction.Select;
            noneAreSelect = noneAreSelect && action != ExletAction.Select;
        }
        return allAreSelect || noneAreSelect;
    }


    private void validateSequence(RdrConclusion conclusion,
                                  List<ExletValidationError> errorList) {
        ExletState targetState = new ExletState();
        for (int i=1; i <= conclusion.getCount(); i++) {
            ExletAction action = conclusion.getPrimitive(i).getExletAction();
            ExletTarget target = conclusion.getPrimitive(i).getExletTarget();
            ExletAction state = targetState.getState(target);
            String error = null;

            // get current target state;look at action
            if (action == state) {
                error = "Duplicate '" + action.toString() + "' action.";
            }
            else if (isFinalState(state)) {
                StringBuilder s = new StringBuilder();
                s.append("Invalid '").append(action).append("' action. ");
                s.append("Target '").append(target.toString());
                s.append("' will be in a finalized state due to a previous '");
                s.append(state.toString()).append("' action.");
                error = s.toString();
            }
            else if (action == ExletAction.Continue && state != ExletAction.Suspend) {
                error = "Invalid 'continue' action. Target '" + target.toString() +
                        "' has not been previously suspended";
            }
            else {
                targetState.setState(action, target);
            }

            if (error != null) {
                errorList.add(new ExletValidationError(i, error + " [Item " + i + "]"));
            }
        }

        for (ExletTarget target : ExletTarget.values()) {
            if (targetState.getState(target) == ExletAction.Suspend) {
                errorList.add(new ExletValidationError(0, "Target '" + target.toString() +
                        "' is left in a suspended state when this exlet completes."));
            }
        }
    }


    private boolean isFinalState(ExletAction action) {
        switch (action) {
            case Remove:
            case Complete:
            case Fail: return true;
        }
        return false;
    }


    /*********************************************************************************/

    class ExletState {

        // actions taken against each target
        ExletAction caseState = ExletAction.Invalid;
        ExletAction ancestorCasesState = ExletAction.Invalid;
        ExletAction allCasesState = ExletAction.Invalid;
        ExletAction workitemState = ExletAction.Invalid;


        ExletAction getState(ExletTarget target) {
            switch (target) {
                case Case: return caseState;
                case AllCases: return allCasesState;
                case AncestorCases: return ancestorCasesState;
                case Workitem: return workitemState;
            }
            return ExletAction.Invalid;
        }


        void setState(ExletAction action, ExletTarget target) {
            switch (target) {
                case AllCases: allCasesState = action;            // deliberate fallthrough
                case AncestorCases: ancestorCasesState = action;  // deliberate fallthrough
                case Case: caseState = action; break;
                case Workitem: workitemState = action; break;
            }
        }

    }

}
