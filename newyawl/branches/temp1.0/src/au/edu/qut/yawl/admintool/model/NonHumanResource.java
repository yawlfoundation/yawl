/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.admintool.model;

import au.edu.qut.yawl.elements.YVerifiable;
import au.edu.qut.yawl.util.YVerificationMessage;

import java.util.List;
import java.util.ArrayList;

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