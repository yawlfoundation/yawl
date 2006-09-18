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
import javax.persistence.Column;

import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.Basic;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
/**
 * 
 * @author Lachlan Aldred
 * Date: 22/09/2005
 * Time: 15:21:12
 */
@Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(
    name="resource_type",
    discriminatorType=DiscriminatorType.STRING
)
public abstract class Resource implements YVerifiable {
    private String rsrcID;
    private String description;
    private String isOfResSerPosType;




    
    public Resource(String rsrcID) {
        this.rsrcID = rsrcID;

    }

    protected Resource() {
    }
    
    @Id
    @Basic
    public String getRsrcID() {
        return rsrcID;
    }

    private void setRsrcID(String rsrcID) {
        this.rsrcID = rsrcID;
    }

    @Basic
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<YVerificationMessage> verify() {
        List<YVerificationMessage> results =
                new ArrayList<YVerificationMessage>();
        if(rsrcID == null || rsrcID.length() == 0){
            results.add(new YVerificationMessage(
                    this,
                    "ResourceID cannot be null or empty",
                    YVerificationMessage.ERROR_STATUS));
        }
        return results;
    }

    @Basic
    public String getIsOfResSerPosType(){
        return isOfResSerPosType;
    }

    public void setIsOfResSerPosType(String isOfResSerPosType) {
        this.isOfResSerPosType = isOfResSerPosType;
    }

}
