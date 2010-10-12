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

package org.yawlfoundation.yawl.resourcing.calendar.utilisation;

import org.yawlfoundation.yawl.util.StringUtil;
import org.yawlfoundation.yawl.util.XNode;

/**
 * @author Michael Adams
* @date 6/10/2010
*/
public class Reservation extends StatusMessage {

    private StringWithMessage _statusToBe;
    private StringWithMessage _status;
    private UtilisationResource _resource;
    private StringWithMessage _workload;

    public Reservation() { }

    public Reservation(String statusToBe, String status,
                       UtilisationResource resource, int workload) {
        setStatusToBe(statusToBe);
        setStatus(status);
        setResource(resource);
        setWorkload(workload);
    }

    public Reservation(XNode node) {
        fromXNode(node);
    }


    /************************************************************************/

    public String getStatusToBe() { return _statusToBe.getValue(); }

    public void setStatusToBe(String status) {
        if (_statusToBe == null) _statusToBe = new StringWithMessage("StatusToBe");
        _statusToBe.setValue(status);
    }


    public String getStatus() { return _status.getValue(); }

    public void setStatus(String status) {
        if (_status == null) _status = new StringWithMessage("Status");
        _status.setValue(status);
    }


    public UtilisationResource getResource() { return _resource; }

    public void setResource(UtilisationResource resource) {
        _resource = resource;
    }


    public int getWorkload() { return _workload.getIntValue(); }

    public void setWorkload(int workload) {
        if (_workload == null) _workload = new StringWithMessage("Workload");
        _workload.setValue(workload);
    }


    public String toXML() {
        return toXNode().toString();
    }

    public XNode toXNode() {
        XNode node = new XNode("Reservation");
        addAttributes(node);
        if (_statusToBe != null) node.addChild(_statusToBe.toXNode());
        if (_status != null) node.addChild(_status.toXNode());
        if (_resource != null) node.addChild(_resource.toXNode());
        if (_workload != null) node.addChild(_workload.toXNode());
        return node;
    }

    public void fromXNode(XNode node) {
        super.fromXNode(node);
        setStatusToBe(node.getChildText("StatusToBe"));
        setStatus(node.getChildText("Status"));
        setResource(new UtilisationResource(node.getChild("Resource")));
        setWorkload(StringUtil.strToInt(node.getChildText("Workload"), -1));
    }
}
