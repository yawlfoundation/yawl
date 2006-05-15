/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.elements.data;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.hibernate.annotations.CollectionOfElements;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import au.edu.qut.yawl.elements.YDecomposition;
import au.edu.qut.yawl.persistence.PolymorphicPersistableObject;
import au.edu.qut.yawl.util.YVerificationMessage;

/**
 * 
 * @author Lachlan Aldred
 * Date: 24/09/2003
 * Time: 15:49:36
 * 
 * 
 */
@Entity
@DiscriminatorValue("output")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OutputParameterFactsType", namespace="http://www.citi.qut.edu.au/yawl", 
		propOrder = {"_mandatory"}
)
public class YOutputParameter extends YParameter implements Comparable, PolymorphicPersistableObject  {
	/**
	 * One should only change the serialVersionUID when the class method signatures have changed.  The
	 * UID should stay the same so that future revisions of the class can still be backwards compatible
	 * with older revisions if method signatures have not changed. 
	 * Serial version format: year (4 digit) - month (2 digit) - yawl release version (4 digit)
	 */
	private static final long serialVersionUID = 2006030080l;
	
    @XmlElement(name="mandatory", namespace="http://www.citi.qut.edu.au/yawl")
    private boolean _mandatory = false;

    @XmlTransient
    private int _ordering;
    @XmlTransient
    private boolean _cutsThroughDecompositionStateSpace;

    @XmlTransient
//    private static final String[] _paramTypes = new String[]{"inputParam", "outputParam", "enablementParam"};
//    public static final int _INPUT_PARAM_TYPE = 0;
    public static final int _OUTPUT_PARAM_TYPE = 1;
    public static final int _ENABLEMENT_PARAM_TYPE = 2;
    @XmlTransient
    private String _paramType;
    @XmlTransient
    private Map<String, String> attributes;

    /**
     * Null constructor inserted for hibernate
     */
    protected YOutputParameter() {
    	super(null, _OUTPUT_PARAM_TYPE);
    }

    /**
     * creates a parameter
     * @param decomposition the parent decomposition
     * @param type use one of the public static type attributes
     */
    public YOutputParameter(YDecomposition decomposition, int type){
        super(decomposition, _OUTPUT_PARAM_TYPE);
//        try{
//            _paramType = _paramTypes[type];
//            if (type == _INPUT_PARAM_TYPE && decomposition != null) {
//            	setParentInputParameters(decomposition);
//            	setParentLocalVariables(null);
//            } else if (type == _OUTPUT_PARAM_TYPE && decomposition != null) {
//            	setParentOutputParameters(decomposition);
//            	setParentLocalVariables(null);
//            } else if (type == _ENABLEMENT_PARAM_TYPE && decomposition != null) {
//            	setParentEnablementParameters(decomposition);
//            	setParentLocalVariables(null);
//            }
//        }catch (ArrayIndexOutOfBoundsException e){
//            throw new IllegalArgumentException("<type> param is not valid.");
//        }
    }

//
//
//    @XmlTransient
//    private YDecomposition parentEnablementParameters;
//	/**
//	 * Only used by hibernate
//	 */
//    @ManyToOne
//	private YDecomposition getParentEnablementParameters() {
//		return parentEnablementParameters;
//	}

//    @Transient
//    public static String getTypeForInput() {
//        return _paramTypes[_INPUT_PARAM_TYPE];
//    }
//
//    @Transient
//    public static String getTypeForOutput() {
//        return _paramTypes[_OUTPUT_PARAM_TYPE];
//    }
//
//    @Transient
//    public static String getTypeForEnablement() {
//        return _paramTypes[_ENABLEMENT_PARAM_TYPE];
//    }
}
