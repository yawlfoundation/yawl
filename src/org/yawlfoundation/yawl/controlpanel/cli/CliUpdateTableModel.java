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

package org.yawlfoundation.yawl.controlpanel.cli;

import org.yawlfoundation.yawl.controlpanel.update.Differ;
import org.yawlfoundation.yawl.controlpanel.update.table.UpdateRow;
import org.yawlfoundation.yawl.controlpanel.update.table.UpdateTableModel;
import org.yawlfoundation.yawl.util.StringUtil;

/**
 * @author Joerg Evermann
 * @author Michael Adams
 * @date 3/11/2015
 */
public class CliUpdateTableModel extends UpdateTableModel {

    public CliUpdateTableModel(Differ differ) { super(differ); }


    public void print() {
        System.out.println();
        String fmt = "%-20.20s %-45.45s %-8.8s %-8.8s %-1.1s\n";
   		System.out.format(fmt, "Name", "Description", "Current", "Latest", " ");
        System.out.format(fmt, "----", "-----------", "-------", "------", " ");
   		for (UpdateRow r : getRows()) {
   			System.out.format(fmt, r.getName(), r.getDescription(),
                    r.getCurrentBuild(), r.getLatestBuild(), getUpdatedMarker(r));
   		}
        System.out.println();
    }


    private String getUpdatedMarker(UpdateRow row) {
        String current = row.getCurrentBuild();
        return !(StringUtil.isNullOrEmpty(current) || current.equals(row.getLatestBuild())) ?
                "*" : "";
    }
}
