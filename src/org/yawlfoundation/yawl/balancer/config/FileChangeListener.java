package org.yawlfoundation.yawl.balancer.config;

import org.apache.commons.vfs2.FileChangeEvent;
import org.apache.commons.vfs2.FileListener;

/**
 * @author Michael Adams
 * @date 12/9/17
 */
public class FileChangeListener implements FileListener {

    @Override
    public void fileCreated(FileChangeEvent fileChangeEvent) throws Exception { }

    @Override
    public void fileDeleted(FileChangeEvent fileChangeEvent) throws Exception { }

    @Override
    public void fileChanged(FileChangeEvent fileChangeEvent) throws Exception {
        Config.reload();
    }
}
