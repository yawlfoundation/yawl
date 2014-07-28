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

import org.yawlfoundation.yawl.elements.YAttributeMap;
import org.yawlfoundation.yawl.util.XNode;
import org.yawlfoundation.yawl.util.XNodeParser;

import java.util.List;

/**
 * @author Michael Adams
 * @date 6/08/13
 */
public class ExtendedAttributesRepository extends RepoMap {

    protected ExtendedAttributesRepository() {
        super("extendedAttributes");
    }

    protected ExtendedAttributesRepository(String baseDir) {
        super(baseDir, "extendedAttributes");
    }

    /**
     * Adds a set of extended attributes and their values to the repository
     * @param name a reference name for the net decomposition
     * @param description a description of it
     * @param attributes the set of attributes to store in the repository
     * @return whether the add was successful
     */
    public String add(String name, String description, YAttributeMap attributes) {
        if (anyAreNull(name, description, attributes)) return null;
        XNode valueNode = new XNode("attributes");
        valueNode.addContent(attributes.toXMLElements());
        RepoRecord record = addRecord(
                new RepoRecord(name, description, valueNode.toString()));
        return record != null ? record.getName() : null;
    }


    /**
     * Gets a set of extended variables from the repository
     * @param name a reference name for the extended variables
     * @return the referenced set of extended variables, or null if not found
     */
    public YAttributeMap get(String name) {
        RepoRecord record = getRecord(name);
        return record != null ? parse(record.getValue()) : null;
    }


    /**
     * Removes a set of extended variables from the repository
     * @param name a reference name for the extended variables
     * @return whether the removal was successful
     */
    public YAttributeMap remove(String name) {
        RepoRecord record = removeRecord(name);
        return record != null ? parse(record.getValue()) : null;
    }


    /**
     * Gets a sorted list of descriptors for all extended variable sets
     * @return A sorted list of RepoDescriptors (String pairs - name, description)
     */
    public List<RepoDescriptor> getDescriptors() {
        return super.getDescriptors();
    }


    /**
     * Creates a map of extended attributes from its XML description
     * @param xml the XML to parse
     * @return the populated extended attribute map
     */
    private YAttributeMap parse(String xml) {
        YAttributeMap map = new YAttributeMap();
        if (xml != null) {
            XNode node = new XNodeParser().parse(xml);
            if (node != null) {
                for (XNode attNode : node.getChildren()) {
                    map.put(attNode.getName(), attNode.getText());
                }
            }
        }
        return map;
    }


}
