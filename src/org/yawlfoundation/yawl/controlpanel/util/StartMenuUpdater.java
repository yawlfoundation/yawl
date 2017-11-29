/*
 * Copyright (c) 2004-2015 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.controlpanel.util;

import org.yawlfoundation.yawl.util.StringUtil;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Michael Adams
 * @date 27/03/15
 */
public class StartMenuUpdater {

    private ScriptSettings _settings;

    private static final String VB_SCRIPT_TEMPLATE =
            "Set oWS = WScript.CreateObject(\"WScript.Shell\")\r\n" +
            "smPath = oWS.SpecialFolders(\"StartMenu\")\r\n" +
            "sLinkPath = smPath & \"\\Programs\\{0}\r\n" +
            "sLinkFile = sLinkPath & \"\\{1}.lnk\"\r\n" +
            "Dim oFS: Set oFS = CreateObject(\"Scripting.FileSystemObject\")\r\n" +
            "If NOT (oFS.FolderExists(sLinkPath)) Then\r\n" +
            "oFS.CreateFolder(sLinkPath)\r\n" +
            "End If\r\n" +
            "Set oLink = oWS.CreateShortcut(sLinkFile)\r\n" +
            "oLink.TargetPath = \"{2}\r\n" +
            "oLink.Arguments = \"{3}\r\n" +
            "oLink.Description = \"{4}\r\n" +
            "oLink.IconLocation = \"{5}\r\n" +
            "oLink.WorkingDirectory = \"{6}\r\n" +
            "oLink.Save\r\n";


    public StartMenuUpdater() { _settings = new ScriptSettings(); }


    public ScriptSettings getSettings() { return _settings; }


    public boolean update() {
        String script = MessageFormat.format(VB_SCRIPT_TEMPLATE, _settings.list());
        try {
            File scriptFile = File.createTempFile("script", ".vbs");
            StringUtil.stringToFile(scriptFile, script);
            runScript(scriptFile);
            scriptFile.delete();
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }


    private void runScript(File f) throws IOException {
        List<String> cmdList = new ArrayList<String>();
        cmdList.add("cmd");
        cmdList.add("/c");
        cmdList.add("cscript");
        cmdList.add("/nologo");
        cmdList.add(f.getAbsolutePath());
        ProcessBuilder pb = new ProcessBuilder(cmdList);
        pb.start();
    }


    public void checkBatFile(File rootPath) {
        File YawlBat = new File(rootPath, "YAWL.bat");
        if (! YawlBat.exists()) {
            String java = "\"" + System.getProperty("java.home") + File.separator +
                    "bin" + File.separator + "java" + "\"";
            String content = "@echo off\r\n" +
                    "if \"\"%1\"\" == \"\"controlpanel\"\" goto cp\r\n" +
                    "if \"\"%1\"\" == \"\"cp\"\" goto cp\r\n" +
                    "if \"\"%1\"\" == \"\"editor\"\" goto editor\r\n" +
                    "goto usage\r\n\r\n" +
                    ":cp\r\n" +
                    "cd controlpanel\r\n" +
                    java + " -jar YawlControlPanel-4.2.jar\r\n" +
                    "goto end\r\n\r\n" +
                    ":editor\r\n" +
                    "cd editor\r\n" +
                    "shift\r\n" +
                    java + " -Xms256m -Xmx256m -XX:PermSize=128m " +
                    "-XX:MaxPermSize=128m -jar YAWLEditor4.0.jar \"%1\"\r\n" +
                    "goto end\r\n\r\n" +
                    ":usage\r\n" +
                    "echo USAGE: One of the following\r\n" +
                    "echo    YAWL controlpanel\r\n" +
                    "echo    YAWL editor\r\n" +
                    "\r\n" +
                    ":end\r\n";
            StringUtil.stringToFile(YawlBat, content);
        }
    }



    public class ScriptSettings {

        String folderName;
        String menuName;
        String targetPath;
        String arguments;
        String description;
        String iconLocation;
        String workingDir;

        public ScriptSettings setFolderName(String s) { folderName = s; return this; }

        public ScriptSettings setMenuName(String s) { menuName = s; return this; }

        public ScriptSettings setTargetPath(String s) { targetPath = s; return this; }

        public ScriptSettings setArguments(String s) { arguments = s; return this; }

        public ScriptSettings setDescription(String s) { description = s; return this; }

        public ScriptSettings setIconLocation(String s) { iconLocation = s; return this; }

        public ScriptSettings setWorkingDir(String s) { workingDir = s; return this; }

        String[] list() {
            String[] items = new String[7];
            items[0] = folderName;
            items[1] = menuName;
            items[2] = targetPath;
            items[3] = arguments;
            items[4] = description;
            items[5] = iconLocation;
            items[6] = workingDir;
            return items;
        }
    }


}
