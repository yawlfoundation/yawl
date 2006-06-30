/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.elements.data;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import au.edu.qut.yawl.elements.YDecomposition;
import au.edu.qut.yawl.persistence.PolymorphicPersistableObject;

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
public class YOutputParameter extends YParameter implements PolymorphicPersistableObject  {
	/**
	 * One should only change the serialVersionUID when the class method signatures have changed.  The
	 * UID should stay the same so that future revisions of the class can still be backwards compatible
	 * with older revisions if method signatures have not changed. 
	 * Serial version format: year (4 digit) - month (2 digit) - yawl release version (4 digit)
	 */
	private static final long serialVersionUID = 2006030080l;

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
