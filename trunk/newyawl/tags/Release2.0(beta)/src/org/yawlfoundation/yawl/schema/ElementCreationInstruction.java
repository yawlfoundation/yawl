/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
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
