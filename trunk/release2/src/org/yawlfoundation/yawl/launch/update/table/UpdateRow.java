package org.yawlfoundation.yawl.launch.update.table;

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

    public boolean hasNewVersion() { return ! _latestBuild.equals(_currentBuild); }

    public boolean isRemoving() { return _isInstalled && ! _installAction; }

    public boolean isAdding() { return ! _isInstalled && _installAction; }

    public boolean isInstalled() { return _isInstalled; }

    public boolean getInstallAction() { return _installAction; }

    public void setInstallAction(boolean action) { _installAction = action; }

    public boolean isInstallable() { return _installable; }

    public void setInstallable(boolean installable) { _installable = installable ; }

}
