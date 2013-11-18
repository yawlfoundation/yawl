/*
 * Copyright (c) 2004-2013 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.editor.ui.engine;

import org.yawlfoundation.yawl.editor.ui.specification.SpecificationModel;
import org.yawlfoundation.yawl.elements.YExternalNetElement;
import org.yawlfoundation.yawl.elements.YNet;
import org.yawlfoundation.yawl.elements.YTask;

import java.util.*;

/**
 * @author Michael Adams
 * @date 16/10/13
 */
public class ValidationResultsParser {

    public ValidationResultsParser() { }

    public List<ValidationMessage> parse(List<String> messages) {
        if (messages == null) return Collections.emptyList();

        List<ValidationMessage> messageList = new ArrayList<ValidationMessage>();
        for (String message : messages) {
             messageList.add(parse(message));
        }
        Collections.sort(messageList);
        return messageList;
    }


    private ValidationMessage parse(String message) {
        if (message.equals("Specifications must have a root net.")) {
            return new ValidationMessage("Error: No root net specified.",
                    "A specification must have exactly one net that is denoted as the" +
                    " 'root' net. " + canSet("root net"));
        }
        if (message.contains("to complete without any generated work")) {
            return new ValidationMessage("Warning: Net '"
                    + extractLabel(message) +
                    "' performs no work.", "To generate a work item at runtime, a task " +
                    "must be associated with a decomposition. There are currently no " +
                    "tasks in the net that have a decomposition. This will mean the net" +
                    " will complete without having performed any work. " +
                    canSet("task decomposition"));
        }
        if (message.contains("infinite loop/recursion")) {
            return new ValidationMessage("Error: Element '" +
                    extractLabel(message, ':', ')') + "' is in an infinite loop.",
                    "The element plays a part in an infinite loop/recursion in which " +
                    "no work items are created, which will result at runtime in a process " +
                    "that cannot complete. Add a task with a decomposition to the loop " +
                    "or modify the control flow of that part of the net.");
        }
        if (message.contains("has timer settings but no decomposition")) {
            return new ValidationMessage("Warning: Task '"
                    + extractLabel(message) +
                    "' has invalid timer settings.", message + canSet("decomposition"));
        }
        if (message.startsWith("The type library (Schema) in specification contains")) {
            return new ValidationMessage("Error: Missing data type definition '"
                    + extractLabel(message, '[', ']') + "'.", message);
        }
        if (message.contains("forward directed path")) {
            return new ValidationMessage("Error: " + extractLabel(message, 0, ' ') +
                    " is disconnected.",
                    "Every task and condition must be connected on some path between " +
                    "the input and output conditions of a net. There is no path " +
                    "that can be traced from the input condition to the " +
                    "output condition that includes this element.");
        }
        if (message.contains("backward directed path")) {
            return new ValidationMessage("Error: " + extractLabel(message, 0, ' ') +
                    " is disconnected.",
                    "Every task and condition must be connected on some path between " +
                    "the input and output conditions of a net. There is no path " +
                    "that can be traced from the output condition back to the " +
                    "input condition that includes this element.");
        }
        if (message.contains("postset size must be > 0")) {
            return new ValidationMessage("Error: " + extractLabel(message, 0, ' ') +
                    " has no outgoing flows.",
                    "Every task and condition must be connected on some path between the " +
                    "input and output conditions of a net, and therefore must have at " +
                    "least one outgoing flow (exception the output condition). Add a " +
                    "flow out of the task to resolve this issue.");
        }
        if (message.contains("preset size must be > 0")) {
            return new ValidationMessage("Error: " + extractLabel(message, 0, ' ') +
                    " has no incoming flows.",
                    "Every task and condition must be connected on some path between the " +
                    "input and output conditions of a net, and therefore must have at " +
                    "least one incoming flow (except the input condition). Add a flow " +
                    "into the task to resolve this issue.  ");
        }
        if (message.contains("exactly one default flow")) {
            return new ValidationMessage("Error: "+ extractLabel(message, 0, ' ') +
                    "has no default outgoing flow.",
                    "Any task with an OR or XOR split must have at least one outgoing " +
                    "flow marked as its default. " +
                     canSet("default flow in the Split Properties for the task") +
                    " NOTE: You may also see this error if the task has no split and " +
                    "no outgoing flow at all, in which case it can be resolved by " +
                    "adding an outgoing flow to the task.");
        }
        if (message.contains("an empty string value")) {
            return new ValidationMessage(
                    "Warning: A string data value may be uninitialised at runtime.", message);
        }
        if (message.contains("be uninitialised when the mapping")) {
            return new ValidationMessage(
                    "Error: An uninitialised data value will cause a failure at runtime.",
                    message);
        }
        if (message.startsWith("The decomposition")) {
            return new ValidationMessage("Warning: Decomposition:"
                    + extractLabel(message, '[', ']') + " is unused.",
                    "The decomposition is either not associated with any task " +
                    "or its task is currently not connected. You can remove this " +
                    "decomposition now using the 'File->Delete Orphaned Decompositions' " +
                    "menu, or have it automatically discarded when the file is next saved.");
        }
        return new ValidationMessage(message);     // default - no long form
    }


    private String canSet(String inner) {
        return "You can set a " + inner + " in the Properties pane.";
    }


    private String extractLabel(String message) {
        return extractLabel(message, ':', ']');
    }

    private String extractLabel(String message, char leftChar, char rightChar) {
        return extractLabel(message, message.indexOf(leftChar), rightChar);
    }

    private String extractLabel(String message, int left, char rightChar) {
        int right = message.indexOf(rightChar);
        if (left > -1 && right > -1) {
            return simplifyLabel(message.substring(left == 0 ? 0 : left + 1, right));
        }
        else return "";
    }

    private String simplifyLabel(String label) {
        if (label.startsWith("AtomicTask") || label.startsWith("CompositeTask")) {
            return label.replaceFirst("^\\w+Task", "Task");
        }
        if (label.startsWith("ExternalCondition")) {
            return label.replaceFirst("ExternalCondition", "Condition");
        }
        if (label.startsWith("InputCondition")) {
            return label.replaceFirst("^[\\w|:]+", "The Input Condition");
        }
        if (label.startsWith("OutputCondition")) {
            return label.replaceFirst("^[\\w|:]+", "The Output Condition");
        }
        if (label.startsWith("Condition")) {
            return simplifyImplicitCondition(label);
        }
        return label;
    }

    private String simplifyImplicitCondition(String label) {
        int left = label.indexOf("c{");
        int right = label.indexOf('}');
        if (left > -1 && right > -1) {
            return "The flow between " + identifyElements(label.substring(left+2, right));
        }
        return label;
    }


    private String identifyElements(String joinedNames) {
        String[] names = splitNames(joinedNames);
        return names.length > 1 ? names[0] + " & " + names[1] : joinedNames;
    }


    private String[] splitNames(String names) {
        Map<String, YExternalNetElement> map = getAllElementNames();
        for (int i=0; i<names.length(); i++) {
            if (names.charAt(i) == '_') {
                String name1 = names.substring(0,i);
                String name2 = names.substring(i+1);
                YExternalNetElement e1 = map.get(name1);
                YExternalNetElement e2 = map.get(name1);
                if (e1 != null && e2 != null) {
                    return new String[] {
                            getElementPrefix(e1) + name1, getElementPrefix(e2) + name2 };
                }
            }
        }
        return new String[] { names };
    }


    private Map<String, YExternalNetElement> getAllElementNames() {
        Map<String, YExternalNetElement> map = new HashMap<String, YExternalNetElement>();
        for (YNet net : SpecificationModel.getHandler().getControlFlowHandler().getNets()) {
            map.putAll(net.getNetElements());
        }
        return map;
    }


    private String getElementPrefix(YExternalNetElement e) {
        return (e instanceof YTask) ? "Task:" : "Condition";
    }
}
