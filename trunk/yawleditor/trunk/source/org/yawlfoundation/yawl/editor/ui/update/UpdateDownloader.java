/*
 * Copyright (c) 2004-2014 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.editor.ui.update;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Michael Adams
 * @date 27/05/2014
 */
public class UpdateDownloader extends SwingWorker<Void, Void> implements PropertyChangeListener {

    private String _urlBase;
    private String _urlSuffix;
    private List<String> _fileNames;
    private long _totalBytes;
    private File _targetDir;
    private Map<DownloadWorker, Integer> _workerMap;
    private final Object _lock = new Object();


    public UpdateDownloader(String urlBase, String urlSuffix, List<String> fileNames,
                            long totalBytes, File targetDir) {
        _urlBase = urlBase;
        _urlSuffix = urlSuffix;
        _fileNames = fileNames;
        _totalBytes = totalBytes;
        _targetDir = targetDir;
    }


    protected File getTargetDir() { return _targetDir; }


    protected boolean hasErrors() {
        for (DownloadWorker worker : _workerMap.keySet()) {
            if (worker.hasErrors()) return true;
        }
        return false;
    }


    protected List<String> getErrors() {
        List<String> errors = new ArrayList<String>();
        for (DownloadWorker worker : _workerMap.keySet()) {
            if (worker.hasErrors()) errors.add(worker.getError());
        }
        return errors;
    }


    @Override
    protected Void doInBackground() {
        _workerMap = new HashMap<DownloadWorker, Integer>();
        setProgress(0);
        for (String fileName : _fileNames) {
            DownloadWorker worker = new DownloadWorker(_urlBase, _urlSuffix,
                     fileName, _totalBytes, _targetDir);
            worker.addPropertyChangeListener(this);
            _workerMap.put(worker, 0);
            worker.execute();
        }
        while (! isComplete()) {
            pause(1000);
            if (hasErrors()) {
                cancel();
                break;
            }
        }
        return null;
    }


    public boolean cancel() {
        for (DownloadWorker worker : _workerMap.keySet()) {
             if (! worker.isDone()) worker.cancel(true);
        }
        return super.cancel(true);
    }


    public void propertyChange(PropertyChangeEvent event) {
        DownloadWorker worker = (DownloadWorker) event.getSource();
        if (event.getPropertyName().equals("progress")) {
            int progress = (Integer) event.getNewValue();
            _workerMap.put(worker, progress);
            setProgress(getTotalProgress());
        }
    }


    private boolean isComplete() {
        for (DownloadWorker worker : _workerMap.keySet()) {
            if (! worker.isDone()) return false;
        }
        return true;
    }


    private int getTotalProgress() {
        int progress = 0;
        for (Integer i : _workerMap.values()) progress += i;
        return progress;
    }

    private void pause(long milliseconds) {
        long now = System.currentTimeMillis();
        long finishTime = now + milliseconds;
        while (now < finishTime) {
            long timeToWait = finishTime - now;
            synchronized (_lock) {
                try {
                    _lock.wait(timeToWait);
                }
                catch (InterruptedException ex) {
                    // go round again
                }
            }
            now = System.currentTimeMillis();
        }
    }


}
