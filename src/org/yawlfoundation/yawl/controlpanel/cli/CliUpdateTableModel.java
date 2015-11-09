package org.yawlfoundation.yawl.controlpanel.cli;

import org.yawlfoundation.yawl.controlpanel.update.Differ;
import org.yawlfoundation.yawl.controlpanel.update.table.UpdateRow;
import org.yawlfoundation.yawl.controlpanel.update.table.UpdateTableModel;

/**
 * @author Joerg Evermann
 * @author Michael Adams
 * @date 3/11/2015
 */
public class CliUpdateTableModel extends UpdateTableModel {

    public CliUpdateTableModel(Differ differ) { super(differ); }


    public void print() {
        System.out.println();
        String fmt = "%-20.20s %-50.50s %-10.10s %-10.10s\n";
   		System.out.format(fmt, "Name", "Description", "Current", "Latest");
        System.out.format(fmt, "----", "-----------", "-------", "------");
   		for (UpdateRow r : getRows()) {
   			System.out.format(fmt, r.getName(), r.getDescription(),
                    r.getCurrentBuild(), r.getLatestBuild());
   		}
        System.out.println();
    }
}
