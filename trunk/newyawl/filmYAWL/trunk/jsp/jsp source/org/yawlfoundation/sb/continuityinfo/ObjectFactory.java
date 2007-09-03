//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.1.3-b01-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2007.06.02 at 12:52:10 PM EST 
//


package org.yawlfoundation.sb.continuityinfo;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.yawlfoundation.sb.continuityinfo package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _FillOutContinuityReport_QNAME = new QName("http://www.yawlfoundation.org/sb/continuityInfo", "Fill_Out_Continuity_Report");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.yawlfoundation.sb.continuityinfo
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link TakeInfoType }
     * 
     */
    public TakeInfoType createTakeInfoType() {
        return new TakeInfoType();
    }

    /**
     * Create an instance of {@link ContinuityInfoType }
     * 
     */
    public ContinuityInfoType createContinuityInfoType() {
        return new ContinuityInfoType();
    }

    /**
     * Create an instance of {@link FillOutContinuityReportType }
     * 
     */
    public FillOutContinuityReportType createFillOutContinuityReportType() {
        return new FillOutContinuityReportType();
    }

    /**
     * Create an instance of {@link SlateInfoType }
     * 
     */
    public SlateInfoType createSlateInfoType() {
        return new SlateInfoType();
    }

    /**
     * Create an instance of {@link SceneInfoType }
     * 
     */
    public SceneInfoType createSceneInfoType() {
        return new SceneInfoType();
    }

    /**
     * Create an instance of {@link GeneralInfoType }
     * 
     */
    public GeneralInfoType createGeneralInfoType() {
        return new GeneralInfoType();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link FillOutContinuityReportType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.yawlfoundation.org/sb/continuityInfo", name = "Fill_Out_Continuity_Report")
    public JAXBElement<FillOutContinuityReportType> createFillOutContinuityReport(FillOutContinuityReportType value) {
        return new JAXBElement<FillOutContinuityReportType>(_FillOutContinuityReport_QNAME, FillOutContinuityReportType.class, null, value);
    }

}
