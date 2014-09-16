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

package org.yawlfoundation.yawl.editor.ui.specification.io;

import org.yawlfoundation.yawl.editor.core.repository.RepoMap;
import org.yawlfoundation.yawl.editor.core.repository.RepoRecord;
import org.yawlfoundation.yawl.editor.ui.util.FileLocations;
import org.yawlfoundation.yawl.engine.YSpecificationID;

/**
 * @author Michael Adams
 * @date 18/10/13
 */
public class LayoutRepository extends RepoMap {

    private static final LayoutRepository INSTANCE = new LayoutRepository();


    private LayoutRepository() { super(FileLocations.getRepositoryPath(), "layouts"); }


    public static LayoutRepository getInstance() { return INSTANCE; }


    /**
     * Adds a layout for a specification to the repository
     * @param specID the specification identifier for a layout
     * @param layoutXML the layout XML to store in the repository
     * @return whether the add was successful
     */
    public String add(YSpecificationID specID, String layoutXML) {
        if (specID != null && layoutXML != null && ! hasRecord(specID.toKeyString())) {
            RepoRecord record = addRecord(new LayoutRecord(specID, layoutXML));
            if (record != null) return record.getName();
        }
        return null;
    }


    /**
     * Gets a data definition from the repository
     * @param specID the specification identifier for a layout
     * @return the referenced data definition, or null if not found
     */
    public String get(YSpecificationID specID) {
        RepoRecord record = getRecord(specID.toKeyString());
        return record != null ? record.getValue() : null;
    }


    /**
     * Removes a data definition from the repository
     * @param specID the specification identifier for a layout
     * @return whether the removal was successful
     */
    public String remove(YSpecificationID specID) {
        RepoRecord record = removeRecord(specID.toKeyString());
        return record != null ? record.getValue() : null;
    }


    /******************************************************************************/

    class LayoutRecord extends RepoRecord {

        LayoutRecord(YSpecificationID specID, String layoutXML) {
            super(specID.toKeyString(), "", layoutXML);
        }
    }

}
