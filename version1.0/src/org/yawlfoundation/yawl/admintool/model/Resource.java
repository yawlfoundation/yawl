/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
 */


package org.yawlfoundation.yawl.admintool.model;

import org.yawlfoundation.yawl.elements.YVerifiable;
import org.yawlfoundation.yawl.util.YVerificationMessage;

import java.util.List;
import java.util.ArrayList;

/**
 * 
 * @author Lachlan Aldred
 * Date: 22/09/2005
 * Time: 15:21:12
 */
public abstract class Resource implements YVerifiable {
    private String rsrcID;
    private String description;
    private String isOfResSerPosType;

    public Resource(String rsrcID) {
        this.rsrcID = rsrcID;

    }

    protected Resource() {
    }

    public String getRsrcID() {
        return rsrcID;
    }

    private void setRsrcID(String rsrcID) {
        this.rsrcID = rsrcID;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List verify() {
        List results = new ArrayList();
        if(rsrcID == null || rsrcID.length() == 0){
            results.add(new YVerificationMessage(
                    this,
                    "ResourceID cannot be null or empty",
                    YVerificationMessage.ERROR_STATUS));
        }
        return results;
    }

    public String getIsOfResSerPosType(){
        return isOfResSerPosType;
    }

    public void setIsOfResSerPosType(String isOfResSerPosType) {
        this.isOfResSerPosType = isOfResSerPosType;
    }

}
