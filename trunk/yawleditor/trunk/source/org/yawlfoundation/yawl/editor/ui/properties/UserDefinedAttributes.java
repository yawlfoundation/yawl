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

package org.yawlfoundation.yawl.editor.ui.properties;

import org.yawlfoundation.yawl.editor.ui.YAWLEditor;
import org.yawlfoundation.yawl.util.StringUtil;

import javax.swing.*;
import java.io.*;
import java.util.*;

/**
 * A class to load/save user defined attributes from/to disk
 *
 * @author Michael Adams
 * @date 25/07/13
 */
public abstract class UserDefinedAttributes {

    private Map<String, String> _map;                 // attribute name, type

    public static final java.util.List<String> VALID_TYPE_NAMES = Arrays.asList(
            "string", "boolean", "integer", "double", "font", "color", "xquery");


    UserDefinedAttributes() { }



    protected abstract String getFilePath();


    public boolean add(String name, String type) throws IllegalArgumentException {
        if (validateType(type)) {
            Map<String, String> map = getMap();
            if (map != null) {
                map.put(name, type);
                return save(getMap(), getFilePath());
            }
            return false;
        }
        else throw new IllegalArgumentException("Invalid type name: " + type);
    }


    public boolean remove(String name) {
        Map<String, String> map = getMap();
        if (map != null) {
            String type = map.remove(name);
            if (type != null) {
                return save(getMap(), getFilePath());
            }
        }
        return false;
    }


    public String getType(String name) {
        Map<String, String> map = getMap();
        return map != null ? map.get(name) : null;
    }


    public Set<String> getNames() {
        Map<String, String> map = getMap();
        return map != null ? map.keySet() : null;
    }


    protected Map<String, String> getMap() {
        if (_map == null) {
            _map = load(getFilePath());
        }
        return _map;
    }


    protected Map<String, String> load(String path) {
        Map<String, String> map = new Hashtable<String, String>();
        java.util.List<String> errors = new ArrayList<String>();
        try {
            InputStream in = new FileInputStream(path);
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line = reader.readLine();
            while (line != null) {
                processLine(map, line, errors);
                line = reader.readLine();
            }
        }
        catch (IOException ioe) {
           // no UDA file
        }
        if (! errors.isEmpty()) {
            JOptionPane.showMessageDialog(YAWLEditor.getInstance(),
                    coalesceErrorMessages(errors),
                    "Problems Loading User-Defined Attributes",
                    JOptionPane.WARNING_MESSAGE);

        }
        return map;
    }


    protected boolean save(Map<String, String> map, String path) {
        StringBuilder s = new StringBuilder();
        s.append('#').append(new Date(System.currentTimeMillis()).toString());
        s.append('\n');
        for (String key : map.keySet()) {
            s.append(key).append('=').append(map.get(key)).append('\n');
        }
        s.append('\n');
        StringUtil.stringToFile(path, s.toString());
        return true;
    }


    private boolean isEnumeration(String type) {
        return type.startsWith("enumeration{");
    }

    private boolean isDynamic(String type) {
        return type.startsWith("dynamic{");
    }


    private void processLine(Map<String, String> map, String rawLine,
                             java.util.List<String> errors) {
        int startingErrorCount = errors.size();
        rawLine = rawLine.trim();

        // ignore comments and empty lines
        if ((rawLine.length() == 0) || rawLine.startsWith("#")) return;

        int equalsPos = rawLine.indexOf('=');
        if (equalsPos > -1) {
            String name = rawLine.substring(0, equalsPos).trim();
            String type = rawLine.substring(equalsPos + 1).trim();
            if (StringUtil.isNullOrEmpty(name)) {
                errors.add("Malformed entry, no attribute name found on line: " + rawLine);
            }
            else if (StringUtil.isNullOrEmpty(type)) {
                errors.add("Malformed entry, no attribute type found on line: " + rawLine);
            }
            else if (! validateType(type)) {
                errors.add("Invalid type: '" + type + "' found on line: " + rawLine);
            }
            else if (isEnumeration(type)) {
                validateEnumeration(rawLine, type, errors);
            }
            else if (isDynamic(type)) {
                validateDynamic(rawLine, type, errors);
            }

            if (errors.size() == startingErrorCount) {
                map.put(name, type);            // all good
            }
        }
        else errors.add("Malformed entry, no '=' found on line: " + rawLine);
    }


    protected boolean validateType(String type) {
        return VALID_TYPE_NAMES.contains(type) || isEnumeration(type) || isDynamic(type);
    }

    private String coalesceErrorMessages(java.util.List<String> messages) {
        StringBuilder s = new StringBuilder();
        for (String text : messages) {
            s.append(text);
            if (! text.startsWith("Error")) s.append(". Line ignored.");
            s.append('\n');
        }
        return s.toString();
    }


    private void validateEnumeration(String rawLine, String type,
                                        java.util.List<String> errors) {
        int lastBracePos = type.indexOf('}');
        if (lastBracePos == -1) {
            errors.add("Malformed enumeration entry, missing closing brace '}'" +
                    " found on line: " + rawLine);
        }
        String[] entries = type.split(",");
        if (entries.length < 2) {
            errors.add("Malformed enumeration entry, at least two comma-separated" +
                   " values required, found on line: " + rawLine);
        }
    }


    private void validateDynamic(String rawLine, String type,
                                        java.util.List<String> errors) {
        int lastBracePos = type.indexOf('}');
        if (lastBracePos == -1) {
            errors.add("Malformed dynamic attribute entry, missing closing brace '}'" +
                    " found on line: " + rawLine);
        }
    }

}
