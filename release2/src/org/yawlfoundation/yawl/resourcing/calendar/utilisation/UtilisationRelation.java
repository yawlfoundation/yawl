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
public class UtilisationRelation extends StatusMessage {

    private StringWithMessage _thisType;
    private StringWithMessage _otherType;
    private StringWithMessage _otherActivityName;
    private StringWithMessage _min;
    private StringWithMessage _max;

    public UtilisationRelation() { }

    public UtilisationRelation(String thisType, String otherType, String name) {
        setThisType(thisType);
        setOtherType(otherType);
        setOtherActivityName(name);
    }

    public UtilisationRelation(String thisType, String otherType, String name,
                               int min, int max) {
        this(thisType, otherType, name);
        setMin(min);
        setMax(max);
    }

    public UtilisationRelation(XNode node) {
        fromXNode(node);
    }
    
    /*************************************************************************/

    public String getThisType() { return _thisType.getValue(); }

    public void setThisType(String type) {
        if (_thisType == null) {
            _thisType = new StringWithMessage("ThisUtilisationType");
        }
        _thisType.setValue(type);
    }


    public String getOtherType() { return _otherType.getValue(); }

    public void setOtherType(String type) {
        if (_otherType == null) {
            _otherType = new StringWithMessage("OtherUtilisationType");
        }
        _otherType.setValue(type);
    }


    public String getOtherActivityName() { return _otherActivityName.getValue(); }

    public void setOtherActivityName(String name) {
        if (_otherActivityName == null) {
            _otherActivityName = new StringWithMessage("OtherActivityName");
        }
        _otherActivityName.setValue(name);
    }


    public int getMin() { return _min.getIntValue(); }

    public void setMin(int min) {
        if (_min == null) _min = new StringWithMessage("Min");
        _min.setValue(min);
    }


    public int getMax() { return _max.getIntValue(); }

    public void setMax(int max) {
        if (_max == null) _max = new StringWithMessage("Max");
        _max.setValue(max);
    }


    public String toXML() {
        return toXNode().toString();
    }

    public XNode toXNode() {
        XNode node = new XNode("UtilisationRelation");
        addAttributes(node);
        if (_thisType != null) node.addChild(_thisType.toXNode());
        if (_otherType != null) node.addChild(_otherType.toXNode());
        if (_otherActivityName != null) node.addChild(_otherActivityName.toXNode());
        if (_min != null) node.addChild(_min.toXNode());
        if (_max != null) node.addChild(_max.toXNode());
        return node;
    }

    public void fromXNode(XNode node) {
        super.fromXNode(node);
        setThisType(node.getChildText("ThisUtilisationType"));
        setOtherType(node.getChildText("OtherUtilisationType"));
        setOtherActivityName(node.getChildText("OtherActivityName"));
        setMin(StringUtil.strToInt(node.getChildText("Min"), -1));
        setMax(StringUtil.strToInt(node.getChildText("Max"), -1));
    }
}
