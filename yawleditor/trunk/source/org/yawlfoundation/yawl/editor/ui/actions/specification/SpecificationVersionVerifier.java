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

package org.yawlfoundation.yawl.editor.ui.actions.specification;

import org.yawlfoundation.yawl.editor.ui.swing.JFormattedSelectField;
import org.yawlfoundation.yawl.elements.YSpecVersion;

import javax.swing.*;
import javax.swing.text.Document;


public class SpecificationVersionVerifier extends InputVerifier {

    private YSpecVersion _origVersion;

    public SpecificationVersionVerifier(YSpecVersion origVersion) {
        super();
        _origVersion = origVersion;
    }

    public void setStartingVersion(YSpecVersion value) {
        _origVersion = value;
    }

    public String decStartingVersion() { return _origVersion.minorRollback(); }

    public String incStartingVersion() { return _origVersion.minorIncrement(); }


    public boolean verify(JComponent component) {
        assert component instanceof JFormattedSelectField;
        JFormattedSelectField field = (JFormattedSelectField) component;

        try {
            Document doc = field.getDocument();
            String docContent = doc.getText(0,doc.getLength());

            // version nbrs match doubles in format - an exception here means a bad format
            new Double(docContent);

            // no exception - ok, convert to a version number & compare with original
            return (new YSpecVersion(docContent).compareTo(_origVersion) >= 0);

        }
        catch (Exception e) {
            return false;
        }
    }


    public boolean shouldYieldFocus(JComponent component) {
        boolean isValid = verify(component);
        JFormattedSelectField field = (JFormattedSelectField) component;
        if (!isValid) {
            field.invalidEdit();
        }
        return isValid;
    }
}