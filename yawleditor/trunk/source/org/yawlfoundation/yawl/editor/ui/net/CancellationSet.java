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

package org.yawlfoundation.yawl.editor.ui.net;

import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLTask;
import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLCell;
import org.yawlfoundation.yawl.editor.ui.elements.model.Condition;
import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLFlowRelation;

import java.io.Serializable;
import java.util.HashSet;

public class CancellationSet implements Serializable, Cloneable {

    private static final long serialVersionUID = 1L;
    private YAWLTask triggeringTask;
    private HashSet<YAWLCell> setMembers;

    public CancellationSet() {
        setMembers = new HashSet<YAWLCell>();
    }

    public CancellationSet(YAWLTask task) {
        this();
        triggeringTask = task;
    }

    public YAWLTask getTriggeringTask() {
        return triggeringTask;
    }

    public void setTriggeringTask(YAWLTask task) {
        triggeringTask = task;
    }

    public HashSet<YAWLCell> getSetMembers() {
        return setMembers;
    }

    public void setSetMembers(HashSet<YAWLCell> set) {
        setMembers = set;
    }

    public boolean addMember(YAWLCell newSetMember) {
        if (newSetMember instanceof YAWLTask || newSetMember instanceof Condition ||
                newSetMember instanceof YAWLFlowRelation) {
            return setMembers.add(newSetMember);
        }
        return false;
    }

    public boolean removeMember(YAWLCell oldSetMember) {
        return setMembers.remove(oldSetMember);
    }

    public boolean contains(YAWLCell cell) {
        return setMembers.contains(cell);
    }

    public int size() {
        return setMembers.size();
    }

    public boolean hasMembers() {
        return size() > 0;
    }

    public Object clone() {
        // Cancellation set members will NOT be copied.
        return new CancellationSet();
    }
}
