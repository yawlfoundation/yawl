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

package org.yawlfoundation.yawl.resourcing;

import org.jdom2.Element;
import org.yawlfoundation.yawl.util.JDOMUtil;

import java.util.*;

/**
 * The base class inherited by all of the 'selector' classes :- filters, constraints
 * and allocators.
 * <p/>
 * As well as giving an inherited class a meaningful class name, extending classes need
 * to provide values for:
 * _name --> the class name
 * _displayName --> a 'pretty' name to show to designers/users when required
 * _description --> a user-oriented description of what the extending class does
 * _params --> a set of parameters needed when applying the extending class's
 * 'perform...' method  (the param value should be set to null when
 * initialised).
 * <p/>
 * Create Date: 03/08/2007. Last Date: 09/11/2007.
 *
 * @author Michael Adams (BPM Group, QUT Australia)
 * @version 2.0
 */

public abstract class AbstractSelector implements Comparable<AbstractSelector> {

    protected String _name;                       // the simple class name for this selector
    protected String _canonicalName;              // the full class name for this selector
    protected String _displayName;                // a 'user-friendly' name
    protected String _description;                // what does it do?
    protected Map<String, String> _params =
            new Hashtable<String, String>();    // params used by the 'selection'

    /**
     * ****************************************************************************
     */

    public AbstractSelector() {}                   // constructor for reflection


    public AbstractSelector(String name) { _name = name; }


    public AbstractSelector(String name, String desc) {
        _name = name;
        _description = desc;
    }


    public AbstractSelector(String name, Map<String, String> params) {
        _name = name;
        _params = params;
    }


    public AbstractSelector(String name, String desc, Map<String, String> params) {
        this(name, desc);
        _params = params;
    }


    /********************************************************************************/

    // GETTERS & SETTERS //

    /**
     * @return a Set of parameter names needing values to be used in the
     *         performance of the selection
     */
    protected Set<String> getParamKeys() { return _params.keySet(); }

    /**
     * @return the name of this selector
     */
    public String getName() { return _name; }

    /**
     * @return the display name of this selector
     */
    public String getDisplayName() { return _displayName; }

    /**
     * @return how this selector is described
     */
    public String getDescription() { return _description; }

    /**
     * @return a Set of keys (ie. attribute names) for the expected params
     */
    public Set<String> getKeys() { return _params.keySet(); }

    /**
     * @return a HashMap of parameters of the form [name, value]
     */
    public Map<String, String> getParams() { return _params; }

    /**
     * @return the name of this selector class
     */
    public String getClassName() { return this.getClass().getSimpleName(); }

    /**
     * @return the full name of this selector class
     */
    public String getCanonicalName() {
        if (_canonicalName != null) return _canonicalName;
        else return this.getClass().getCanonicalName();
    }

    /**
     * Retrieves the value of the specified parameter
     *
     * @param key the name of the parameter
     * @return the specified parameter's value
     */
    public String getParamValue(String key) { return _params.get(key); }


    /**
     * Stores the class name of this 'selector'
     *
     * @param name the name to set
     */
    public void setName(String name) { _name = name; }

    /**
     * Stores the full class name of this 'selector'
     *
     * @param name the name to set
     */
    public void setCanonicalName(String name) { _canonicalName = name; }

    /**
     * Sets the user-friendly display name of this 'selector'
     *
     * @param name the name to set
     */
    public void setDisplayName(String name) { _displayName = name; }

    /**
     * Sets the description of this 'selector'
     *
     * @param desc the description value to set
     */
    public void setDescription(String desc) { _description = desc; }

    /**
     * Sets (replaces) the parameters with the map passed
     *
     * @param paramsMap the new parameter map of the form [name, value] (both Strings)
     */
    public void setParams(Map<String, String> paramsMap) {
        _params = paramsMap;
    }

    /**
     * Adds (does not replace) the parameters in the map passed to the selectors
     * parameters
     *
     * @param paramMap the new parameter map of the form [name, value] (both Strings)
     */
    public void addParams(Map<String, String> paramMap) {
        _params.putAll(paramMap);
    }

    /**
     * Adds a single parameter passed to the selector's parameters
     *
     * @param key   the name of the parameter
     * @param value the value of the parameter
     */
    public void addParam(String key, String value) { _params.put(key, value); }

    /**
     * Adds a key - ie. an attribute which needs to be assigned a value at design time
     *
     * @param key the attribute name
     */
    public void addKey(String key) { addParam(key, ""); }

    /**
     * Sets the value of a key (at specification design time)
     *
     * @param key   the attribute name
     * @param value the value to set
     */
    public void setKeyValue(String key, String value) { addParam(key, value); }

    /**
     * AbstractSelectors are considered equal if their canonical names and name fields
     * are equal
     *
     * @param other the object to compare to this
     * @return true if equal
     */
    public boolean equals(Object other) {
        return (other instanceof AbstractSelector) &&
                ((AbstractSelector) other).getCanonicalName().equals(getCanonicalName()) &&
                ((AbstractSelector) other).getName().equals(getName());
    }


    public int hashCode() {
        return getCanonicalName().hashCode();
    }


    public int compareTo(AbstractSelector other) {
        return _name != null ? _name.compareTo(other.getName()) : 1;
    }


    /**
     * Evaluates a list of Sets against an expression. Sets may be unioned (when the
     * expression operator is '|') or intersected (when '&'). Operator precedence is
     * strictly left to right. The number of operators in the expression should be
     * one less than the number of Sets in the List
     *
     * @param setList    the list of sets
     * @param expression the expression containing the operators
     * @param <T>        the object type contained in each set
     * @return the resultant final set
     */
    protected <T> Set<T> evaluate(List<Set<T>> setList, String expression) {
        if (setList == null || setList.isEmpty()) return Collections.emptySet();
        if (setList.size() > 1) {
            for (char c : expression.toCharArray()) {
                if (c == '&') {
                    setList.set(0, intersection(setList.get(0), setList.get(1)));
                    setList.remove(1);
                } else if (c == '|') {
                    setList.set(0, union(setList.get(0), setList.get(1)));
                    setList.remove(1);
                }
                if (setList.size() == 1) break;   // no more operators
            }
        }
        return setList.get(0);
    }


    /**
     * Performs an intersection over two Sets
     *
     * @param set1 Set A
     * @param set2 Set B
     * @param <T>  the object type contained in each set
     * @return A Set containing only those members that are present in both A and B
     */
    protected <T> Set<T> intersection(Set<T> set1, Set<T> set2) {
        Set<T> intersectedSet = new HashSet<T>();
        for (T t : set1) if (set2.contains(t)) intersectedSet.add(t);
        return intersectedSet;
    }


    /**
     * Performs an union over two Sets
     *
     * @param set1 Set A
     * @param set2 Set B
     * @param <T>  the object type contained in each set
     * @return A Set containing all the members of A and all the members of B
     */
    protected <T> Set<T> union(Set<T> set1, Set<T> set2) {
        Set<T> unionedSet = new HashSet<T>();
        unionedSet.addAll(set1);
        unionedSet.addAll(set2);
        return unionedSet;
    }


    public String toString() { return getClass().getCanonicalName() + ": " + _name; }

    /*******************************************************************************/

    // SPECIFICATION XML METHODS //

    /**
     * @return an xml representation of this object's parameters (if any). Used to
     *         build the specification xml
     */
    protected String toXML() {
        StringBuilder xml = new StringBuilder();
        String name = isExternalClass() ? getCanonicalName() : getName();
        xml.append("<name>").append(name).append("</name>");
        if ((_params != null) && (!_params.isEmpty())) {
            xml.append("<params>");

            // write the key and value for each parameter
            for (String key : _params.keySet()) {
                xml.append("<param>");
                xml.append("<key>").append(key).append("</key>");
                xml.append("<value>")
                        .append(JDOMUtil.encodeEscapes(_params.get(key))).append("</value>");
                xml.append("</param>");
            }
            xml.append("</params>");
        }
        return xml.toString();
    }

    private boolean isExternalClass() {
        String canonicalName = getCanonicalName();
        return (canonicalName != null) && (!canonicalName.startsWith("org.yawlfoundation"));
    }

    /**
     * Unpacks the xml describing the parameters to a HashMap object
     *
     * @param eParams
     * @return a [key, value] map of the parameters described by the {@code Element}
     */
    protected static Map<String, String> unmarshalParams(Element eParams) {
        if (eParams != null) {
            HashMap<String, String> result = new HashMap<String, String>();
            for (Element param : eParams.getChildren())
                result.put(param.getChildText("key"), param.getChildText("value"));
            return result.isEmpty() ? null : result;
        }
        return null;
    }

    /*******************************************************************************/

    /**
     * Gets a 'dump' of this selector object as an XML'd String
     *
     * @param outerTag a value for the surrounding tag (one of the extended classes)
     * @return an XML String describing this object
     * @see #reconstitute(Element)
     */
    protected String getInformation(String outerTag) {
        StringBuilder xml = new StringBuilder();
        xml.append("<").append(outerTag).append(">");

        xml.append("<name>");
        if (_name != null) xml.append(_name);
        xml.append("</name>");

        xml.append("<canonicalname>");
        xml.append(getClass().getCanonicalName());
        xml.append("</canonicalname>");

        xml.append("<displayname>");
        if (_displayName != null) xml.append(_displayName);
        xml.append("</displayname>");

        xml.append("<description>");
        if (_description != null) xml.append(_description);
        xml.append("</description>");

        xml.append("<keys>");
        for (String key : _params.keySet())
            xml.append("<key>").append(key).append("</key>");
        xml.append("</keys>");

        xml.append("</").append(outerTag).append(">");
        return xml.toString();
    }

    /**
     * Fills the members of this object with values found in an XML description
     *
     * @param e a JDOM Element containing the values
     * @see #getInformation(String)
     */
    public void reconstitute(Element e) {
        setName(e.getChildText("name"));
        setCanonicalName(e.getChildText("canonicalname"));
        setDisplayName(e.getChildText("displayname"));
        setDescription(e.getChildText("description"));
        List<Element> keys = e.getChild("keys").getChildren();
        if (keys != null) {
            for (Element key : keys) addParam(key.getText(), "");
        }
    }

    /*******************************************************************************/
}
