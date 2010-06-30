/*
 * Copyright (c) 2004-2010 The YAWL Foundation. All rights reserved.
 * The YAWL Foundation is a collaboration of individuals and
 * organisations who are committed to improving workflow technology.
 *
 * This file is part of YAWL. YAWL is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation.
 *
 * YAWL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with YAWL. If not, see <http://www.gnu.org/licenses/>.
 */

package org.yawlfoundation.yawl.elements;

import org.yawlfoundation.yawl.elements.data.YParameter;
import org.yawlfoundation.yawl.util.YVerificationMessage;

import java.util.*;

/**
 *
 * @author Lachlan Aldred
 * @author Michael Adams (updates for v2.0)
 * @since 0.1
 * @date 25/09/2003
 */
public class YAWLServiceGateway extends YDecomposition implements YVerifiable {
    private Map<String, YAWLServiceReference> _yawlServices;
    private Map<String, YParameter> _enablementParameters;  //name --> parameter

    public YAWLServiceGateway(String id, YSpecification specification) {
        super(id, specification);
        _yawlServices = new HashMap<String, YAWLServiceReference>();
        _enablementParameters = new HashMap<String, YParameter>();
    }


    public List<YVerificationMessage> verify() {
        List<YVerificationMessage> messages = new Vector<YVerificationMessage>();
        messages.addAll(super.verify());
        for (YParameter parameter : _enablementParameters.values()) {
            messages.addAll(parameter.verify());
        }
        for (YAWLServiceReference yawlService : _yawlServices.values()) {
            List<YVerificationMessage> verificationResult = yawlService.verify();
            for (YVerificationMessage message : verificationResult) {
                message.setSource(this);
            }
            messages.addAll(verificationResult);
        }
        return messages;
    }


    public String toXML() {
        StringBuilder xml = new StringBuilder();

        //just do the decomposition facts (not the surrounding element) - to keep life simple
        xml.append(super.toXML());

        for (YParameter parameter : _enablementParameters.values()) {
            xml.append(parameter.toXML());
        }
        for (YAWLServiceReference service : _yawlServices.values())  {
            xml.append(service.toXML());
        }
        return xml.toString();
    }


    public YAWLServiceReference getYawlService(String yawlServiceID) {
        return _yawlServices.get(yawlServiceID);
    }

    public YAWLServiceReference getYawlService() {
        if (_yawlServices.values().size() > 0) {
            return _yawlServices.values().iterator().next();
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
    public Map<String, YParameter> getEnablementParameters() {
        return _enablementParameters;
    }

    /**
     * Returns the parameter names for enablement.
     * @return the set of parameter names.
     */
    protected Set<String> getEnablementParameterNames() {
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
