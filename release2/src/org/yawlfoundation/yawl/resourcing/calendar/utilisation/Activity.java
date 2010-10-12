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

import java.util.ArrayList;
import java.util.List;

/**
 * @author Michael Adams
* @date 6/10/2010
*/
public class Activity extends StatusMessage {

    private String _name;
    private StringWithMessage _from;
    private StringWithMessage _to;
    private StringWithMessage _duration;
    private List<Reservation> _reservationList;
    private List<UtilisationRelation> _relationList;

    public Activity() { }

    public Activity(String name, String from, String to, int duration) {
        setName(name);
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


    public String getFrom() { return _from.getValue(); }

    public void setFrom(String from) {
        if (_from == null) _from = new StringWithMessage("From");
        _from.setValue(from);
    }


    public String getTo() { return _to.getValue(); }

    public void setTo(String to) {
        if (_to == null) _to = new StringWithMessage("To");
        _to.setValue(to);
    }


    public int getDuration() { return _duration.getIntValue(); }

    public void setDuration(int duration) {
        if (_duration == null) _duration = new StringWithMessage("Duration");
        _duration.setValue(duration);
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
        return (_reservationList != null) && _reservationList.remove(r);
    }


    public List<UtilisationRelation> getRelationList() {
        return _relationList;
    }

    public void setRelationList(List<UtilisationRelation> list) {
        _relationList = list;
    }

    public boolean addUtilisationRelation(UtilisationRelation r) {
        if (_relationList == null) _relationList = new ArrayList<UtilisationRelation>();
        return _relationList.add(r);
    }

    public boolean removeUtilisationRelation(UtilisationRelation r) {
        return (_relationList != null) && _relationList.remove(r);
    }


    public String toXML() {
        return toXNode().toString();
    }

    public XNode toXNode() {
        XNode node = new XNode("Activity");
        addAttributes(node);
        node.addChild("Name", _name);
        if (_from != null) node.addChild(_from.toXNode());
        if (_to != null) node.addChild(_to.toXNode());
        if (_duration != null) node.addChild(_duration.toXNode());
        if (_reservationList != null) {
            for (Reservation r : _reservationList) {
                node.addChild(r.toXNode());
            }
        }
        if (_relationList != null) {
            for (UtilisationRelation r : _relationList) {
                node.addChild(r.toXNode());
            }
        }
        return node;
    }

    public void fromXNode(XNode node) {
        super.fromXNode(node);
        setName(node.getChildText("Name"));
        setFrom(node.getChildText("From"));
        setTo(node.getChildText("To"));
        setDuration(StringUtil.strToInt(node.getChildText("Duration"), -1));
        for (XNode reservationNode : node.getChildren("Reservation")) {
            addReservation(new Reservation(reservationNode));
        }
        for (XNode relationNode : node.getChildren("UtilisationRelation")) {
            addUtilisationRelation(new UtilisationRelation(relationNode));
        }
    }

}
