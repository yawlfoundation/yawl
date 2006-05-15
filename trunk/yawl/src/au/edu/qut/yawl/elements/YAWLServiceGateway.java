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

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import au.edu.qut.yawl.elements.data.YParameter;
import au.edu.qut.yawl.elements.data.YVariable;
import au.edu.qut.yawl.persistence.PolymorphicPersistableObject;
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
@XmlRootElement(name="WebServiceGatewayFactsType", namespace="http://www.citi.qut.edu.au/yawl")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "WebServiceGatewayFactsType", namespace="http://www.citi.qut.edu.au/yawl", 
		propOrder = {
    "enablementParam"
//    "yawlService"
})
public class YAWLServiceGateway extends YDecomposition implements YVerifiable, PolymorphicPersistableObject {
	/**
	 * One should only change the serialVersionUID when the class method signatures have changed.  The
	 * UID should stay the same so that future revisions of the class can still be backwards compatible
	 * with older revisions if method signatures have not changed. 
	 * Serial version format: year (4 digit) - month (2 digit) - yawl release version (4 digit)
	 */
	private static final long serialVersionUID = 2006030080l;
	@XmlTransient
    private Set<YAWLServiceReference> _yawlServices;
	@XmlElement(name="enablementParam", namespace="http://www.citi.qut.edu.au/yawl")
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

    @OneToMany(mappedBy="parentEnablementParameters")
    protected List<YParameter> getEnablementParameters() {
    	return enablementParam;
    }
    protected void setEnablementParameters(List<YParameter> param) {
    	enablementParam.clear();
    	enablementParam.addAll(param);
    }
    

    public List verify() {
        List messages = new Vector();
        messages.addAll(super.verify());
        for (Iterator iterator = enablementParam.iterator(); iterator.hasNext();) {
            YParameter parameter = (YParameter) iterator.next();
            messages.addAll(parameter.verify());
        }
        Collection yawlServices = _yawlServices;
        for (Iterator iterator = yawlServices.iterator(); iterator.hasNext();) {
            YAWLServiceReference yawlService = (YAWLServiceReference) iterator.next();
            List verificationResult = yawlService.verify();
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
    @OneToMany
        @JoinTable(
            name="YawlServices",
            joinColumns = { @JoinColumn( name="decomp_id") },
            inverseJoinColumns = @JoinColumn( name="yawlServiceID")
    )
    protected Set<YAWLServiceReference> getYawlServices() {
    	return _yawlServices;
    }
    /**
     * Inserted for hibernate
     * @param yawlServices
     */
    protected void setYawlServices(Set<YAWLServiceReference> yawlServices) {
    	_yawlServices.clear();
    	_yawlServices.addAll(yawlServices);
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
    @XmlTransient
    public YAWLServiceReference getYawlService() {
        if (_yawlServices.size() > 0) {
            return (YAWLServiceReference) _yawlServices.iterator().next();
        }
        return null;
    }


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
    @XmlTransient
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

}
