//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.1.3-b01-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2007.05.22 at 10:45:47 AM EST 
//


package org.yawlfoundation.sb.camerainfo;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for camInfoSumType_2 complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="camInfoSumType_2">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="totalExposed" type="{http://www.w3.org/2001/XMLSchema}integer"/>
 *         &lt;element name="totalDeveloped" type="{http://www.w3.org/2001/XMLSchema}integer"/>
 *         &lt;element name="totalPrinted" type="{http://www.w3.org/2001/XMLSchema}integer"/>
 *         &lt;element name="heldOrNotSent" type="{http://www.w3.org/2001/XMLSchema}integer"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "camInfoSumType_2", propOrder = {
    "totalExposed",
    "totalDeveloped",
    "totalPrinted",
    "heldOrNotSent"
})
public class CamInfoSumType2 {

    @XmlElement(required = true)
    protected BigInteger totalExposed;
    @XmlElement(required = true)
    protected BigInteger totalDeveloped;
    @XmlElement(required = true)
    protected BigInteger totalPrinted;
    @XmlElement(required = true)
    protected BigInteger heldOrNotSent;

    /**
     * Gets the value of the totalExposed property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getTotalExposed() {
        return totalExposed;
    }

    /**
     * Sets the value of the totalExposed property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setTotalExposed(BigInteger value) {
        this.totalExposed = value;
    }

    /**
     * Gets the value of the totalDeveloped property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getTotalDeveloped() {
        return totalDeveloped;
    }

    /**
     * Sets the value of the totalDeveloped property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setTotalDeveloped(BigInteger value) {
        this.totalDeveloped = value;
    }

    /**
     * Gets the value of the totalPrinted property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getTotalPrinted() {
        return totalPrinted;
    }

    /**
     * Sets the value of the totalPrinted property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setTotalPrinted(BigInteger value) {
        this.totalPrinted = value;
    }

    /**
     * Gets the value of the heldOrNotSent property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getHeldOrNotSent() {
        return heldOrNotSent;
    }

    /**
     * Sets the value of the heldOrNotSent property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setHeldOrNotSent(BigInteger value) {
        this.heldOrNotSent = value;
    }

}
