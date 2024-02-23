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

package org.yawlfoundation.yawl.util;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Michael Adams
 * @date 6/05/12
 */
public class YVerificationHandler {

    public enum MessageType { error, warning }

    private List<YVerificationMessage> _errors;
    private List<YVerificationMessage> _warnings;

    public YVerificationHandler() {
        _errors = new ArrayList<YVerificationMessage>();
        _warnings = new ArrayList<YVerificationMessage>();
    }


    public void error(Object object, String message) {
        _errors.add(new YVerificationMessage(object, message));
    }

    public void warn(Object object, String message) {
        _warnings.add(new YVerificationMessage(object, message));
    }

    public void add(Object object, String message, MessageType mType) {
        switch (mType) {
            case error : error(object, message); break;
            case warning: warn(object, message); break;
        }
    }

    public void reset() {
        _errors.clear();
        _warnings.clear();
    }

    public boolean hasErrors() { return ! _errors.isEmpty(); }

    public boolean hasWarnings() { return ! _warnings.isEmpty(); }

    public boolean hasMessages() { return hasErrors() || hasWarnings(); }

    public List<YVerificationMessage> getErrors() { return _errors; }

    public List<YVerificationMessage> getWarnings() { return _warnings; }


    public List<YVerificationMessage> getMessages() {
        List<YVerificationMessage> messages = new ArrayList<YVerificationMessage>();
        messages.addAll(_errors);
        messages.addAll(_warnings);
        return messages;
    }

    public int getMessageCount() {
        return _errors.size() + _warnings.size();
    }

    public String getMessagesXML() {
        XNode parentNode = new XNode("verificationMessages");
        for (YVerificationMessage message : _errors) {
            populateNode(parentNode.addChild("error"), message);
        }
        for (YVerificationMessage message : _warnings) {
            populateNode(parentNode.addChild("warning"), message);
        }
        return parentNode.toString();
    }

    private void populateNode(XNode msgNode, YVerificationMessage message) {
        if (message.getSource() != null) msgNode.addChild("source", message.getSource());  // todo check tostrings
        msgNode.addChild("message", message.getMessage());
    }

}
