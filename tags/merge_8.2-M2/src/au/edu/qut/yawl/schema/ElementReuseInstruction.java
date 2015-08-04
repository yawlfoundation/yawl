/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
 */


package au.edu.qut.yawl.schema;

/**
 /**
 * 
 * @author Lachlan Aldred
 * Date: 13/09/2004
 * Time: 12:40:16
 * 
 */
public class ElementReuseInstruction extends Instruction {
    /**
     * An instruction for creating a schema element.
     * @param elementName the name of the element to create.
     */
    public ElementReuseInstruction(String elementName) {
        if (null == elementName) {
            throw new IllegalArgumentException("you need to specify an element name.");
        }
        _elementName = elementName;
    }
}
