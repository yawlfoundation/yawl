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
