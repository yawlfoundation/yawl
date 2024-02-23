/*
 * Copyright (c) 2004-2020 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.stateless.elements;

import org.yawlfoundation.yawl.elements.YVerifiable;
import org.yawlfoundation.yawl.util.YVerificationHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * A decomposition associated with a Web Service Gateway.
 * <p/>
 * A web service gateway contains a reference to a YAWL Service, which represents the
 * service that will take responsibility for the execution of any task based on this
 * gateway decomposition.
 * @author Lachlan Aldred
 * @author Michael Adams (updates for v2.0)
 * @since 0.1
 * @date 25/09/2003
 */
public class YAWLServiceGateway extends YDecomposition implements YVerifiable {

    private Map<String, YAWLServiceReference> _yawlServices;


    /**
     * Constructs a new web service gateway decomposition.
     * @param id the service gateway identifier. Typically this is the same as its name.
     * @param specification the comtaining specification of this service gateway.
     */
    public YAWLServiceGateway(String id, YSpecification specification) {
        super(id, specification);
        _yawlServices = new HashMap<String, YAWLServiceReference>();
    }


    /**
     * Verifies this service gateway decomposition against YAWL semantics.
     * @return a List of error and/or warning messages. An empty list is returned if
     * the verification is successful.
     */
    public void verify(YVerificationHandler handler) {
        super.verify(handler);
        for (YAWLServiceReference yawlService : _yawlServices.values()) {
            yawlService.verify(handler);
        }
    }


    /**
     * Outputs this service gateway to an XML representation.
     * @return an XML (String) representation of this service gateway
     */
    public String toXML() {
        StringBuilder xml = new StringBuilder();

        //just do the decomposition facts (not the surrounding element) - to keep life simple
        xml.append(super.toXML());

        for (YAWLServiceReference service : _yawlServices.values())  {
            xml.append(service.toXML());
        }
        return xml.toString();
    }


    /**
     * Gets the named YAWL Service associated with this gateway.
     * @param yawlServiceID the identifier of the service.
     * @return the Service associated with this gateway iff the service matches the id
     * passed, or null if it doesn't match ori f there is no associated gateway.
     */
    public YAWLServiceReference getYawlService(String yawlServiceID) {
        return _yawlServices.get(yawlServiceID);
    }


    /**
     * Gets the YAWL Service associated with this gateway.
     * @return the Service associated with this gateway, or null if there is no
     * associated gateway.
     */
    public YAWLServiceReference getYawlService() {
        if (_yawlServices.values().size() > 0) {
            return _yawlServices.values().iterator().next();
        }
        return null;
    }


    /**
     * Sets the YAWL Service associated with this gateway.
     * @param yawlService the Service to associate with this gateway.
     */
    public void setYawlService(YAWLServiceReference yawlService) {
        if (yawlService != null) {
            _yawlServices.put(yawlService.getURI(), yawlService);
        }
    }


    /**
     * Clears any service associated with this gateway. Used at design time
     */
    public void clearYawlService() { _yawlServices.clear(); }

}
