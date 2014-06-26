/*
 * Copyright (c) 2004-2014 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.editor.ui.properties;

import org.yawlfoundation.yawl.elements.YDecomposition;
import org.yawlfoundation.yawl.elements.YNet;
import org.yawlfoundation.yawl.logging.YLogPredicate;

/**
 * @author Michael Adams
 * @date 26/06/2014
 */
public class LogPredicateTransport {

    private YDecomposition _decomposition;

    public LogPredicateTransport(YDecomposition decomposition) {
        _decomposition = decomposition;
    }

    public YLogPredicate newLogPredicate() {
        _decomposition.setLogPredicate(new YLogPredicate());
        return _decomposition.getLogPredicate();
    }

    public LogPredicateTransport newInstance() {
        return new LogPredicateTransport(_decomposition);
    }

    public String getTitle() {
        String pre = _decomposition instanceof YNet ? "Net " : "";
        return pre + _decomposition.getID();
    }


    public void setStartPredicate(String predicate) {
        getOrCreate().setStartPredicate(predicate);
    }

    public String getStartPredicate() {
        YLogPredicate predicate = get();
        return predicate != null ? predicate.getStartPredicate() : null;
    }


    public void setCompletionPredicate(String predicate) {
        getOrCreate().setCompletionPredicate(predicate);
    }


    public String getCompletionPredicate() {
        YLogPredicate predicate = get();
        return predicate != null ? predicate.getCompletionPredicate() : null;
    }


    public String toString() {
        YLogPredicate logPredicate = _decomposition.getLogPredicate();
        int i = 0;
        if (logPredicate != null) {
            if (logPredicate.getStartPredicate() != null) i++;
            if (logPredicate.getCompletionPredicate() != null) i++;
        }
        return i + " defined";
    }


    private YLogPredicate getOrCreate() {
        YLogPredicate predicate = get();
        return predicate != null ? predicate : newLogPredicate();
    }

    private YLogPredicate get() { return _decomposition.getLogPredicate(); }
}
