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

package org.yawlfoundation.yawl.controlpanel.editor;

import org.yawlfoundation.yawl.controlpanel.components.ToolBar;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Michael Adams
 * @date 14/05/15
 */
public class EditorLauncher {

    ToolBar _btnPanel;


    public EditorLauncher(ToolBar btnPanel) {
        _btnPanel = btnPanel;
    }


    public void launch() {
        try {
            List<String> command = new ArrayList<String>();
            command.add(getJavaPath());
            command.add("-Xms256m");
            command.add("-Xmx256m");
            command.add("-jar");
            command.add(getEditorPath());

            ProcessBuilder builder = new ProcessBuilder(command);
            monitor(builder.start());
        }
        catch (Exception e) {
                 JOptionPane.showMessageDialog(_btnPanel, e.getMessage(),
                         "Error Starting Editor",
                         JOptionPane.ERROR_MESSAGE);
        }
    }


    private String getEditorPath() throws URISyntaxException, IOException {
        File baseDir = getJarFile().getParentFile().getParentFile();
        File editorDir = new File(baseDir, "editor");
        File[] fileList = editorDir.listFiles();
        if (fileList == null) throw new IOException("Editor jar not found.");
        for (File f : fileList) {
            if (f.isFile() && f.getName().startsWith("YAWLEditor") &&
                    f.getName().endsWith(".jar")) {
                return f.getAbsolutePath();
            }
        }
        throw new IOException("Editor jar not found.");
    }


    private File getJarFile() throws URISyntaxException {
        return new File(this.getClass().getProtectionDomain()
                        .getCodeSource().getLocation().toURI());
    }


    private String getJavaPath() {
        return System.getProperty("java.home") + File.separator +
                            "bin" + File.separator + "java";
    }


    private void monitor(Process proc) {
        Thread t = new Thread(new ProcessMonitor(proc));
        t.start();
    }


    /***********************************************************************/

    class ProcessMonitor implements Runnable {

        private final Process _proc;

        ProcessMonitor(Process proc) { _proc = proc; }

        public void run() {
            try {
                _proc.waitFor();
            }
            catch (Exception e) {
                //
            }
            _btnPanel.enableEditorButton(true);
        }

    }

}
