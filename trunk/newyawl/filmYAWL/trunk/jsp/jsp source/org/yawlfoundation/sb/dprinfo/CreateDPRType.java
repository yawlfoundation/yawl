//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.1.3-b01-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2007.06.02 at 12:04:03 PM EST 
//


package org.yawlfoundation.sb.dprinfo;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for Create_DPR_Type complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Create_DPR_Type">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="generalInfo" type="{http://www.yawlfoundation.org/sb/DPRinfo}generalInfoType"/>
 *         &lt;element name="producer" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="director" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="productionManager" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="directorOfPhotography" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="DPRinfo" type="{http://www.yawlfoundation.org/sb/DPRinfo}DPRinfoType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Create_DPR_Type", propOrder = {
    "generalInfo",
    "producer",
    "director",
    "productionManager",
    "directorOfPhotography",
    "dpRinfo"
})
public class CreateDPRType {

    @XmlElement(required = true)
    protected GeneralInfoType generalInfo;
    @XmlElement(required = true)
    protected String producer;
    @XmlElement(required = true)
    protected String director;
    @XmlElement(required = true)
    protected String productionManager;
    @XmlElement(required = true)
    protected String directorOfPhotography;
    @XmlElement(name = "DPRinfo", required = true)
    protected DPRinfoType dpRinfo;

    /**
     * Gets the value of the generalInfo property.
     * 
     * @return
     *     possible object is
     *     {@link GeneralInfoType }
     *     
     */
    public GeneralInfoType getGeneralInfo() {
        return generalInfo;
    }

    /**
     * Sets the value of the generalInfo property.
     * 
     * @param value
     *     allowed object is
     *     {@link GeneralInfoType }
     *     
     */
    public void setGeneralInfo(GeneralInfoType value) {
        this.generalInfo = value;
    }

    /**
     * Gets the value of the producer property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProducer() {
        return producer;
    }

    /**
     * Sets the value of the producer property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProducer(String value) {
        this.producer = value;
    }

    /**
     * Gets the value of the director property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDirector() {
        return director;
    }

    /**
     * Sets the value of the director property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDirector(String value) {
        this.director = value;
    }

    /**
     * Gets the value of the productionManager property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProductionManager() {
        return productionManager;
    }

    /**
     * Sets the value of the productionManager property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProductionManager(String value) {
        this.productionManager = value;
    }

    /**
     * Gets the value of the directorOfPhotography property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDirectorOfPhotography() {
        return directorOfPhotography;
    }

    /**
     * Sets the value of the directorOfPhotography property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDirectorOfPhotography(String value) {
        this.directorOfPhotography = value;
    }

    /**
     * Gets the value of the dpRinfo property.
     * 
     * @return
     *     possible object is
     *     {@link DPRinfoType }
     *     
     */
    public DPRinfoType getDPRinfo() {
        return dpRinfo;
    }

    /**
     * Sets the value of the dpRinfo property.
     * 
     * @param value
     *     allowed object is
     *     {@link DPRinfoType }
     *     
     */
    public void setDPRinfo(DPRinfoType value) {
        this.dpRinfo = value;
    }

}
