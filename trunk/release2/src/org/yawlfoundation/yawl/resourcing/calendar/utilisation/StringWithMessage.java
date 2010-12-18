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
public class StringWithMessage extends StatusMessage {

    private String _key;
    private String _value;

    public StringWithMessage() { }

    public StringWithMessage(String key) {
        _key = key;
    }

    public StringWithMessage(int i) {
        setValue(i);
    }


    public StringWithMessage(XNode node) {
        fromXNode(node);
    }


    public static boolean hasError(StringWithMessage strMsg) {
        return (strMsg != null) && strMsg.hasError();
    }


    public static boolean hasData(StringWithMessage strMsg) {
        return (strMsg != null) && strMsg.hasData();        
    }


    public String getKey() { return _key; }

    public void setKey(String key) { _key = key; }


    public void setValue(String s) { _value = s; }

    public void setValue(int i) { _value = String.valueOf(i); }

    public String getValue() { return _value; }

    public int getIntValue() { return StringUtil.strToInt(_value, -1); }

    public boolean hasData() { return (_value != null) || hasMessage(); }

    public XNode toXNode() {
        XNode node = super.toXNode(_key);
        node.setText(_value);
        return node;
    }

    public void fromXNode(XNode node) {
        super.fromXNode(node);
        setValue(node.getText());
    }
}
