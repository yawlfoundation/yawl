/*
 * Copyright (c) 2004-2012 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.resourcing.jsf.dynform;

import org.yawlfoundation.yawl.elements.data.YParameter;

import java.io.Serializable;
import java.util.Map;

/**
 *  Adds a few extra members to a YParameter object, so it can be used to populate
 *  JSF pages.
 *
 *  @author Michael Adams
 *  Date: 9/01/2008 (updated 09/2014)
 */
public class FormParameter implements Serializable {

    private String _name;
    private String _dataType;
    private String _value;
    private boolean _inputOnly;
    private boolean _required;
    private Map<String, String> _attributes;


    public FormParameter(String name, String dataType, Map<String, String> attributes) {
        _name = name;
        _dataType = dataType;
        _attributes = attributes;
    }


    public FormParameter(YParameter parameter) {
        _name = parameter.getName();
        _dataType = parameter.getDataTypeName();
        _attributes = parameter.getAttributes();
    }


    public String getValue() { return _value; }

    public void setValue(String value) { _value = value; }

    public boolean isInputOnly() { return _inputOnly; }

    public void setInputOnly(boolean inputOnly) { _inputOnly = inputOnly; }

    public boolean isRequired() { return _required; }

    public void setRequired(boolean required) { _required = required; }

    public boolean isReadOnly() {
        if (_attributes != null) {
            String value = _attributes.get("readOnly");
            return value != null && value.equalsIgnoreCase("true");
        }
        return false;
    }


    public String getName() { return _name; }

    public String getDataTypeName() { return _dataType; }

    public Map<String, String> getAttributes() { return _attributes; }
}
