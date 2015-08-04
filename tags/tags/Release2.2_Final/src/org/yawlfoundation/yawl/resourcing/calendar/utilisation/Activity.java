/*
 * Copyright (c) 2004-2011 The YAWL Foundation. All rights reserved.
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

import java.util.ArrayList;
import java.util.List;

/**
 * @author Michael Adams
 * @date 6/10/2010
 */
public class Activity extends StatusMessage {

    private String _name;
    private String _type;
    private String _startTaskID;
    private String _endTaskID;
    private String _phase;
    private StringWithMessage _from;
    private StringWithMessage _to;
    private StringWithMessage _duration;
    private List<Reservation> _reservationList;
    private List<XNode> _relationList;
    private List<XNode> _transferList;

    public static enum Phase { POU, SOU, EOU }      // pre, start of, end of, utilisation


    public Activity() { }

    public Activity(String name, String startTaskID, String endTaskID,
                    String from, String to, String duration) {
        setName(name);
        setStartTaskID(startTaskID);
        setStartTaskID(endTaskID);
        setFrom(from);
        setTo(to);
        setDuration(duration);
    }

    public Activity(XNode node) {
        fromXNode(node);
    }

    /**************************************************************************/

    public String getName() { return _name; }

    public void setName(String name) { _name = name; }


    public String getType() { return _type; }

    public void setType(String type) { _type = type; }


    public String getStartTaskID() { return _startTaskID; }

    public void setStartTaskID(String id) { _startTaskID = id; }


    public String getEndTaskID() { return _endTaskID; }

    public void setEndTaskID(String id) { _endTaskID = id; }


    public String getPhase() { return _phase; }

    public void setPhase(String phase) { _phase = phase; }

    public boolean hasValidPhase() {
        try {
            Phase.valueOf(_phase);
            return true;
        }
        catch (Exception e)  {
            return false;
        }
    }

    public boolean isSOU() { return _phase.equals(Phase.SOU.name()); }

    public boolean isEOU() { return _phase.equals(Phase.EOU.name()); }


    public String getFrom() {
        return (_from != null) ? _from.getValue() : null;
    }

    public void setFrom(String from) {
        if (_from == null) _from = new StringWithMessage("From");
        _from.setValue(from);
    }

    public long getFromAsLong() { return getTime(_from); }


    public String getTo() {
        return (_to != null) ? _to.getValue() : null; 
    }

    public void setTo(String to) {
        if (_to == null) _to = new StringWithMessage("To");
        _to.setValue(to);
    }

    public long getToAsLong() { return getTime(_to); }


    // duration is stored as String that represents an xs:duration value
    public String getDuration() {
        return (_duration != null) ? _duration.getValue() : null;
    }

    public void setDuration(String duration) {
        if (_duration == null) _duration = new StringWithMessage("Duration");
        _duration.setValue(duration);
    }

    public long getDurationMSecs() {
        return (_duration != null) ? StringUtil.durationStrToMSecs(_duration.getValue()) : 0;
    }


    public List<Reservation> getReservationList() {
        return _reservationList;
    }

    public void setReservationList(List<Reservation> list) {
        _reservationList = list;
    }

    public boolean addReservation(Reservation r) {
        if (_reservationList == null) _reservationList = new ArrayList<Reservation>();
        return _reservationList.add(r);
    }

    public boolean removeReservation(Reservation r) {
        return (hasReservation()) && _reservationList.remove(r);
    }

    public boolean hasReservation() {
        return ! ((_reservationList == null) || _reservationList.isEmpty());
    }

    public List<XNode> getRelationList() {
        return _relationList;
    }

    public void setRelationList(List<XNode> list) {
        _relationList = list;
    }

    public List<XNode> getTransferList() {
        return _transferList;
    }

    public void setTransferList(List<XNode> list) {
        _transferList = list;
    }


    public boolean hasErrors() {
        return hasError() ||
               StringWithMessage.hasError(_from) ||
               StringWithMessage.hasError(_to) ||
               StringWithMessage.hasError(_duration) ||
               reservationsHaveErrors();
    }


    public boolean reservationsHaveErrors() {
        if (hasReservation()) {
            for (Reservation reservation : _reservationList) {
                if (reservation.hasErrors()) {
                    return true;
                }
            }
        }
        return false;
    }


    public String toXML() {
        return toXNode().toString();
    }

    
    public XNode toXNode() {
        XNode node = new XNode("Activity");
        addAttributes(node);
        node.addChild("StartTaskId", _startTaskID);
        node.addChild("EndTaskId", _startTaskID);
        node.addChild("ActivityName", _name);
        node.addChild("ActivityType", _type);
        if (_duration != null) node.addChild(_duration.toXNode());
        if (_from != null) node.addChild(_from.toXNode());
        if (_to != null) node.addChild(_to.toXNode());
        node.addChild("RequestType", _phase);
        if (_reservationList != null) {
            for (Reservation r : _reservationList) {
                node.addChild(r.toXNode());
            }
        }
        if (_relationList != null) node.addChildren(_relationList);
        if (_transferList != null) node.addChildren(_transferList);
        return node;
    }


    public void fromXNode(XNode node) {
        super.fromXNode(node);
        setName(node.getChildText("ActivityName"));
        setType(node.getChildText("ActivityType"));
        setStartTaskID(node.getChildText("StartTaskId"));
        setEndTaskID(node.getChildText("EndTaskId"));
        setPhase(node.getChildText("RequestType"));
        if (node.hasChild("From")) setFrom(node.getChildText("From"));
        if (node.hasChild("To")) setTo(node.getChildText("To"));
        if (node.hasChild("Duration")) setDuration(node.getChildText("Duration"));
        for (XNode reservationNode : node.getChildren("Reservation")) {
            addReservation(new Reservation(reservationNode));
        }

        // these 2 element sets are never modified, so are left undeconstructed
        _relationList = node.getChildren("UtilisationRelation");
        _transferList = node.getChildren("MessageTransfer");        
    }


    private long getTime(StringWithMessage smTime) {
        long time = StringUtil.xmlDateToLong(smTime.getValue());
        if (time < 0) smTime.setError("Invalid dateTime value: " + smTime.getValue());
        return time;        
    }

}
