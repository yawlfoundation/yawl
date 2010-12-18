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

    private String _reservationID;
    private StringWithMessage _statusToBe;
    private StringWithMessage _status;
    private UtilisationResource _resource;
    private StringWithMessage _workload;

    public Reservation() { }

    public Reservation(String id, String statusToBe, String status,
                       UtilisationResource resource, int workload) {
        setReservationID(id);
        setStatusToBe(statusToBe);
        setStatus(status);
        setResource(resource);
        setWorkload(workload);
    }

    public Reservation(XNode node) {
        fromXNode(node);
    }


    /************************************************************************/

    public String getReservationID() { return _reservationID; }

    public boolean isUpdate() { return (_reservationID != null); }

    public void setReservationID(String id) {
        _reservationID = id;
    }

    public long getReservationIDAsLong() {
        return StringUtil.strToLong(_reservationID, -1);
    }


    public String getStatusToBe() {
        return (_statusToBe != null) ? _statusToBe.getValue() : null;
    }

    public void setStatusToBe(String status) {
        if (_statusToBe == null) _statusToBe = new StringWithMessage("StatusToBe");
        _statusToBe.setValue(status);
    }


    public String getStatus() {
        return (_status != null) ? _status.getValue() : null;
    }

    public void setStatus(String status) {
        if (_status == null) _status = new StringWithMessage("Status");
        _status.setValue(status);
    }


    public UtilisationResource getResource() { return _resource; }

    public void setResource(UtilisationResource resource) {
        _resource = resource;
    }

    public boolean hasResource() { return _resource != null; }


    public int getWorkload() {
        return (_workload != null) ? _workload.getIntValue() : 0;
    }

    public void setWorkload(int workload) {
        if (_workload == null) _workload = new StringWithMessage("Workload");
        _workload.setValue(workload);
    }


    public boolean hasErrors() {
        return hasError() ||
               StringWithMessage.hasError(_statusToBe) ||
               StringWithMessage.hasError(_status) ||
               StringWithMessage.hasError(_workload) ||
               (hasResource() && _resource.hasErrors()) ;
    }


    public String toXML() {
        return toXNode().toString();
    }

    public XNode toXNode() {
        XNode node = new XNode("Reservation");
        addAttributes(node);
        if (_reservationID != null) node.addChild("ReservationId", _reservationID);
        if (StringWithMessage.hasData(_statusToBe)) node.addChild(_statusToBe.toXNode());
        if (StringWithMessage.hasData(_status)) node.addChild(_status.toXNode());
        if (_resource != null) node.addChild(_resource.toXNode());
        if (StringWithMessage.hasData(_workload)) node.addChild(_workload.toXNode());
        return node;
    }

    public void fromXNode(XNode node) {
        super.fromXNode(node);
        setReservationID(node.getChildText("ReservationId"));
        setStatusToBe(node.getChildText("StatusToBe"));
        setStatus(node.getChildText("Status"));
        XNode resourceNode = node.getChild("Resource");
        if (resourceNode != null) setResource(new UtilisationResource(resourceNode));
        setWorkload(StringUtil.strToInt(node.getChildText("Workload"), 100));
    }
}
