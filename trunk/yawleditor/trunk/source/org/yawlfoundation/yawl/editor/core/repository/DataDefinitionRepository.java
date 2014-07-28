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

import java.util.List;

/**
 * @author Michael Adams
 * @date 6/08/13
 */
public class DataDefinitionRepository extends RepoMap {

    protected DataDefinitionRepository() {
        super("dataDefinitions");
    }

    protected DataDefinitionRepository(String baseDir) {
        super(baseDir, "dataDefinitions");
    }

    /**
     * Adds a data definition to the repository
     * @param name a reference name for the data definition
     * @param description a description of it
     * @param xml the data definition XML to store in the repository
     * @return whether the add was successful
     */
    public String add(String name, String description, String xml) {
        if (! anyAreNull(name, description, xml)) {
            RepoRecord record = addRecord(new RepoRecord(name, description, xml));
            if (record != null) return record.getName();
        }
        return null;
    }


    /**
     * Gets a data definition from the repository
     * @param name a reference name for the data definition
     * @return the referenced data definition, or null if not found
     */
    public String get(String name) {
        RepoRecord record = getRecord(name);
        return record != null ? record.getValue() : null;
    }


    /**
     * Removes a data definition from the repository
     * @param name a reference name for the data definition
     * @return whether the removal was successful
     */
   public String remove(String name) {
        RepoRecord record = removeRecord(name);
        return record != null ? record.getValue() : null;
   }


    /**
     * Gets a sorted list of descriptors for all extended variable sets
     * @return A sorted list of RepoDescriptors (String pairs - name, description)
     */
    public List<RepoDescriptor> getDescriptors() {
        return super.getDescriptors();
    }

}
