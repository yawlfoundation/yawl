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

package org.yawlfoundation.yawl.controlpanel.update.table;

/**
 * @author Michael Adams
 * @date 17/08/2014
 */
public class UpdateRow {

    private String _appName;
    private String _description;
    private String _currentBuild;
    private String _latestBuild;
    private boolean _isInstalled;
    private boolean _installAction;
    private boolean _installable;

    public UpdateRow(String name, String desc, String current, String latest,
                     boolean installed) {
        _appName = name;
        _description = desc;
        _currentBuild = current;
        _latestBuild = latest;
        _isInstalled = installed;
        _installAction = installed;
    }

    public String getName() { return _appName; }

    public String getDescription() { return _description; }

    public String getCurrentBuild() { return _isInstalled ? _currentBuild : ""; }

    public String getLatestBuild() { return _latestBuild; }

    public boolean hasNewVersion() {
        return ! (_latestBuild.equals("") || _latestBuild.equals(_currentBuild));
    }

    public boolean isRemoving() { return _isInstalled && ! _installAction; }

    public boolean isAdding() { return ! _isInstalled && _installAction; }

    public boolean isInstalled() { return _isInstalled; }

    public boolean getInstallAction() { return _installAction; }

    public void setInstallAction(boolean action) { _installAction = action; }

    public boolean isInstallable() { return _installable; }

    public void setInstallable(boolean installable) { _installable = installable ; }

    public boolean hasUpdates() {
        return hasNewVersion() || isRemoving() || isAdding();
    }

}
