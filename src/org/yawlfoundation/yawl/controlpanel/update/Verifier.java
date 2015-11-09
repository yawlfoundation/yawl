package org.yawlfoundation.yawl.controlpanel.update;

import org.yawlfoundation.yawl.controlpanel.util.FileUtil;
import org.yawlfoundation.yawl.util.CheckSummer;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * @author Michael Adams
 * @date 23/08/2014
 */
public class Verifier extends SwingWorker<Boolean, Void> {

    private Map<String, String> _md5Map;


    public Verifier(Map<String, String> md5Map) {
        _md5Map = md5Map;
    }


    @Override
    protected Boolean doInBackground() throws Exception {
        return verify();
    }


    public boolean verify() {
        if (_md5Map == null) return false;
        CheckSummer summer = new CheckSummer();
        File dir = FileUtil.getTmpDir();
        for (String key : _md5Map.keySet()) {
            String fileName = dir.getAbsolutePath() + File.separator + key;
            try {
                if (! summer.compare(fileName, _md5Map.get(key))) {
                    return false;
                }
            }
            catch (IOException ioe) {
                return false;
            }
        }
        return true;
    }

}
