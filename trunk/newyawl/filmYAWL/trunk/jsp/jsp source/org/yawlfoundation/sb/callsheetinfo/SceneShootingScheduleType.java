//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.1.3-b01-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2007.06.02 at 01:01:41 PM EST 
//


package org.yawlfoundation.sb.callsheetinfo;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for sceneShootingScheduleType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="sceneShootingScheduleType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="singleSceneShootingSchedule" type="{http://www.yawlfoundation.org/sb/callSheetInfo}singleSceneShootingScheduleType" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "sceneShootingScheduleType", propOrder = {
    "singleSceneShootingSchedule"
})
public class SceneShootingScheduleType {

    @XmlElement(required = true)
    protected List<SingleSceneShootingScheduleType> singleSceneShootingSchedule;

    /**
     * Gets the value of the singleSceneShootingSchedule property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the singleSceneShootingSchedule property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSingleSceneShootingSchedule().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SingleSceneShootingScheduleType }
     * 
     * 
     */
    public List<SingleSceneShootingScheduleType> getSingleSceneShootingSchedule() {
        if (singleSceneShootingSchedule == null) {
            singleSceneShootingSchedule = new ArrayList<SingleSceneShootingScheduleType>();
        }
        return this.singleSceneShootingSchedule;
    }

}
