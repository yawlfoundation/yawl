package org.yawlfoundation.yawl.editor.core.validation;

import org.yawlfoundation.yawl.elements.YSpecification;
import org.yawlfoundation.yawl.util.YVerificationHandler;
import org.yawlfoundation.yawl.util.YVerificationMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Michael Adams
 * @date 20/06/12
 */
public class Validator {

    private static final String NO_PROBLEMS_MESSAGE = "No problems reported.";


    public List<String> validate(YSpecification specification) {
        YVerificationHandler verificationHandler = new YVerificationHandler();
        specification.verify(verificationHandler);
        return createProblemList(verificationHandler.getMessages());
    }


    private List<String> createProblemList(List<YVerificationMessage> verificationList) {
        List<String> problemList = new ArrayList<String>();

        for (YVerificationMessage message : verificationList) {
            String messageString = message.getMessage();

            if (messageString.contains("composite task may not decompose to other than a net")) {
                continue;
            }
            if (messageString.contains("is not registered with engine.")) {
                // We have no running engine when validating, so this is not valid.
                continue;
            }

            // External db validation needs a running engine, so this is not valid.
            if (messageString.contains("could not be successfully parsed. External DB")) {
                continue;
            }

            messageString = messageString.replaceAll("postset size", "outgoing flow number");
            messageString = messageString.replaceAll("preset size", "incoming flow number");
            messageString = messageString.replaceAll(
                    "Check the empty tasks linking from i to o.",
                    "Should all atomic tasks in the net have no decomposition?");
            messageString = messageString.replaceAll("from i to o",
                    "between the input and output conditions");
            messageString = messageString.replaceAll("InputCondition", "Input Condition");
            messageString = messageString.replaceAll("OutputCondition", "Output Condition");
            messageString = messageString.replaceAll("ExternalCondition", "Condition");
            messageString = messageString.replaceAll("AtomicTask", "Atomic Task");
            messageString = messageString.replaceAll("CompositeTask", "Composite Task");
            messageString = messageString.replaceAll("The net \\(Net:", "The net (");
            messageString = messageString.replaceAll("composite task must contain a net",
                    "must unfold to some net");

            problemList.add(messageString);
        }
        if (problemList.size() == 0) {
            problemList.add(NO_PROBLEMS_MESSAGE);
        }
        return problemList;
    }

}
