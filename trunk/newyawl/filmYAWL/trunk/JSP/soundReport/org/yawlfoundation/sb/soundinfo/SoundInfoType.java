//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.1.3-b01-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2007.05.19 at 04:56:47 PM EST 
//


package org.yawlfoundation.sb.soundinfo;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for soundInfoType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="soundInfoType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="soundRoll" type="{http://www.w3.org/2001/XMLSchema}integer"/>
 *         &lt;element name="techInfo" type="{http://www.yawlfoundation.org/sb/soundInfo}techInfoType"/>
 *         &lt;element name="sceneInfo" type="{http://www.yawlfoundation.org/sb/soundInfo}sceneInfoType" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "soundInfoType", propOrder = {
    "soundRoll",
    "techInfo",
    "sceneInfo"
})
public class SoundInfoType {

    @XmlElement(required = true)
    protected BigInteger soundRoll;
    @XmlElement(required = true)
    protected TechInfoType techInfo;
    @XmlElement(required = true)
    protected List<SceneInfoType> sceneInfo;

    /**
     * Gets the value of the soundRoll property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getSoundRoll() {
        return soundRoll;
    }

    /**
     * Sets the value of the soundRoll property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setSoundRoll(BigInteger value) {
        this.soundRoll = value;
    }

    /**
     * Gets the value of the techInfo property.
     * 
     * @return
     *     possible object is
     *     {@link TechInfoType }
     *     
     */
    public TechInfoType getTechInfo() {
        return techInfo;
    }

    /**
     * Sets the value of the techInfo property.
     * 
     * @param value
     *     allowed object is
     *     {@link TechInfoType }
     *     
     */
    public void setTechInfo(TechInfoType value) {
        this.techInfo = value;
    }

    /**
     * Gets the value of the sceneInfo property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the sceneInfo property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSceneInfo().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SceneInfoType }
     * 
     * 
     */
    public List<SceneInfoType> getSceneInfo() {
        if (sceneInfo == null) {
            sceneInfo = new ArrayList<SceneInfoType>();
        }
        return this.sceneInfo;
    }

}
