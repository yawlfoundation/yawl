/*
 * Copyright (c) 2004-2020 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.cost.data;

import org.yawlfoundation.yawl.util.XNode;
import org.yawlfoundation.yawl.util.XNodeIO;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Michael Adams
 * @date 3/10/11
 */
public class CostFunction implements XNodeIO {

    private long functionID;                    // hibernate primary key
    private String name;
    private String description;
    private String expression;
    private String returnType;
    List<FunctionParameter> parameters;


    public CostFunction() {
        parameters = new ArrayList<FunctionParameter>();
    }

    public CostFunction(XNode node) {
        this();
        fromXNode(node);
    }

    private  long getFunctionID() { return functionID; }

    private  void setFunctionID(long id) { functionID = id; }

    public String toXML() {
        return toXNode().toPrettyString();
    }


    public void fromXNode(XNode node) {
        name = node.getChildText("name");
        description = node.getChildText("description");
        expression = node.getChildText("expression");
        returnType = node.getChildText("returnType");
        for (XNode pNode : node.getChild("parameters").getChildren(XNode.ContentType.text)) {
             parameters.add(new FunctionParameter(pNode.getAttributeValue("key"),
                     pNode.getAttributeValue("type")));
        }
    }


    public XNode toXNode() {
        XNode node = new XNode("function");
        node.addChild("name", name);
        node.addChild("description", description);
        node.addChild("expression", expression);
        node.addChild("returnType", returnType);
        XNode pNode = node.addChild("parameters");
        for (FunctionParameter p : parameters) {
            XNode item = pNode.addChild("parameter");
            item.addAttribute("key", p.key);
            item.addAttribute("type", p.type);           
        }
        return node;
    }

    public XNodeIO newInstance(XNode node) {
        return new CostFunction(node);
    }
}
