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

package org.yawlfoundation.yawl.editor.ui.swing;

import javax.swing.JFormattedTextField;
import javax.swing.JFormattedTextField.AbstractFormatter;

public class JFormattedAlphaNumericField extends JFormattedSelectField {

    public JFormattedAlphaNumericField(int columns) {
        super();
        setFormatterFactory(new AlphaNumericFormatterFactory());
        setColumns(columns);
    }

    public void allowSpaces() {
        getAlphaNumericFormatter().allowSpaces();
    }

    public boolean spacesAllowed() {
        return getAlphaNumericFormatter().spacesAllowed();
    }

    public boolean xmlNameCharactersAllowed() {
        return getAlphaNumericFormatter().xmlNameCharactersAllowed();
    }

    /**
     * After invocation, this method will allow extra characters
     * that can go into valid XML names to be input by this
     * widget.  Note that if allowSpaces has also
     * been called, spaces will also be allowed, which is not
     * valid XML name convention.
     */
    public void allowXMLNames() {
        getAlphaNumericFormatter().allowXMLNameCharacters();
    }

    public AlphaNumericFormatter getAlphaNumericFormatter() {
        return (AlphaNumericFormatter) getFormatter();
    }
}

class AlphaNumericFormatterFactory extends JFormattedTextField.AbstractFormatterFactory {
    private final AlphaNumericFormatter FORMATTER = new AlphaNumericFormatter();

    public AbstractFormatter getFormatter(JFormattedTextField field) {
        assert field instanceof JFormattedAlphaNumericField;
        return FORMATTER;
    }
}
