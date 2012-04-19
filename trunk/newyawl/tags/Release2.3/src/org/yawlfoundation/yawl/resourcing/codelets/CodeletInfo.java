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

package org.yawlfoundation.yawl.resourcing.codelets;

import org.jdom.Element;
import org.yawlfoundation.yawl.elements.data.YParameter;
import org.yawlfoundation.yawl.util.XNode;
import org.yawlfoundation.yawl.util.XNodeParser;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores some information about a codelet class for transporting across the API.
 * 
 * @author Michael Adams
 * @date 7/09/2010
 */
public class CodeletInfo {

    private String _name;
    private String _description;
    private String _canonicalName;
    private List<YParameter> _requiredParams;

    public CodeletInfo() { }

    public CodeletInfo(String name, String desc) {
        _name = name;
        _description = desc;
    }

    public CodeletInfo(String name, String desc, List<YParameter> p) {
        this(name, desc);
        _requiredParams = p;
    }


    public CodeletInfo(String xml) {
        XNode node = new XNodeParser().parse(xml);
        fromXML(node);
    }

    public CodeletInfo(Element xml) {
        XNode node = new XNodeParser().parse(xml);
        fromXML(node);
    }


    public String getName() {
        return _name;
    }

    public void setName(String name) {
        _name = name;
    }

    public String getCanonicalName() {
        return _canonicalName;
    }

    public void setCanonicalName(String name) {
        _canonicalName = name;
    }

    public String getDescription() {
        return _description;
    }

    public void setDescription(String description) {
        _description = description;
    }

    public List<YParameter> getRequiredParams() {
        return _requiredParams;
    }

    public void setRequiredParams(List<YParameter> requiredParams) {
        _requiredParams = requiredParams;
    }

    public void fromXML(XNode node) {
        if (node != null) {
            _name = node.getChildText("name");
            _description = node.getChildText("description", true);
            _canonicalName = node.getChildText("canonicalname");
            XNode params = node.getChild("requiredparams");
            if (params != null) _requiredParams = getRequiredParametersFromXML(params);
        }
    }

    public List<YParameter> getRequiredParametersFromXML(String xml) {
        XNode node = new XNodeParser().parse(xml);
        return getRequiredParametersFromXML(node);
    }


    public List<YParameter> getRequiredParametersFromXML(XNode node) {
        List<YParameter> params = new ArrayList<YParameter>();
        if (node != null) {
            for (XNode pNode : node.getChildren()) {
                YParameter param = new YParameter(null, pNode.getName());
                param.setDataTypeAndName(pNode.getChildText("datatype"),
                        pNode.getChildText("name"), pNode.getChildText("namespace"));
                param.setDocumentation(pNode.getChildText("documentation"));
                params.add(param);
            }
        }
        return params;
    }


    public String toString() {
        return "Codelet: " + _name;
    }

}
