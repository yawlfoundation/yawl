package org.yawlfoundation.yawl.controlpanel.update.table;

import org.yawlfoundation.yawl.controlpanel.update.Differ;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author Michael Adams
 * @date 17/08/2014
 */
public class UpdateRowFactory {

    private final Differ _differ;

    public UpdateRowFactory(Differ differ) {
        _differ = differ;
    }


    public List<UpdateRow> get() {
        List<String> installedApps = _differ.getInstalledWebAppNames();
        List<UpdateRow> rows = new ArrayList<UpdateRow>();
        for (String name : _differ.getWebAppNames()) {
            AppEnum appEnum = AppEnum.fromString(name);
            if (appEnum == null) continue;
            String desc = appEnum.getDescription();
            String current = _differ.getCurrentBuild(name);
            String latest = _differ.getLatestBuild(name);
            boolean installed = installedApps.contains(name);
            UpdateRow row = new UpdateRow(name, desc, current, latest, installed);
            row.setInstallable(appEnum.isInstallable());
            rows.add(row);
        }
        Collections.sort(rows, new UpdateRowComparator());
        return rows;
    }


    class UpdateRowComparator implements Comparator<UpdateRow> {

        public int compare(UpdateRow row1, UpdateRow row2) {
            if (row1 == null) return -1;
            if (row2 == null) return 1;

            // installed apps get precedence over uninstalled ones
            boolean installed1 = row1.isInstalled();
            boolean installed2 = row2.isInstalled();
            if (installed1 && ! installed2) return -1;
            if (installed2 && ! installed1) return 1;

            // either both installed or both not installed
            int order1 = AppEnum.fromString(row1.getName()).getSortOrder();
            int order2 = AppEnum.fromString(row2.getName()).getSortOrder();
            return order1 - order2;
        }

    }

}
