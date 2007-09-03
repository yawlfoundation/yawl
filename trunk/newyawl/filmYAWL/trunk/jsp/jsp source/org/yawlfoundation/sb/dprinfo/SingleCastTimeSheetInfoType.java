//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.1.3-b01-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2007.06.02 at 12:04:03 PM EST 
//


package org.yawlfoundation.sb.dprinfo;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for singleCastTimeSheetInfoType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="singleCastTimeSheetInfoType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="cast" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="character" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="pickUp" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="MU_WD_Call" type="{http://www.w3.org/2001/XMLSchema}time"/>
 *         &lt;element name="travelTime" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="wrap" type="{http://www.w3.org/2001/XMLSchema}time"/>
 *         &lt;element name="lunchBreak" type="{http://www.w3.org/2001/XMLSchema}time"/>
 *         &lt;element name="totalHours" type="{http://www.w3.org/2001/XMLSchema}time"/>
 *         &lt;element name="extrasNOs" type="{http://www.w3.org/2001/XMLSchema}integer"/>
 *         &lt;element name="HRs" type="{http://www.w3.org/2001/XMLSchema}time"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "singleCastTimeSheetInfoType", propOrder = {
    "cast",
    "character",
    "pickUp",
    "muwdCall",
    "travelTime",
    "wrap",
    "lunchBreak",
    "totalHours",
    "extrasNOs",
    "hRs"
})
public class SingleCastTimeSheetInfoType {

    @XmlElement(required = true)
    protected String cast;
    @XmlElement(required = true)
    protected String character;
    @XmlElement(required = true)
    protected String pickUp;
    @XmlElement(name = "MU_WD_Call", required = true)
    @XmlSchemaType(name = "time")
    protected XMLGregorianCalendar muwdCall;
    @XmlElement(required = true)
    protected String travelTime;
    @XmlElement(required = true)
    @XmlSchemaType(name = "time")
    protected XMLGregorianCalendar wrap;
    @XmlElement(required = true)
    @XmlSchemaType(name = "time")
    protected XMLGregorianCalendar lunchBreak;
    @XmlElement(required = true)
    @XmlSchemaType(name = "time")
    protected XMLGregorianCalendar totalHours;
    @XmlElement(required = true)
    protected BigInteger extrasNOs;
    @XmlElement(name = "HRs", required = true)
    @XmlSchemaType(name = "time")
    protected XMLGregorianCalendar hRs;

    /**
     * Gets the value of the cast property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCast() {
        return cast;
    }

    /**
     * Sets the value of the cast property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCast(String value) {
        this.cast = value;
    }

    /**
     * Gets the value of the character property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCharacter() {
        return character;
    }

    /**
     * Sets the value of the character property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCharacter(String value) {
        this.character = value;
    }

    /**
     * Gets the value of the pickUp property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPickUp() {
        return pickUp;
    }

    /**
     * Sets the value of the pickUp property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPickUp(String value) {
        this.pickUp = value;
    }

    /**
     * Gets the value of the muwdCall property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getMUWDCall() {
        return muwdCall;
    }

    /**
     * Sets the value of the muwdCall property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setMUWDCall(XMLGregorianCalendar value) {
        this.muwdCall = value;
    }

    /**
     * Gets the value of the travelTime property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTravelTime() {
        return travelTime;
    }

    /**
     * Sets the value of the travelTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTravelTime(String value) {
        this.travelTime = value;
    }

    /**
     * Gets the value of the wrap property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getWrap() {
        return wrap;
    }

    /**
     * Sets the value of the wrap property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setWrap(XMLGregorianCalendar value) {
        this.wrap = value;
    }

    /**
     * Gets the value of the lunchBreak property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getLunchBreak() {
        return lunchBreak;
    }

    /**
     * Sets the value of the lunchBreak property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setLunchBreak(XMLGregorianCalendar value) {
        this.lunchBreak = value;
    }

    /**
     * Gets the value of the totalHours property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getTotalHours() {
        return totalHours;
    }

    /**
     * Sets the value of the totalHours property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setTotalHours(XMLGregorianCalendar value) {
        this.totalHours = value;
    }

    /**
     * Gets the value of the extrasNOs property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getExtrasNOs() {
        return extrasNOs;
    }

    /**
     * Sets the value of the extrasNOs property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setExtrasNOs(BigInteger value) {
        this.extrasNOs = value;
    }

    /**
     * Gets the value of the hRs property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getHRs() {
        return hRs;
    }

    /**
     * Sets the value of the hRs property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setHRs(XMLGregorianCalendar value) {
        this.hRs = value;
    }

}
