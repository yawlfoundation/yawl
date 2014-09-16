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

/**
 * A convenience class for transporting sets of name-description pairs for items in
 * the repository.
 *
 * @author Michael Adams
 * @date 17/06/12
 */
public class RepoDescriptor implements Comparable<RepoDescriptor> {

    private String name;
    private String description;


    public RepoDescriptor(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int compareTo(RepoDescriptor other) {
        if (this == other) return 0;
        if (other == null) return 1;                 // null < !null
        if (name != null) {
            if (other.name == null) return 1;
            int result = name.compareTo(other.name);
            if (result == 0) {
                if (description != null) {
                    if (other.description == null) return 1;
                    return description.compareTo(other.description);
                }
                return other.description == null ? 0 : -1;   // this.description == null
            }
        }
        return other.name == null ? 0 : -1;                  // this.name == null
    }

}
