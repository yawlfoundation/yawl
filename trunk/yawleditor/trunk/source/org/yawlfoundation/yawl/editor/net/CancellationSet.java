/*
 * Created on 23/01/2004
 * YAWLEditor v1.0 
 *
 * @author Lindsay Bradford
 * 
 * 
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package org.yawlfoundation.yawl.editor.net;

import org.yawlfoundation.yawl.editor.elements.model.YAWLTask;
import org.yawlfoundation.yawl.editor.elements.model.YAWLCell;
import org.yawlfoundation.yawl.editor.elements.model.Condition;
import org.yawlfoundation.yawl.editor.elements.model.YAWLFlowRelation;

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
