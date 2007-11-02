/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
 */


package au.edu.qut.yawl.elements.e2wfoj;

import java.util.HashSet;
import java.util.Set;


public class RTransition extends RElement {
    private Set _removeSet = new HashSet();

    public RTransition(String id) {
        super(id);
    }

    public void setRemoveSet(Set removeSet) {
        _removeSet.addAll(removeSet);
    }

    public void setRemoveSet(RPlace p) {
        _removeSet.add(p);
    }

    public Set getRemoveSet() {
        if (_removeSet != null) {
            return _removeSet;
        }
        return null;
    }

    public boolean isCancelTransition() {
        return _removeSet.size() > 0;
    }

}

