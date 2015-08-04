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
 * Time: 13:45:29
 * 
 */
public final class YOutputCondition extends YCondition {


    public YOutputCondition(String id, String label, YNet container) {
        super(id, label, container);

    }


    public YOutputCondition(String id, YNet container) {
        super(id, container);
    }


    public List<YVerificationMessage> verify() {
        List<YVerificationMessage> messages = new Vector<YVerificationMessage>();

        if (getPostsetElements().size() != 0) {
            messages.add(new YVerificationMessage(this, this
                    + " postset must be empty: " + getPostsetElements(), YVerificationMessage.ERROR_STATUS));
        }
        messages.addAll(verifyPresetFlows());
        return messages;
    }


    public Object clone() throws CloneNotSupportedException {
        YNet copyContainer = _net.getCloneContainer();
        if (copyContainer.getNetElements().containsKey(this.getID())) {
            return copyContainer.getNetElement(this.getID());
        }
        YOutputCondition copy = (YOutputCondition) super.clone();
        copy._net.setOutputCondition(copy);
        return copy;
    }
}
