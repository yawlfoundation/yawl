/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
 */


package org.yawlfoundation.yawl.admintool.model;

/**
 * 
 * @author Lachlan Aldred
 * Date: 22/09/2005
 * Time: 16:23:34

 */
public class NonHumanResource extends Resource {

    public NonHumanResource(String rsrcID) {
	super(rsrcID);
        setIsOfResSerPosType("Resource");

    }

    public NonHumanResource() {
        setIsOfResSerPosType("Resource");
    }

}