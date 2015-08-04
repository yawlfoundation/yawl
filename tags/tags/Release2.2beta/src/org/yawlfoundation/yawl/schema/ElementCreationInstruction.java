/*
 * Copyright (c) 2004-2011 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.schema;

/**
 /**
 * 
 * @author Lachlan Aldred
 * Date: 13/09/2004
 * Time: 12:35:11
 * 
 */
public class ElementCreationInstruction extends Instruction {
    private String _typeName;
    private boolean _isSchem4SchemaType;
    private boolean _mandatory;


    /**
     * Creates an element creation instruction.
     * @param elementName the name of the new element to create.
     * @param typeName the name of the type to retrieve from the primary schema.
     * @param isPrimitiveType
     */
    public ElementCreationInstruction(String elementName, String typeName, boolean isPrimitiveType, boolean mandatory) {
        if (null == elementName || null == typeName) {
            throw new IllegalArgumentException(
                    "You need to specifiy both an element and type name" +
                    " to create an element.");
        }
        this._elementName = elementName;
        this._typeName = typeName;
        this._isSchem4SchemaType = isPrimitiveType;
        this._mandatory = mandatory;
    }

    /**
     * Creates an element creation instruction.
     * @param elementName the name of the new element to create.
     * @param typeName the name of the type to retrieve from the primary schema.
     * @param isPrimitiveType
     */
    public ElementCreationInstruction(String elementName, String typeName, boolean isPrimitiveType) {
        this(elementName, typeName, isPrimitiveType, false);
    }


    /**
     * Gets the type name
     * @return type name
     */
    public String getTypeName() {
        return _typeName;
    }


    /**
     * Gets if should create element typed by schema 4 schema.
     */
    public boolean isSchem4SchemaType() {
        return _isSchem4SchemaType;
    }

    public boolean isMandatory()
    {
        return this._mandatory;
    }
}
