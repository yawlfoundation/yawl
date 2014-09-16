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

package org.yawlfoundation.yawl.editor.ui.specification.validation;

/**
 * @author Michael Adams
 * @date 16/10/13
 */
public class ValidationMessage implements Comparable<ValidationMessage> {

    private String _shortForm;
    private String _longForm;

    public ValidationMessage(String shortForm, String longForm) {
        setShortForm(shortForm);
        setLongForm(longForm);
    }

    public ValidationMessage(String shortForm) {
        setShortForm(shortForm);
    }


    public String getShortForm() {
        return _shortForm;
    }

    public void setShortForm(String shortForm) {
        _shortForm = shortForm;
    }

    public String getLongForm() {
        return _longForm != null ? _longForm : _shortForm;
    }

    public void setLongForm(String longForm) {
        _longForm = longForm;
    }


    public String getTableRowForm() {
        return _longForm != null ? _shortForm + " <more...>" : _shortForm;
    }


    public int compareTo(ValidationMessage other) {
        return other != null ? _shortForm.compareTo(other.getShortForm()) : 1;
    }

}
