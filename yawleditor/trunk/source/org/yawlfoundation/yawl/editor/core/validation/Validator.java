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

    public static final int ERROR_MESSAGES = 1;
    public static final int WARNING_MESSAGES = 2;
    public static final int ALL_MESSAGES = 3;


    public List<String> validate(YSpecification specification) {
        return validate(specification, ALL_MESSAGES);
    }


    public List<String> validate(YSpecification specification, int messageType) {
        YVerificationHandler verificationHandler = new YVerificationHandler();
        specification.verify(verificationHandler);
        return createProblemList(verificationHandler, messageType);
    }


    private List<String> createProblemList(YVerificationHandler verificationHandler,
                                           int messageType) {
        switch (messageType) {
            case ERROR_MESSAGES :
                return createProblemList(verificationHandler.getErrors());
            case WARNING_MESSAGES :
                return createProblemList(verificationHandler.getWarnings());
            default : // ALL_MESSAGES
                return createProblemList(verificationHandler.getMessages());
        }
    }


    private List<String> createProblemList(List<YVerificationMessage> verificationList) {
        List<String> problemList = new ArrayList<String>();

        for (YVerificationMessage message : verificationList) {
            String messageString = message.getMessage();

            if (messageString.contains("composite task may not decompose to other than a net")) {
                continue;
            }

            // We have no running engine when validating, so this is not valid.
            if (messageString.contains("is not registered with engine.")) {
                continue;
            }

            // External db validation needs a running engine, so this is not valid.
            if (messageString.contains("could not be successfully parsed. External DB")) {
                continue;
            }

            messageString = messageString.replaceAll(
                    "Check the empty tasks linking from i to o.",
                    "Should all atomic tasks in the net have no decomposition?");
            messageString = messageString.replaceAll("from i to o",
                    "between the input and output conditions");
//            messageString = messageString.replaceAll("InputCondition", "Input Condition");
//            messageString = messageString.replaceAll("OutputCondition", "Output Condition");
//            messageString = messageString.replaceAll("ExternalCondition", "Condition");
//            messageString = messageString.replaceAll("AtomicTask", "Atomic Task");
//            messageString = messageString.replaceAll("CompositeTask", "Composite Task");
            messageString = messageString.replaceAll("The net \\(Net:", "The net (");
            messageString = messageString.replaceAll("composite task must contain a net",
                    "must unfold to some net");

            problemList.add(messageString);
        }
        return problemList;
    }

}
