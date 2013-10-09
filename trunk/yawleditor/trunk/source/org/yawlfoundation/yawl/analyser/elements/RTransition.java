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

package org.yawlfoundation.yawl.analyser.elements;

import org.yawlfoundation.yawl.analyser.elements.RElement;
import org.yawlfoundation.yawl.analyser.elements.RPlace;

import java.util.HashSet;
import java.util.Set;

public class RTransition extends RElement {

    private Set<RElement> _removeSet = new HashSet<RElement>();

    public RTransition(String id) {
        super(id);
    }

    public void setRemoveSet(Set<RElement> removeSet) {
        _removeSet.addAll(removeSet);

        //Add to populate _cancelledBySet.
        for (RElement element : removeSet) {
            element.addToCancelledBySet(this);
        }
    }

    public void setRemoveSet(RPlace p) {
        _removeSet.add(p);
        p.addToCancelledBySet(this);
    }

    public Set<RElement> getRemoveSet() {
        return _removeSet;
    }

    public void removeFromRemoveSet(RPlace p) {
        _removeSet.remove(p);
    }

    public boolean isCancelTransition() {
        return _removeSet.size() > 0;
    }

}
