package org.yawlfoundation.yawl.editor.api.util;

/**
 * @author Michael Adams
 * @date 5/09/11
 */
public class FileSaveOptions {

    private boolean _autoIncVersion;
    private boolean _backupOnSave;
    private boolean _versioningOnSave;
    private boolean _verifyOnSave;
    private boolean _analyseOnSave;

    public FileSaveOptions() {
        setAnalyseOnSave(false);
        setAutoIncVersion(false);
        setBackupOnSave(false);
        setVerifyOnSave(false);
        setVersioningOnSave(false);
    }


    public boolean autoIncVersion() { return _autoIncVersion; }

    public void setAutoIncVersion(boolean autoInc) { _autoIncVersion = autoInc; }


    public boolean backupOnSave() { return _backupOnSave; }

    public void setBackupOnSave(boolean backup) { _backupOnSave = backup; }


    public boolean versioningOnSave() { return _versioningOnSave; }

    public void setVersioningOnSave(boolean versioning) { _versioningOnSave = versioning; }


    public boolean verifyOnSave() { return _verifyOnSave; }

    public void setVerifyOnSave(boolean verify) { _verifyOnSave = verify; }


    public boolean analyseOnSave() { return _analyseOnSave; }

    public void setAnalyseOnSave(boolean analyse) { _analyseOnSave = analyse; }

}
