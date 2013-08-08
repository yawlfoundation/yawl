/*
 * Created on 16/05/2004
 * YAWLEditor v1.03 
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
 */

package org.yawlfoundation.yawl.editor.ui.swing.data;

import org.yawlfoundation.yawl.editor.core.data.YInternalType;
import org.yawlfoundation.yawl.schema.XSDType;

public class XMLSchemaInstanceEditor extends ValidityEditorPane {

    private String variableName;
    private String variableType;

    private static final String SCHEMA_HEADER =
            "<schema xmlns=\"http://www.w3.org/2001/XMLSchema\">";
    private static final String SCHEMA_CLOSER = "</schema>";


    public XMLSchemaInstanceEditor() {
        super();
        setDocument(new XMLInstanceStyledDocument(this));
        subscribeForValidityEvents();
    }

    public String getVariableType() {
        return this.variableType;
    }

    public String getVariableName() {
        return this.variableName;
    }

    public void setVariableType(String variableType) {
        this.variableType = variableType;
        validate();
    }

    public void setVariableName(String variableName) {
        this.variableName = variableName;
        validate();
    }

    public void validate() {
        if (this.variableName == null || this.variableType == null) {
            return;
        }
        super.validate();
    }

    public String getTypeDefinition() {
        return makeSchema("<element name=\"" +
                this.variableName +
                "\" type=\"" +
                this.variableType +
                "\"/>");
    }

    public String getSchemaInstance() {
        if (XSDType.getInstance().isBuiltInType(variableType) || isYInternalType(variableType)) {
            return "<" + this.variableName + ">\n" +
                    this.getText().trim() +
                    "\n</" + this.variableName + ">";
        }

        return this.getText();
    }


    private boolean isYInternalType(String type) {
        for (YInternalType internalType : YInternalType.values()) {
            if (internalType.name().equals(type)) return true;
        }
        return false;
    }

    private String makeSchema(String innards) {
        return SCHEMA_HEADER + innards + SCHEMA_CLOSER;
    }

}

