/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.admintool.model;

import java.io.Serializable;

/**
 * 
 * @author Lachlan Aldred
 * Date: 27/09/2005
 * Time: 08:24:46
 * 
 */
public class ResourceCapability  implements Serializable {

    private String resourceID;
    private String capabilityDesc;

    public ResourceCapability() {
    }

    public String getCapabilityDesc() {
        return capabilityDesc;
    }

    public void setCapabilityDesc(String capabilityDesc) {
        this.capabilityDesc = capabilityDesc;
    }

    public String getResourceID() {
        return resourceID;
    }

    public void setResourceID(String resourceID) {
        this.resourceID = resourceID;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ResourceCapability)) return false;

        final ResourceCapability resourceCapability = (ResourceCapability) o;

        if (!capabilityDesc.equals(resourceCapability.capabilityDesc)) return false;
        if (!resourceID.equals(resourceCapability.resourceID)) return false;

        return true;
    }

    public int hashCode() {
        int result;
        result = resourceID.hashCode();
        result = 29 * result + capabilityDesc.hashCode();
        return result;
    }


}
