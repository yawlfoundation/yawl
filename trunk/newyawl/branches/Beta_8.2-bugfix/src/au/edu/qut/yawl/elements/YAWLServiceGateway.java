/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.elements;

import au.edu.qut.yawl.elements.data.YParameter;
import au.edu.qut.yawl.util.YVerificationMessage;

import java.util.*;

/**
 * 
 * @author Lachlan Aldred
 * Date: 25/09/2003
 * Time: 15:45:13
 * 
 */
public class YAWLServiceGateway extends YDecomposition implements YVerifiable {
    private Map _yawlServices;
    private Map _enablementParameters;  //name --> parameter

    public YAWLServiceGateway(String id, YSpecification specification) {
        super(id, specification);
        _yawlServices = new HashMap();
        _enablementParameters = new HashMap();
    }


    public List verify() {
        List messages = new Vector();
        messages.addAll(super.verify());
        for (Iterator iterator = _enablementParameters.values().iterator(); iterator.hasNext();) {
            YParameter parameter = (YParameter) iterator.next();
            messages.addAll(parameter.verify());
        }
        Collection yawlServices = _yawlServices.values();
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

        Collection enablementParams = _enablementParameters.values();
        for (Iterator iterator = enablementParams.iterator(); iterator.hasNext();) {
            YParameter parameter = (YParameter) iterator.next();
            xml.append(parameter.toXML());
        }
        Collection yawlServices = _yawlServices.values();
        for (Iterator iterator = yawlServices.iterator(); iterator.hasNext();) {
            YAWLServiceReference service = (YAWLServiceReference) iterator.next();
            xml.append(service.toXML());
        }
        return xml.toString();
    }


    public YAWLServiceReference getYawlService(String yawlServiceID) {
        return (YAWLServiceReference) _yawlServices.get(yawlServiceID);
    }

    public YAWLServiceReference getYawlService() {
        if (_yawlServices.values().size() > 0) {
            return (YAWLServiceReference) _yawlServices.values().iterator().next();
        }
        return null;
    }


    public void setYawlService(YAWLServiceReference yawlService) {
        if (yawlService != null) {
            _yawlServices.put(yawlService.getURI(), yawlService);
        }
    }


    /**
     * Gets the enablement parameters.
     * @return a map of the parameters that become available to yawl
     * services when a task is enabled.
     */
    public Map getEnablementParameters() {
        return _enablementParameters;
    }

    /**
     * Returns the parameter names for enablement.
     * @return the set of parameter names.
     */
    protected Set getEnablementParameterNames() {
        return _enablementParameters.keySet();
    }

    /**
     * These parameters become available to yawl services when a task is enabled.
     * @param parameter the parameter
     */
    public void setEnablementParameter(YParameter parameter) {
        if (YParameter.getTypeForEnablement().equals(parameter.getDirection())) {
            if (null != parameter.getName()) {
                _enablementParameters.put(parameter.getName(), parameter);
            } else if (null != parameter.getElementName()) {
                _enablementParameters.put(parameter.getElementName(), parameter);
            }
        } else {
            throw new RuntimeException("Can only set enablement type param as such.");
        }
    }

}
