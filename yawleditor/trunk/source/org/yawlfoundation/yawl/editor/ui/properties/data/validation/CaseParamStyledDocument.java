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

import org.yawlfoundation.yawl.editor.core.data.DataSchemaValidator;
import org.yawlfoundation.yawl.editor.ui.data.Validity;
import org.yawlfoundation.yawl.editor.ui.data.document.AbstractXMLStyledDocument;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Michael Adams
 * @date 16/08/13
 */
public class CaseParamStyledDocument extends AbstractXMLStyledDocument {

    private List<String> _errorList;

    private static final DataSchemaValidator DATA_HANDLER = new DataSchemaValidator();

    public CaseParamStyledDocument(CaseParamEditor editor) {
        super(editor);
        DATA_HANDLER.setDataTypeSchema(editor.getDataType());
        _errorList = new ArrayList<String>();
    }

    public void checkValidity() {
        _errorList = DATA_HANDLER.validate(getEditor().getText());
        setContentValidity(_errorList.isEmpty() ? Validity.VALID : Validity.INVALID);
    }

    public List<String> getProblemList() { return _errorList; }

    public void setPreAndPostEditorText(String pre, String post) { }

}
