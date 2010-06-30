/*
 * Copyright (c) 2004-2010 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.elements;

import org.yawlfoundation.yawl.util.YVerificationMessage;

import java.util.List;
import java.util.Vector;

/**
 * 
 * @author Lachlan Aldred
 * Date: 22/04/2003
 * Time: 13:43:54
 * 
 */
public final class YInputCondition extends YCondition {

    /**
     * Constructor.
     * @param id of the NetElement.
     * @param label of the condition.
     */
    public YInputCondition(String id, String label, YNet container) {
        super(id, label, container);
    }

    public YInputCondition(String id, YNet container) {
        super(id, container);
    }


    /**
     * This is one of those few cases where the sub-class has tighter constraints than the
     * parent class on one of the supertype members (preset).  ie. an InputCondition must always
     * have an empty preset.
     * @return a List of error messages.
     */
    public List<YVerificationMessage> verify() {
        List<YVerificationMessage> messages = new Vector<YVerificationMessage>();
        if (getPresetElements().size() != 0) {
            messages.add(new YVerificationMessage(this,
                         this + " preset must be empty: " + getPresetElements(),
                         YVerificationMessage.ERROR_STATUS));
        }
        messages.addAll(verifyPostsetFlows());
        return messages;
    }


    public Object clone() throws CloneNotSupportedException {
        YNet copyContainer = _net.getCloneContainer();
        if (copyContainer.getNetElements().containsKey(this.getID())) {
            return copyContainer.getNetElement(this.getID());
        }
        YInputCondition copy = (YInputCondition) super.clone();
        copy._net.setInputCondition(copy);
        return copy;
    }
}
