/*
 * Copyright (c) 2004-2013 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.editor.ui.properties.data;

/**
 * @author Michael Adams
 * @date 14/08/12
 */
public class DefaultMapping {

    private String _containerName;
    private String _variableName;
    private String _suffix;
    private String _dataType;
    private String _customMapping;

    private static final char SEP_CHAR = '/';

    public DefaultMapping() { }

    public DefaultMapping(String mapping) {
        parse(mapping);
    }


    public DefaultMapping(String containerName, String variableName, String suffix,
                          String dataType) {
        _containerName = containerName;
        _variableName = variableName;
        _suffix = suffix;
        _dataType = dataType;
    }

    public String getContainerName() { return _containerName; }

    public void setContainerName(String containerName) { _containerName = containerName; }


    public String getVariableName() { return _variableName; }

    public void setVariableName(String variableName) { _variableName = variableName; }


    public String getSuffix() { return _suffix; }

    public void setSuffix(String suffix) { _suffix = suffix; }


    public String getDataType() { return _dataType; }

    public void setDataType(String dataType) { _dataType = dataType; }


    public String getCustomMapping() { return _customMapping; }

    public void setCustomMapping(String mapping) { _customMapping = mapping; }

    public boolean isCustomMapping() { return _customMapping != null; }


    public boolean equals(Object o) {
        return (o instanceof DefaultMapping) && toString().equals(o.toString());
    }


    public int hashCode() {
        return toString().hashCode();
    }


    public String toString() {
        if (_customMapping != null) return _customMapping;

        StringBuilder sb = new StringBuilder();
        sb.append('{').append(SEP_CHAR)
          .append(_containerName).append(SEP_CHAR)
          .append(_variableName).append(SEP_CHAR)
          .append(_suffix)
          .append('}');
        return sb.toString();
    }


    private void parse(String mapping) {

        // if there's not three separators, it's not a default mapping
        int sepCount = 0;
        for (char c : mapping.toCharArray()) {
            if (c == SEP_CHAR) sepCount++;
        }
        if (sepCount != 3) {
            _customMapping = mapping;
            return;
        }

        int index = -1;
        StringBuilder s = new StringBuilder();
        for (char c : mapping.substring(mapping.indexOf(SEP_CHAR)).toCharArray()) {
            if (Character.isWhitespace(c)) continue;
            if (c == SEP_CHAR) {
                switch (index) {
                    case 0: _containerName = s.toString(); break;
                    case 1: _variableName = s.toString(); break;
                }
                if (s.length() > 0) s.delete(0, s.length());
                index++;
            }
            else {
                s.append(c);
            }
        }
        _suffix = s.toString();
        if (_suffix.endsWith("())")) _suffix = _suffix.replace("())", "()");
    }
}
