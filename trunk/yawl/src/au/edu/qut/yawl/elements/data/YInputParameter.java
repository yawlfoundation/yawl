/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.elements.data;

import java.util.List;
import java.util.Vector;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

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
@DiscriminatorValue("input")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "InputParameterFactsType", namespace="http://www.citi.qut.edu.au/yawl"
//	,propOrder = {"_mandatory"}
)
public class YInputParameter extends YParameter implements PolymorphicPersistableObject  {
	/**
	 * One should only change the serialVersionUID when the class method signatures have changed.  The
	 * UID should stay the same so that future revisions of the class can still be backwards compatible
	 * with older revisions if method signatures have not changed. 
	 * Serial version format: year (4 digit) - month (2 digit) - yawl release version (4 digit)
	 */
	private static final long serialVersionUID = 2006030080l;
	
//    @XmlElement(name="mandatory", namespace="http://www.citi.qut.edu.au/yawl")
//    private boolean _mandatory = false;

//    @XmlTransient
//    private String _paramType;

    /**
     * Null constructor inserted for hibernate
     */
    protected YInputParameter() {
    }

   

    /**
     * creates a parameter
     * @param decomposition the parent decomposition
     * @param type use one of the public static type attributes
     */
    public YInputParameter(YDecomposition decomposition, int type){
        super(decomposition, _INPUT_PARAM_TYPE);
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

    /**
     * Constructs a parameter using a type string.
     * @param decomposition the decomposition
     * @param type the parameter type (inputParam /outputParam /enablementParam).
     */
    public YInputParameter(YDecomposition decomposition, String type) {
        super(decomposition, _INPUT_PARAM_TYPE);
//        List types = Arrays.asList(_paramTypes);
//        if(! types.contains(type)){
//            throw new IllegalArgumentException("Type (" + type + ") is not valid.");
//        }
//        _paramType = type;
//
//        if (type.equals("inputParam") && decomposition != null) {
//        	setParentInputParameters(decomposition);
//        	setParentLocalVariables(null);
//        } else if (type.equals("outputParam") && decomposition != null) {
//        	setParentOutputParameters(decomposition);
//        	setParentLocalVariables(null);
//        } else if (type.equals("enablementParam") && decomposition != null) {
//        	setParentEnablementParameters(decomposition);
//        	setParentLocalVariables(null);
//        }
    }



//    @XmlTransient
//    private YDecomposition parentEnablementParameters;
//	/**
//	 * Only used by hibernate
//	 */
//    @ManyToOne
//	private YDecomposition getParentEnablementParameters() {
//		return parentEnablementParameters;
//	}
//
//	/**
//	 * Only used by hibernate
//	 * @param parentEnablementParameters
//	 */
//	protected void setParentEnablementParameters( YDecomposition parentEnablementParameters ) {
//		this.parentEnablementParameters = parentEnablementParameters;
//	}
//    
    
//    /**
//     * Establishes whether or not the parameter is meant to be mandatory
//     * @param isMandatory
//     */
//    public void setManadatory(boolean isMandatory){
//        //todo make this mean something to the engine because at the moment it means nothing
//        _mandatory = isMandatory;
//    }


//    /**
//     * Set whether the param bypasses the decomposition's state space.  Can only be set
//     * on an input param.
//     * @param isCutThroughParam is yes then true.
//     */
//    public void setIsCutThroughParam(boolean isCutThroughParam) {
//        if(_paramType.equals(_paramTypes[_OUTPUT_PARAM_TYPE])){
//            _cutsThroughDecompositionStateSpace = isCutThroughParam;
//        }
//        else {
//            throw new IllegalArgumentException("Cannot be set on input param.");
//        }
//    }


    /**
     * 
     * @return
     * @hibernate.property column="IS_MANDATORY"
     */
//    @Column(name="is_mandatory")
//    public boolean isMandatory() {
//        return _mandatory;
//    }
//    /**
//     * Inserted for hibernate
//     * @param b
//     */
//    protected void setMandatory(boolean b) {
//    	_mandatory = b;
//    }
//


    /**
     * 
     * @return
     * @hibernate.property column="DIRECTION" length="255"
     */
//    @Column(name="direction")
//    public String getDirection() {
//        return _paramType;
//    }
    /**
     * Inserted for hibernate
     * @param s
     */
//    protected void setDirection(String s) {
//    	_paramType = s;
//    }
//

//    public String toXML() {
//        StringBuffer xml = new StringBuffer();
//
//        xml.append("<" + _paramType);
//
//        /**
//         * AJH: Output any attributes for this parameter
//         */
//        if ((getAttributes() != null) && (_paramType.equals(_paramTypes[_INPUT_PARAM_TYPE])))
//        {
//            Iterator iter = getAttributes().keySet().iterator();
//            while(iter.hasNext())
//            {
//                String attrName = (String)iter.next();
//                xml.append(" " + attrName + "=\"" + (String)getAttributes().get(attrName) + "\"");
//            }
//        }
//
//        xml.append(toXMLGuts());
//        if (_mandatory) {
//            xml.append("<mandatory/>");
//        }
//        if (_cutsThroughDecompositionStateSpace) {
//            xml.append("<bypassesStatespaceForDecomposition/>");
//        }
//
//        xml.append("</" + _paramType + ">");
//
//        return xml.toString();
//    }


    public List <YVerificationMessage> verify() {
        List<YVerificationMessage> messages =
                new Vector<YVerificationMessage>();
        messages.addAll(super.verify());
        if (super.isMandatory() && _initialValue != null) {
            messages.add(new YVerificationMessage(this,
                    this + "cannot be mandatory and have initial value.",
                    YVerificationMessage.ERROR_STATUS));
        }
        return messages;
    }

//    @Transient
//    private boolean isParamType(int paramType) {
//       return _paramTypes[paramType].equals(_paramType);
//    }
}
