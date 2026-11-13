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

package org.yawlfoundation.yawl.worklet.rdr;


import org.yawlfoundation.yawl.worklet.exception.ExletAction;
import org.yawlfoundation.yawl.worklet.exception.ExletTarget;

public class RdrPrimitive implements Comparable<RdrPrimitive> {

    private int index;
    private ExletAction _action;
    private ExletTarget _target;
    private String worklet;

    private int listIndex;         // for hibernate only

    protected RdrPrimitive() { }  // for hibernate


    public RdrPrimitive(int i, String a, String t) {
        setIndex(i);
        setAction(a);
        setTarget(t);
    }

    public RdrPrimitive(int i, ExletAction a, ExletTarget t) {
        index = i; _action = a; _target = t;
    }


    public int getIndex() { return index; }

    public String getAction() { return _action.toString(); }

    public String getTarget() {
        return worklet != null ? worklet : _target.toString();
    }

    public String getWorkletTarget() { return worklet; }


    public ExletAction getExletAction() { return _action; }

    public ExletTarget getExletTarget() { return _target; }


    public void setIndex(int index) { this.index = index; }

    public void setAction(String _action) {
        this._action = ExletAction.fromString(_action);
    }

    public void setTarget(String _target) {
        this._target = ExletTarget.fromString(_target);
        if (this._target == ExletTarget.Invalid && this._action != null &&
                this._action.isWorkletAction()) {
            worklet = _target;
        }
    }

    public void setExletAction(ExletAction action) { this._action = action; }

    public void setExletTarget(ExletTarget target) { this._target = target; }


    public boolean isValid() {
        return getExletAction() != ExletAction.Invalid &&
                (getExletTarget() != ExletTarget.Invalid || getWorkletTarget() != null);
    }


    public int compareTo(RdrPrimitive other) {return index - other.index; }


    public boolean equals(Object o) {
        if (o instanceof RdrPrimitive) {
            RdrPrimitive other = (RdrPrimitive) o;
            return getIndex() == other.getIndex() &&
                    getAction().equals(other.getAction()) &&
                    getTarget().equals((other.getTarget()));
        }
        return false;
    }


    public int hashCode() {
        return 31 * getIndex() * (getAction().hashCode() + getTarget().hashCode());
    }


    public String toString() {
        return getAction() + " " + getTarget();
    }


    public int getListIndex() { return index; }
    public void setListIndex(int listIndex) { this.index = listIndex; }

}
