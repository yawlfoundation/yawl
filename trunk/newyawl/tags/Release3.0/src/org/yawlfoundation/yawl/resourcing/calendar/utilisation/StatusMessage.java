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

package org.yawlfoundation.yawl.resourcing.calendar.utilisation;

import org.yawlfoundation.yawl.util.XNode;

/**
 * @author Michael Adams
 * @date 6/10/2010
 */
public class StatusMessage {
    private String warning;
    private String error;

    public StatusMessage() { }

    public StatusMessage(XNode node) {
        fromXNode(node);
    }

    public String getWarning() { return warning; }

    public void setWarning(String s) { warning = s; }

    public boolean hasWarning() { return (warning != null); }


    public String getError() { return error; }

    public void setError(String s) { error = s; }

    public boolean hasError() { return (error != null); }


    public boolean hasMessage() { return hasError() || hasWarning(); }


    public void addAttributes(XNode node) {
        if (error != null) node.addAttribute("error", error);
        if (warning != null) node.addAttribute("warning", warning);
    }

    public void fromXNode(XNode node) {
        if (node.hasAttribute("warning")) setWarning(node.getAttributeValue("warning"));
        if (node.hasAttribute("error")) setError(node.getAttributeValue("error"));
    }

    public XNode toXNode(String tag) {
        XNode node = new XNode(tag);
        addAttributes(node);
        return node;
    }

}
