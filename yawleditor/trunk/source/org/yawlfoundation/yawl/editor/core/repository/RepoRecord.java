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

package org.yawlfoundation.yawl.editor.core.repository;

import org.yawlfoundation.yawl.util.StringUtil;
import org.yawlfoundation.yawl.util.XNode;
import org.yawlfoundation.yawl.util.XNodeParser;

/**
 * Represents a single record in the repository
 * @author Michael Adams
 * @date 17/06/12
 */
public class RepoRecord {

    private RepoDescriptor descriptor;
    private String value;


    /**
     * Constructs a new RepoRecord
     * @param name a name to reference the record
     * @param description a description of the record
     * @param value the record's value
     */
    protected RepoRecord(String name, String description, String value) {
        descriptor = new RepoDescriptor(name, description);
        this.value = value;
    }


    /**
     * Constructs a new RepoRecord
     * @param node an XNode containing the record's name, description and value
     */
    protected RepoRecord(XNode node) {
        fromXNode(node);
    }


    protected RepoDescriptor getDescriptor() {
        return descriptor;
    }

    protected String getName() {
        return descriptor.getName();
    }

    protected void setName(String name) {
        descriptor.setName(name);
    }

    protected String getDescription() {
        return descriptor.getDescription();
    }

    protected void setDescription(String description) {
        descriptor.setDescription(description);
    }

    protected String getValue() {
        return value;
    }

    protected void setValue(String value) {
        this.value = value;
    }


    /**
     * Creates an XNode representation of this record
     * @return the populated XNode
     */
    protected XNode toXNode() {
        XNode node = new XNode("record");
        node.addChild("name", getName());
        node.addChild("description", getDescription());
        XNode valueNode = node.addChild("value");
        valueNode.addContent(value);
        return node;
    }


    /**
     * Creates an XML representation of this record
     * @return the populated XML String
     */
    protected String toXML() {
        return toXNode().toString();
    }


    /**
     * Populate this record with values in an XNode
     * @param node the populated XNode
     */
    protected void fromXNode(XNode node) {
        descriptor = new RepoDescriptor(node.getChildText("name"),
                node.getChildText("description"));
        setValue(StringUtil.unwrap(node.getChild("value").toString()));
    }


    /**
     * Populate this record with values in an XML String
     * @param xml the populated XML String
     */
    protected void fromXML(String xml) {
        XNode node = new XNodeParser().parse(xml);
        if (node != null) fromXNode(node);
    }
}
