package org.yawlfoundation.yawl.editor.core.util;

/**
 * Options for what else to do when saving a specification to file, besides the
 * actual save
 *
 * @author Michael Adams
 * @date 5/09/11
 */
public class FileSaveOptions {

    // increment the minor version number for existing specifications
    private boolean _autoIncVersion;

    // copy the existing specification to a .bak file before save
    private boolean _backup;

    // copy the existing specification to a [file name][version].yawl file before save
    private boolean _versioning;

    // verify the specification on save
    private boolean _verify;

    // the options as bitmasks
    public static final int AUTO_INC_VERSION = 1;
    public static final int CREATE_BACKUP = 2;
    public static final int CREATE_PREV_VERSION = 4;
    public static final int VERIFY = 8;


    public FileSaveOptions() {
        this(false, false, false, false);
    }


    public FileSaveOptions(boolean autoIncVersion, boolean backup,
                           boolean versioning, boolean verify) {
        setAutoIncVersion(autoIncVersion);
        setBackupOnSave(backup);
        setVersioningOnSave(versioning);
        setVerifyOnSave(verify);
    }


    public FileSaveOptions(int options) {
        setAutoIncVersion(unmask(AUTO_INC_VERSION, options));
        setBackupOnSave(unmask(CREATE_BACKUP, options));
        setVersioningOnSave(unmask(CREATE_PREV_VERSION, options));
        setVerifyOnSave(unmask(VERIFY, options));
    }


    public boolean autoIncVersion() { return _autoIncVersion; }

    public void setAutoIncVersion(boolean autoInc) { _autoIncVersion = autoInc; }


    public boolean backupOnSave() { return _backup; }

    public void setBackupOnSave(boolean backup) { _backup = backup; }


    public boolean versioningOnSave() { return _versioning; }

    public void setVersioningOnSave(boolean versioning) { _versioning = versioning; }


    public boolean verifyOnSave() { return _verify; }

    public void setVerifyOnSave(boolean verify) { _verify = verify; }


    private boolean unmask(int mask, int options) {
        return (mask & options) == mask;
    }

}
