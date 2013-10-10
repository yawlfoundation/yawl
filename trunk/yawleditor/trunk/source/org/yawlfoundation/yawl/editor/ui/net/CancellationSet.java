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

import org.yawlfoundation.yawl.editor.core.controlflow.YCompoundFlow;
import org.yawlfoundation.yawl.editor.ui.elements.model.*;
import org.yawlfoundation.yawl.elements.YCondition;
import org.yawlfoundation.yawl.elements.YExternalNetElement;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CancellationSet implements Serializable, Cloneable {

    private YAWLTask _ownerTask;
    private Set<YAWLCell> _members;

    // only called internally
    private CancellationSet() {
        _members = new HashSet<YAWLCell>();
    }

    public CancellationSet(YAWLTask task) {
        this();
        _ownerTask = task;
    }

    public YAWLTask getOwnerTask() {
        return _ownerTask;
    }

    public void setOwnerTask(YAWLTask task) {
        _ownerTask = task;
    }


    public Set<YAWLCell> getMembers() {
        return _members;
    }

    public void setMembers(Set<YAWLCell> members) {
        _members = members;
    }

    public boolean add(YAWLCell member) {
        return _members.add(member);
    }

    public boolean remove(YAWLCell cell) {
        return _members.remove(cell);
    }

    public void save() {
        List<YExternalNetElement> removeSet = new ArrayList<YExternalNetElement>();
        for (YAWLCell member : _members) {
            if (member instanceof YAWLFlowRelation) {
                YCompoundFlow yFlow = ((YAWLFlowRelation) member).getYFlow();
                YCondition condition = yFlow.getImplicitCondition();
                if (condition != null) {
                    removeSet.add(condition);
                }
            }
            else if (member instanceof VertexContainer) {
                removeSet.add(((VertexContainer) member).getVertex().getYAWLElement());
            }
            else {
                removeSet.add(((YAWLVertex) member).getYAWLElement());
            }
        }
        _ownerTask.getTask().addRemovesTokensFrom(removeSet);
    }


    public boolean contains(YAWLCell cell) {
        return _members.contains(cell);
    }

    public int size() {
        return _members.size();
    }

    public boolean hasMembers() {
        return ! _members.isEmpty();
    }

    public Object clone() {
        // Cancellation set members will NOT be copied.
        return new CancellationSet();
    }
}
