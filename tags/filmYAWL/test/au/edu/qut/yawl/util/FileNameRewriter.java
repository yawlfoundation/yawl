/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.util;

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
        File currentDir = new File("/D:/YAWL/engine/src/au/edu/qut/yawl");
        cleanGarbage(currentDir, currentDir.list());
    }

    private static void cleanGarbage(File currentDir, String[] listOfFiles) {
        for (int i = 0; i < listOfFiles.length; i++) {
            String fileName = listOfFiles[i];

            File file = new File(currentDir.getAbsolutePath() + File.separator + fileName);
            if(fileName.endsWith(".txt")){
                String newFileNameStr = file.getAbsolutePath();
System.out.println("newFileNameStr = " + newFileNameStr);
                newFileNameStr = newFileNameStr.substring(0, newFileNameStr.indexOf(".txt"));
                File newNameFile = new File(newFileNameStr);
System.out.println("newNameFile = " + newNameFile);
System.out.println("rename succeeded = " + file.renameTo(newNameFile));
            }
            if(file.isDirectory()){
                cleanGarbage(file, file.list());
            }
/*            else if(fileName.endsWith(".java")){
                String newFileNameStr = file.getAbsolutePath();
                newFileNameStr = newFileNameStr + ".old";
                File newNameFile = new File(newFileNameStr);
System.out.println("newNameFile = " + newNameFile);
System.out.println("rename succeeded = " + file.renameTo(newNameFile));
            }*/
        }
    }
}
