package org.yawlfoundation.yawl.util;

import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: aldredl
 * Date: 19/12/2003
 * Time: 16:45:18
 * This class is the property of the "YAWL Project" - a collaborative
 * research effort between Queensland University of Technology and 
 * Eindhoven University of Technology.  You are not permitted to use, modify
 * or distribute this code without the express permission of a core
 * member of the YAWL Project. 
 * This class is not "open source".  This class is not for resale,
 * or commercial application.   It is intented for research purposes only.
 * The YAWL Project or it's members will not be held liable for any damage
 * occuring as a result of using this class.
 */
public class FileNameRewriter {
    public static void main(String[] args) {
        File currentDir = new File("/D:/YAWL/engine/src/org/yawlfoundation/yawl");
        cleanGarbage(currentDir, currentDir.list());
    }

    private static void cleanGarbage(File currentDir, String[] listOfFiles) {
        for (int i = 0; i < listOfFiles.length; i++) {
            String fileName = listOfFiles[i];

            File file = new File(currentDir.getAbsolutePath() + File.separator + fileName);
            if(fileName.endsWith(".txt")){
                String newFileNameStr = file.getAbsolutePath();
                newFileNameStr = newFileNameStr.substring(0, newFileNameStr.indexOf(".txt"));
                File newNameFile = new File(newFileNameStr);
            }
            if(file.isDirectory()){
                cleanGarbage(file, file.list());
            }

        }
    }
}
