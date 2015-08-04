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
 * Time: 12:31:15
 * 
 */
public abstract class Instruction implements Comparable {
    protected String _elementName;


    /**
     *
     * @return the name of the element to create.
     */
    public String getElementName() {
        return _elementName;
    }

    public String toString() {
        return _elementName;
    }

    public int compareTo(Object other) {
        return _elementName.compareToIgnoreCase(((Instruction) other).getElementName());
    }
}
