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

package org.yawlfoundation.yawl.editor.ui.properties.data.validation;

import org.yawlfoundation.yawl.editor.core.data.YDataHandler;
import org.yawlfoundation.yawl.editor.ui.data.editorpane.AbstractXMLStyledDocument;
import org.yawlfoundation.yawl.editor.ui.data.Validity;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationModel;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Michael Adams
 * @date 16/08/13
 */
public class InstanceStyledDocument extends AbstractXMLStyledDocument {

    private List<String> _errorList;
    private String _dataType;

    private static final YDataHandler DATA_HANDLER =
            SpecificationModel.getHandler().getDataHandler();

    public InstanceStyledDocument(InstanceEditor editor) {
        super(editor);
        _dataType = editor.getDataType();
        _errorList = new ArrayList<String>();
    }

    public void checkValidity() {
        _errorList = DATA_HANDLER.validate(_dataType, getEditor().getText());
        setContentValidity(_errorList.isEmpty() ? Validity.VALID : Validity.INVALID);
    }

    public List<String> getProblemList() { return _errorList; }

    public void setPreAndPostEditorText(String pre, String post) { }

}
