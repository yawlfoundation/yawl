/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.elements;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.ManyToMany;
import javax.persistence.Transient;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.Where;

import au.edu.qut.yawl.elements.data.YParameter;
import au.edu.qut.yawl.elements.data.YVariable;
import au.edu.qut.yawl.util.YVerificationMessage;


/**
 * 
 * @author Lachlan Aldred
 * Date: 25/09/2003
 * Time: 15:45:13
 * 
 * 
 * ***************************************************************************************
 * 
 *  In order for an atomic task to actually do something it needs to decompose to a 
 *  YAWLServiceGateway.  Think of this a special kind of decomposition for an atomic task.  
 *  It is basically used to define the input params and output params of the task, and it 
 *  can optionally define which YAWL service must be invoked when its task is enabled.  
 *  
 */
@Entity
@DiscriminatorValue("service_gateway")
public class YAWLServiceGateway extends YDecomposition {
	/**
	 * One should only change the serialVersionUID when the class method signatures have changed.  The
	 * UID should stay the same so that future revisions of the class can still be backwards compatible
	 * with older revisions if method signatures have not changed. 
	 * Serial version format: year (4 digit) - month (2 digit) - yawl release version (4 digit)
	 */
	private static final long serialVersionUID = 2006030080l;
    private Set<YAWLServiceReference> _yawlServices;
	private List<YParameter>enablementParam = new ArrayList<YParameter>();

    /**
     * Null constructor inserted for hibernate
     *
     */
    protected YAWLServiceGateway() {
    	super();
    	_yawlServices = new HashSet();
    }

    public YAWLServiceGateway(String id, YSpecification specification) {
        super(id, specification);
        _yawlServices = new HashSet();
    }

    @OneToMany(mappedBy="decomposition", cascade=CascadeType.ALL, fetch = FetchType.EAGER)
    @OnDelete(action=OnDeleteAction.CASCADE)
    @Where(clause="DataTypeName='enablementParam'")
    protected List<YParameter> getEnablementParameters() {
    	return enablementParam;
    }
    @OneToMany(mappedBy="decomposition", cascade=CascadeType.ALL, fetch = FetchType.EAGER)
    @OnDelete(action=OnDeleteAction.CASCADE)
    @Where(clause="DataTypeName='enablementParam'")
    protected void setEnablementParameters(List<YParameter> param) {
    	enablementParam.clear();
    	enablementParam.addAll(param);
    }
    

    public List<YVerificationMessage> verify() {
        List<YVerificationMessage> messages = new Vector<YVerificationMessage>();
        messages.addAll(super.verify());
        for (Iterator iterator = enablementParam.iterator(); iterator.hasNext();) {
            YParameter parameter = (YParameter) iterator.next();
            messages.addAll(parameter.verify());
        }
        Collection yawlServices = _yawlServices;
        for (Iterator iterator = yawlServices.iterator(); iterator.hasNext();) {
            YAWLServiceReference yawlService = (YAWLServiceReference) iterator.next();
            List<YVerificationMessage> verificationResult = yawlService.verify();
            for (int i = 0; i < verificationResult.size(); i++) {
                YVerificationMessage message = (YVerificationMessage) verificationResult.get(i);
                message.setSource(this);
            }
            messages.addAll(verificationResult);
        }
        return messages;
    }


    public String toXML() {
        StringBuffer xml = new StringBuffer();
        //just do the decomposition facts (not the surrounding element) - to keep life simple
        xml.append(super.toXML());

        Collection enablementParams = enablementParam;
        for (Iterator iterator = enablementParams.iterator(); iterator.hasNext();) {
            YParameter parameter = (YParameter) iterator.next();
            xml.append(parameter.toXML());
        }
		if( _yawlServices.size() > 0 ) {
			Collection yawlServices = _yawlServices;
			for( Iterator iterator = yawlServices.iterator(); iterator.hasNext(); ) {
				YAWLServiceReference service = (YAWLServiceReference) iterator.next();
				xml.append( service.toXML() );
			}
		}
		return xml.toString();
	}
    
    /**
     * Inserted for hibernate
	 * @hibernate.map role="yawlServices" cascade="all-delete-orphan"
	 * @hibernate.key column="DECOMPOSITION_ID"
	 * @hibernate.index column="YAWL_SERVICE_ID" type="string" length="255" 
	 *   not-null="true"
	 * @hibernate.one-to-many 
	 *   class="au.edu.qut.yawl.elements.YAWLServiceReference"
     */

    @ManyToMany
    protected Set<YAWLServiceReference> getYawlServices() {
    	return _yawlServices;
    }
    /**
     * Inserted for hibernate
     * @param yawlServices
     */
    @ManyToMany
    protected void setYawlServices(Set<YAWLServiceReference> yawlServices) {
    	
    	this._yawlServices.addAll(yawlServices);
    }

    @Transient
    public YAWLServiceReference getYawlService(String yawlServiceID) {
    	for (YAWLServiceReference reference: _yawlServices) {
    		if (reference.getURI().equals(yawlServiceID)) {
    			return reference;
    		}
    	}
    	return null;
    }

    @Transient
    public YAWLServiceReference getYawlService() {
        if (_yawlServices.size() > 0) {
            return _yawlServices.iterator().next();
        }
        return null;
    }

    @Transient
    public void setYawlService(YAWLServiceReference yawlService) {
        if (yawlService != null) {
            _yawlServices.add(yawlService);
        }
    }


    /**
     * Gets the enablement parameters.
     * @return a map of the parameters that become available to yawl
     * services when a task is enabled.
     * 
	 * @hibernate.map role="enablementParameters" cascade="all-delete-orphan"
	 * @hibernate.key column="DECOMPOSITION_ID"
	 * @hibernate.index column="PARAMETER_NAME" type="string" length="255" 
	 *   not-null="true"
	 * @hibernate.one-to-many 
	 *   class="au.edu.qut.yawl.elements.data.YVariable"
     */
    @Transient
    public Map<String, YVariable> getEnablementParametersMap() {
    	Map<String, YVariable> map = new HashMap<String, YVariable>();
    	for(YVariable variable:enablementParam) {
    		if (null != variable.getName()) {
    			map.put(variable.getName(), variable);
    		} else if (null != variable.getElementName()) {
    			map.put(variable.getElementName(), variable);
    		}
    	}
        return map;
    }

    /**
     * These parameters become available to yawl services when a task is enabled.
     * @param parameter the parameter
     */
    public void setEnablementParameter(YParameter parameter) {
        if (YParameter.getTypeForEnablement().equals(parameter.getDirection())) {
            if (null != parameter.getName()) {
            	enablementParam.add(parameter);
            } else if (null != parameter.getElementName()) {
            	enablementParam.add(parameter);
            }
        } else {
            throw new RuntimeException("Can only set enablement type param as such.");
        }
    }
    
    public YDecomposition deepClone() {
        return deepClone( new YAWLServiceGateway() );
    }
    
    @Override
    protected YDecomposition deepClone( YDecomposition gateway ) {
        super.deepClone( gateway );
        try {
            YAWLServiceGateway clone = (YAWLServiceGateway) gateway;
            
            clone._yawlServices = new HashSet<YAWLServiceReference>();
            for( YAWLServiceReference reference : _yawlServices ) {
                YAWLServiceReference refClone = new YAWLServiceReference();
                refClone.setDocumentation( reference.getDocumentation() );
                refClone.setYawlServiceGateway( clone );
                refClone.setYawlServiceID( reference.getYawlServiceID() );
                clone._yawlServices.add( refClone );
            }
            
            clone.enablementParam = new ArrayList<YParameter>();
            for( YParameter parameter : enablementParam ) {
                YParameter cloneParam = (YParameter) parameter.clone();
                cloneParam.setParent( clone );
                clone.enablementParam.add( cloneParam );
            }
            
            return clone;
        }
        catch( CloneNotSupportedException e ) {
            throw new Error( e );
        }
    }
}

